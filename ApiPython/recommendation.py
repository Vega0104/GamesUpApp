# # recommendation.py
# from models import UserData

# def generate_recommendations(user_data: UserData):
#     # À compléter avec un vrai algorithme de machine learning

#     # Pour l'instant, retourne une liste de jeux en exemple
#     recommendations = [
#         {"game_id": 101, "game_name": "Pandemic"},
#         {"game_id": 102, "game_name": "Catan"},
#         {"game_id": 103, "game_name": "Ticket to Ride"}
#     ]
#     return recommendations



from models import UserData, GameRecommendation
from knn_model import KNNRecommendationSystem
from typing import List
import logging

logger = logging.getLogger(__name__)

# Instance globale du modèle (sera initialisée au démarrage de l'API)
recommendation_model: KNNRecommendationSystem = None

def initialize_model(games_data: List[dict] = None, user_interactions: List[dict] = None):
    """
    Initialise et entraîne le modèle de recommandation
    
    Args:
        games_data: Données des jeux pour l'entraînement
        user_interactions: Historique des interactions utilisateur-jeu
    """
    global recommendation_model
    
    logger.info("Initialisation du modèle de recommandation KNN")
    recommendation_model = KNNRecommendationSystem(n_neighbors=5)
    
    # Si on a des données, entraîner le modèle
    if games_data and len(games_data) > 0:
        logger.info(f"Entraînement avec {len(games_data)} jeux")
        recommendation_model.fit(games_data, user_interactions)
    else:
        logger.warning("Aucune donnée fournie - le modèle ne sera pas entraîné")
        # Charger des données d'exemple pour le développement
        sample_games = _get_sample_games()
        recommendation_model.fit(sample_games)

def train_model(games_data: List[dict], user_interactions: List[dict] = None):
    """
    (Re)entraîne le modèle avec de nouvelles données
    
    Args:
        games_data: Nouvelles données des jeux
        user_interactions: Nouvelles interactions utilisateur
    """
    global recommendation_model
    
    if recommendation_model is None:
        recommendation_model = KNNRecommendationSystem(n_neighbors=5)
    
    logger.info("Réentraînement du modèle")
    recommendation_model.fit(games_data, user_interactions)

def generate_recommendations(
    user_data: UserData,
    num_recommendations: int = 5,
    exclude_owned: bool = True
) -> List[GameRecommendation]:
    """
    Génère des recommandations de jeux pour un utilisateur
    
    Args:
        user_data: Données de l'utilisateur
        num_recommendations: Nombre de recommandations à générer
        exclude_owned: Exclure les jeux déjà possédés
        
    Returns:
        Liste de recommandations
    """
    global recommendation_model
    
    if recommendation_model is None or not recommendation_model.is_trained:
        logger.error("Le modèle n'est pas initialisé ou entraîné")
        return _get_fallback_recommendations()
    
    try:
        # Convertir les données utilisateur en format attendu par le modèle
        user_purchases = [
            {
                'game_id': purchase.game_id,
                'rating': purchase.rating if purchase.rating else 3.0,
                'playtime_hours': purchase.playtime_hours if purchase.playtime_hours else 0
            }
            for purchase in user_data.purchases
        ]
        
        # Générer les recommandations
        recommendations = recommendation_model.recommend_for_user(
            user_purchases,
            n_recommendations=num_recommendations,
            exclude_owned=exclude_owned
        )
        
        # Convertir en format de réponse
        game_recommendations = []
        for game_id, score, reason in recommendations:
            # Récupérer le nom du jeu depuis le DataFrame du modèle
            game_name = _get_game_name(game_id)
            
            game_recommendations.append(
                GameRecommendation(
                    game_id=game_id,
                    game_name=game_name,
                    score=round(score, 3),
                    reason=reason
                )
            )
        
        logger.info(f"Généré {len(game_recommendations)} recommandations pour l'utilisateur {user_data.user_id}")
        return game_recommendations
        
    except Exception as e:
        logger.error(f"Erreur lors de la génération des recommandations: {str(e)}")
        return _get_fallback_recommendations()


def _get_game_name(game_id: int) -> str:
    """Récupère le nom d'un jeu depuis le modèle"""
    global recommendation_model

    # Map des noms de jeux réels (20 premiers jeux)
    game_names = {
        200: "Gloomhaven",
        201: "Terraforming Mars",
        202: "Brass: Birmingham",
        203: "Wingspan",
        204: "Scythe",
        205: "Azul",
        206: "Splendor",
        207: "7 Wonders",
        208: "Ticket to Ride",
        209: "Catan",
        210: "Pandemic",
        211: "Carcassonne",
        212: "Dominion",
        213: "Codenames",
        214: "King of Tokyo",
        215: "Spirit Island",
        216: "Root",
        217: "Everdell",
        218: "Clank!",
        219: "Viticulture"
    }

    # Si le nom est dans le dictionnaire, le retourner
    if game_id in game_names:
        return game_names[game_id]

    # Sinon, essayer de le récupérer du modèle
    try:
        if recommendation_model and recommendation_model.games_df is not None:
            game_row = recommendation_model.games_df[
                recommendation_model.games_df['game_id'] == game_id
                ]
            if not game_row.empty:
                # Tenter de récupérer le nom depuis le DataFrame si disponible
                return f"Game {game_id}"
    except Exception as e:
        logger.warning(f"Impossible de récupérer le nom du jeu {game_id}: {e}")

    return f"Game {game_id}"

