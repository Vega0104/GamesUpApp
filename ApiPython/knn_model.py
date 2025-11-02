import numpy as np
import pandas as pd
from sklearn.neighbors import NearestNeighbors
from sklearn.preprocessing import StandardScaler
from typing import List, Dict, Tuple
import logging

logger = logging.getLogger(__name__)

class KNNRecommendationSystem:
    """
    Système de recommandation basé sur KNN (K-Nearest Neighbors)
    Utilise une approche item-based : recommande des jeux similaires aux jeux que l'utilisateur aime
    """
    
    def __init__(self, n_neighbors: int = 5):
        """
        Initialise le système de recommandation
        
        Args:
            n_neighbors: Nombre de voisins à considérer pour KNN
        """
        self.n_neighbors = n_neighbors
        self.model = None
        self.scaler = StandardScaler()
        self.games_df = None
        self.user_item_matrix = None
        self.game_features_matrix = None
        self.game_id_to_idx = {}
        self.idx_to_game_id = {}
        self.is_trained = False
        
    def prepare_game_features(self, games_data: List[Dict]) -> pd.DataFrame:
        """
        Prépare les features des jeux pour le modèle
        
        Args:
            games_data: Liste de dictionnaires contenant les données des jeux
            
        Returns:
            DataFrame avec les features encodées
        """
        df = pd.DataFrame(games_data)
        
        # Créer des features numériques
        features = pd.DataFrame()
        features['game_id'] = df['game_id']
        features['min_players'] = df['min_players']
        features['max_players'] = df['max_players']
        features['avg_playtime_minutes'] = df['avg_playtime_minutes']
        features['complexity'] = df['complexity']
        features['average_rating'] = df.get('average_rating', 3.0)
        
        # Encoder les catégories (one-hot encoding)
        all_categories = set()
        for categories in df['categories']:
            all_categories.update(categories)
        
        for category in all_categories:
            features[f'cat_{category}'] = df['categories'].apply(
                lambda x: 1 if category in x else 0
            )
        
        # Encoder les mécaniques (one-hot encoding)
        all_mechanics = set()
        for mechanics in df['mechanics']:
            all_mechanics.update(mechanics)
        
        for mechanic in all_mechanics:
            features[f'mech_{mechanic}'] = df['mechanics'].apply(
                lambda x: 1 if mechanic in x else 0
            )
        
        return features
    
    def create_user_item_matrix(self, user_interactions: List[Dict]) -> pd.DataFrame:
        """
        Crée une matrice utilisateur-item à partir des interactions
        
        Args:
            user_interactions: Liste d'interactions {user_id, game_id, rating, playtime_hours}
            
        Returns:
            DataFrame pivot avec users en lignes et games en colonnes
        """
        df = pd.DataFrame(user_interactions)
        
        # Créer un score combiné basé sur rating et playtime
        if 'rating' in df.columns and 'playtime_hours' in df.columns:
            # Normaliser le temps de jeu (log scale pour éviter les outliers)
            df['playtime_normalized'] = np.log1p(df['playtime_hours'].fillna(0))
            # Score combiné : 70% rating, 30% playtime
            df['score'] = (
                df['rating'].fillna(0) * 0.7 + 
                df['playtime_normalized'] / df['playtime_normalized'].max() * 5 * 0.3
            )
        elif 'rating' in df.columns:
            df['score'] = df['rating'].fillna(0)
        else:
            df['score'] = 1.0  # Score par défaut si pas d'info
        
        # Créer la matrice pivot
        matrix = df.pivot_table(
            index='user_id',
            columns='game_id',
            values='score',
            fill_value=0
        )
        
        return matrix
    
    def fit(self, games_data: List[Dict], user_interactions: List[Dict] = None):
        """
        Entraîne le modèle KNN avec les données fournies
        
        Args:
            games_data: Données sur les jeux (caractéristiques)
            user_interactions: Interactions utilisateur-jeu (optionnel pour item-based)
        """
        logger.info(f"Entraînement du modèle KNN avec {len(games_data)} jeux")
        
        # Préparer les features des jeux
        self.games_df = self.prepare_game_features(games_data)
        
        # Créer le mapping game_id <-> index
        self.game_id_to_idx = {
            game_id: idx for idx, game_id in enumerate(self.games_df['game_id'])
        }
        self.idx_to_game_id = {
            idx: game_id for game_id, idx in self.game_id_to_idx.items()
        }
        
        # Extraire la matrice de features (sans game_id)
        feature_columns = [col for col in self.games_df.columns if col != 'game_id']
        self.game_features_matrix = self.games_df[feature_columns].values
        
        # Normaliser les features
        self.game_features_matrix = self.scaler.fit_transform(self.game_features_matrix)
        
        # Si on a des interactions utilisateur, les intégrer
        if user_interactions and len(user_interactions) > 0:
            self.user_item_matrix = self.create_user_item_matrix(user_interactions)
            logger.info(f"Matrice utilisateur-item créée: {self.user_item_matrix.shape}")
        
        # Entraîner le modèle KNN
        self.model = NearestNeighbors(
            n_neighbors=min(self.n_neighbors + 1, len(games_data)),  # +1 car le jeu lui-même sera inclus
            metric='cosine',
            algorithm='brute'
        )
        self.model.fit(self.game_features_matrix)
        
        self.is_trained = True
        logger.info("Modèle KNN entraîné avec succès")
    
    def get_similar_games(self, game_id: int, n_recommendations: int = 5) -> List[Tuple[int, float]]:
        """
        Trouve les jeux les plus similaires à un jeu donné
        
        Args:
            game_id: ID du jeu de référence
            n_recommendations: Nombre de recommandations à retourner
            
        Returns:
            Liste de tuples (game_id, similarity_score)
        """
        if not self.is_trained:
            raise ValueError("Le modèle n'est pas entraîné. Appelez fit() d'abord.")
        
        if game_id not in self.game_id_to_idx:
            logger.warning(f"Jeu {game_id} non trouvé dans la base")
            return []
        
        # Obtenir l'index du jeu
        game_idx = self.game_id_to_idx[game_id]
        game_features = self.game_features_matrix[game_idx].reshape(1, -1)
        
        # Trouver les voisins les plus proches
        distances, indices = self.model.kneighbors(
            game_features,
            n_neighbors=min(n_recommendations + 1, len(self.game_id_to_idx))
        )
        
        # Convertir les distances en scores de similarité (1 - distance)
        # Exclure le premier résultat (le jeu lui-même)
        similar_games = []
        for idx, distance in zip(indices[0][1:], distances[0][1:]):
            similar_game_id = self.idx_to_game_id[idx]
            similarity_score = max(0,1 - distance)  # Cosine similarity
            similar_games.append((similar_game_id, float(similarity_score)))
        
        return similar_games[:n_recommendations]
    
    def recommend_for_user(
        self,
        user_purchases: List[Dict],
        n_recommendations: int = 5,
        exclude_owned: bool = True
    ) -> List[Tuple[int, float, str]]:
        """
        Génère des recommandations pour un utilisateur basé sur ses achats
        
        Args:
            user_purchases: Liste des achats de l'utilisateur avec ratings
            n_recommendations: Nombre de recommandations
            exclude_owned: Si True, exclut les jeux déjà possédés
            
        Returns:
            Liste de tuples (game_id, score, reason)
        """
        if not self.is_trained:
            raise ValueError("Le modèle n'est pas entraîné. Appelez fit() d'abord.")
        
        if not user_purchases:
            logger.warning("Aucun achat fourni pour l'utilisateur")
            return self._get_popular_games(n_recommendations)
        
        # Récupérer les jeux possédés par l'utilisateur
        owned_game_ids = {p['game_id'] for p in user_purchases}
        
        # Calculer un score pour chaque jeu basé sur les achats de l'utilisateur
        game_scores = {}
        reasons = {}
        
        for purchase in user_purchases:
            game_id = purchase['game_id']
            rating = purchase.get('rating', 3.0)
            
            # Plus le rating est élevé, plus on donne de poids à ce jeu
            weight = rating / 5.0 if rating else 0.6
            
            # Trouver des jeux similaires
            similar_games = self.get_similar_games(game_id, n_recommendations * 2)
            
            for similar_game_id, similarity in similar_games:
                if exclude_owned and similar_game_id in owned_game_ids:
                    continue
                
                # Score pondéré par le rating de l'utilisateur
                score = similarity * weight
                
                if similar_game_id in game_scores:
                    game_scores[similar_game_id] += score
                else:
                    game_scores[similar_game_id] = score
                    reasons[similar_game_id] = f"Similaire à game_id {game_id}"
        
        # Trier par score décroissant
        sorted_recommendations = sorted(
            game_scores.items(),
            key=lambda x: x[1],
            reverse=True
        )[:n_recommendations]
        
        # Formater les résultats
        recommendations = [
            (game_id, score, reasons[game_id])
            for game_id, score in sorted_recommendations
        ]
        
        return recommendations
    
    def _get_popular_games(self, n: int = 5) -> List[Tuple[int, float, str]]:
        """
        Retourne les jeux les mieux notés comme recommandations par défaut
        
        Args:
            n: Nombre de jeux a retourner
            
        Returns:
            Liste de tuples (game_id, score, reason)
        """
        if self.games_df is None or len(self.games_df) == 0:
            return []
        
        # Trier par rating moyen
        sorted_games = self.games_df.sort_values('average_rating', ascending=False)
        top_games = sorted_games.head(n)
        
        recommendations = [
            (
                int(row['game_id']),
                float(row['average_rating']) / 5.0,
                "Jeu populaire (nouveau utilisateur)"
            )
            for _, row in top_games.iterrows()
        ]
        
        return recommendations
    
    def save_model(self, filepath: str):
        """Sauvegarde le modèle (à implémenter avec pickle ou joblib)"""
        import pickle
        model_data = {
            'model': self.model,
            'scaler': self.scaler,
            'games_df': self.games_df,
            'game_id_to_idx': self.game_id_to_idx,
            'idx_to_game_id': self.idx_to_game_id,
            'game_features_matrix': self.game_features_matrix,
            'n_neighbors': self.n_neighbors,
            'is_trained': self.is_trained
        }
        with open(filepath, 'wb') as f:
            pickle.dump(model_data, f)
        logger.info(f"Modèle sauvegardé dans {filepath}")
    
    def load_model(self, filepath: str):
        """Charge un modèle sauvegardé"""
        import pickle
        with open(filepath, 'rb') as f:
            model_data = pickle.load(f)
        
        self.model = model_data['model']
        self.scaler = model_data['scaler']
        self.games_df = model_data['games_df']
        self.game_id_to_idx = model_data['game_id_to_idx']
        self.idx_to_game_id = model_data['idx_to_game_id']
        self.game_features_matrix = model_data['game_features_matrix']
        self.n_neighbors = model_data['n_neighbors']
        self.is_trained = model_data['is_trained']
        logger.info(f"Modèle chargé depuis {filepath}")