def _get_fallback_recommendations() -> List[GameRecommendation]:
    """Retourne des recommandations par défaut en cas d'erreur"""
    logger.warning("Utilisation des recommandations de secours")
    return [
        GameRecommendation(
            game_id=101,
            game_name="Pandemic",
            score=0.85,
            reason="Jeu populaire - coopératif"
        ),
        GameRecommendation(
            game_id=102,
            game_name="Catan",
            score=0.82,
            reason="Jeu populaire - stratégie"
        ),
        GameRecommendation(
            game_id=103,
            game_name="Ticket to Ride",
            score=0.80,
            reason="Jeu populaire - famille"
        ),
        GameRecommendation(
            game_id=104,
            game_name="7 Wonders",
            score=0.78,
            reason="Jeu populaire - draft"
        ),
        GameRecommendation(
            game_id=105,
            game_name="Azul",
            score=0.75,
            reason="Jeu populaire - abstrait"
        )
    ]

def _get_sample_games() -> List[dict]:
    """
    Retourne des données d'exemple pour le développement
    Ces données seront remplacées par de vraies données en production
    """
    return [
        {
            'game_id': 101,
            'name': 'Pandemic',
            'categories': ['Strategy', 'Cooperative', 'Medical'],
            'mechanics': ['Hand Management', 'Action Points', 'Trading'],
            'min_players': 2,
            'max_players': 4,
            'avg_playtime_minutes': 45,
            'complexity': 2.4,
            'year_published': 2008,
            'average_rating': 4.2
        },
        {
            'game_id': 102,
            'name': 'Catan',
            'categories': ['Strategy', 'Negotiation', 'Economic'],
            'mechanics': ['Trading', 'Dice Rolling', 'Network Building'],
            'min_players': 3,
            'max_players': 4,
            'avg_playtime_minutes': 90,
            'complexity': 2.3,
            'year_published': 1995,
            'average_rating': 4.0
        },
        {
            'game_id': 103,
            'name': 'Ticket to Ride',
            'categories': ['Family', 'Strategy', 'Trains'],
            'mechanics': ['Set Collection', 'Route Building', 'Hand Management'],
            'min_players': 2,
            'max_players': 5,
            'avg_playtime_minutes': 60,
            'complexity': 1.9,
            'year_published': 2004,
            'average_rating': 4.1
        },
        {
            'game_id': 104,
            'name': '7 Wonders',
            'categories': ['Strategy', 'Card Game', 'Civilization'],
            'mechanics': ['Card Drafting', 'Set Collection', 'Variable Player Powers'],
            'min_players': 2,
            'max_players': 7,
            'avg_playtime_minutes': 30,
            'complexity': 2.3,
            'year_published': 2010,
            'average_rating': 4.3
        },
        {
            'game_id': 105,
            'name': 'Azul',
            'categories': ['Abstract', 'Puzzle', 'Family'],
            'mechanics': ['Pattern Building', 'Set Collection', 'Tile Placement'],
            'min_players': 2,
            'max_players': 4,
            'avg_playtime_minutes': 30,
            'complexity': 1.8,
            'year_published': 2017,
            'average_rating': 4.3
        },
        {
            'game_id': 106,
            'name': 'Wingspan',
            'categories': ['Strategy', 'Animals', 'Card Game'],
            'mechanics': ['Hand Management', 'Set Collection', 'Engine Building'],
            'min_players': 1,
            'max_players': 5,
            'avg_playtime_minutes': 70,
            'complexity': 2.4,
            'year_published': 2019,
            'average_rating': 4.4
        },
        {
            'game_id': 107,
            'name': 'Splendor',
            'categories': ['Strategy', 'Economic', 'Renaissance'],
            'mechanics': ['Set Collection', 'Engine Building', 'Card Development'],
            'min_players': 2,
            'max_players': 4,
            'avg_playtime_minutes': 30,
            'complexity': 1.8,
            'year_published': 2014,
            'average_rating': 4.0
        },
        {
            'game_id': 108,
            'name': 'Dominion',
            'categories': ['Strategy', 'Card Game', 'Medieval'],
            'mechanics': ['Deck Building', 'Hand Management', 'Card Drafting'],
            'min_players': 2,
            'max_players': 4,
            'avg_playtime_minutes': 30,
            'complexity': 2.4,
            'year_published': 2008,
            'average_rating': 4.1
        },
        {
            'game_id': 109,
            'name': 'Codenames',
            'categories': ['Party Game', 'Word Game', 'Deduction'],
            'mechanics': ['Team Play', 'Pattern Recognition', 'Communication'],
            'min_players': 4,
            'max_players': 8,
            'avg_playtime_minutes': 15,
            'complexity': 1.3,
            'year_published': 2015,
            'average_rating': 4.2
        },
        {
            'game_id': 110,
            'name': 'Terraforming Mars',
            'categories': ['Strategy', 'Economic', 'Science Fiction'],
            'mechanics': ['Card Drafting', 'Hand Management', 'Tile Placement'],
            'min_players': 1,
            'max_players': 5,
            'avg_playtime_minutes': 120,
            'complexity': 3.2,
            'year_published': 2016,
            'average_rating': 4.5
        }
    ]