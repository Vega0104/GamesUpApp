
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import logging
from contextlib import asynccontextmanager

from recommendation import (
    generate_recommendations,
    initialize_model,
    train_model
)
from models import (
    UserData,
    GameFeatures,
    RecommendationRequest,
    RecommendationResponse,
    GameRecommendation
)

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Gestion du cycle de vie de l'application
@asynccontextmanager
async def lifespan(app: FastAPI):
    """Initialise le modèle au démarrage de l'API"""
    logger.info("Démarrage de l'API - Initialisation du modèle")
    initialize_model()
    logger.info("Modèle initialisé avec succès")
    yield
    logger.info("Arrêt de l'API")

# Création de l'application FastAPI
app = FastAPI(
    title="API de Recommandation de Jeux de Société",
    description="API utilisant KNN pour recommander des jeux de société basés sur les préférences utilisateur",
    version="1.0.0",
    lifespan=lifespan
)

# Configuration CORS pour permettre les requêtes depuis l'API Spring
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # À restreindre en production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================================================
# ENDPOINTS PRINCIPAUX
# ============================================================================

@app.get("/")
async def root():
    """Endpoint de base pour tester que l'API est en ligne"""
    return {
        "message": "API de recommandation de jeux de société en ligne",
        "version": "1.0.0",
        "status": "operational"
    }

@app.get("/health")
async def health_check():
    """Vérification de la santé de l'API"""
    return {
        "status": "healthy",
        "model_loaded": True  # À améliorer avec une vraie vérification
    }

@app.post("/recommendations/", response_model=RecommendationResponse)
async def get_recommendations(request: RecommendationRequest):
    """
    Endpoint principal pour obtenir des recommandations de jeux
    
    Args:
        request: Requête contenant les données utilisateur et paramètres
        
    Returns:
        Liste de recommandations avec scores
    """
    try:
        logger.info(f"Requête de recommandation pour l'utilisateur {request.user_data.user_id}")
        
        # Générer les recommandations
        recommendations = generate_recommendations(
            user_data=request.user_data,
            num_recommendations=request.num_recommendations,
            exclude_owned=request.exclude_owned
        )
        
        return RecommendationResponse(
            user_id=request.user_data.user_id,
            recommendations=recommendations
        )
        
    except Exception as e:
        logger.error(f"Erreur lors de la génération des recommandations: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/recommendations/simple/", response_model=List[GameRecommendation])
async def get_recommendations_simple(data: UserData, num_recommendations: int = 5):
    """
    Endpoint simplifié pour les recommandations (compatible avec l'ancien format)
    
    Args:
        data: Données utilisateur
        num_recommendations: Nombre de recommandations
        
    Returns:
        Liste de recommandations
    """
    try:
        recommendations = generate_recommendations(
            user_data=data,
            num_recommendations=num_recommendations,
            exclude_owned=True
        )
        return recommendations
    except Exception as e:
        logger.error(f"Erreur: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

# ============================================================================
# ENDPOINTS D'ADMINISTRATION / TRAINING
# ============================================================================

class TrainingData(BaseModel):
    """Données pour entraîner le modèle"""
    games: List[dict]
    user_interactions: Optional[List[dict]] = None

@app.post("/admin/train")
async def train_model_endpoint(
    training_data: TrainingData,
    background_tasks: BackgroundTasks
):
    """
    Endpoint pour (re)entraîner le modèle avec de nouvelles données
    
    Args:
        training_data: Données de jeux et interactions utilisateur
        background_tasks: Tâches en arrière-plan
        
    Returns:
        Statut de l'entraînement
    """
    try:
        logger.info(f"Démarrage de l'entraînement avec {len(training_data.games)} jeux")
        
        # Lancer l'entraînement en arrière-plan
        background_tasks.add_task(
            train_model,
            training_data.games,
            training_data.user_interactions
        )
        
        return {
            "status": "training_started",
            "message": f"Entraînement lancé avec {len(training_data.games)} jeux",
            "games_count": len(training_data.games),
            "interactions_count": len(training_data.user_interactions) if training_data.user_interactions else 0
        }
        
    except Exception as e:
        logger.error(f"Erreur lors du lancement de l'entraînement: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

class GameDataUpload(BaseModel):
    """Upload de données de jeux"""
    games: List[dict]

@app.post("/admin/games/upload")
async def upload_games(game_data: GameDataUpload):
    """
    Upload de nouvelles données de jeux dans le système
    
    Args:
        game_data: Liste de jeux avec leurs caractéristiques
        
    Returns:
        Statut de l'upload
    """
    try:
        logger.info(f"Upload de {len(game_data.games)} jeux")
        
        # Ici, vous devriez sauvegarder les jeux dans une base de données
        # Pour l'instant, on les utilise juste pour réentraîner le modèle
        train_model(game_data.games)
        
        return {
            "status": "success",
            "message": f"{len(game_data.games)} jeux uploadés et modèle réentraîné",
            "games_count": len(game_data.games)
        }
        
    except Exception as e:
        logger.error(f"Erreur lors de l'upload: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

# ============================================================================
# ENDPOINTS D'INFORMATION
# ============================================================================

@app.get("/games/{game_id}/similar")
async def get_similar_games(game_id: int, limit: int = 5):
    """
    Trouve des jeux similaires à un jeu donné
    
    Args:
        game_id: ID du jeu de référence
        limit: Nombre de jeux similaires à retourner
        
    Returns:
        Liste de jeux similaires avec scores de similarité
    """
    try:
        from recommendation import recommendation_model
        
        if recommendation_model is None or not recommendation_model.is_trained:
            raise HTTPException(
                status_code=503,
                detail="Le modèle n'est pas initialisé"
            )
        
        similar_games = recommendation_model.get_similar_games(
            game_id=game_id,
            n_recommendations=limit
        )
        
        return {
            "game_id": game_id,
            "similar_games": [
                {
                    "game_id": gid,
                    "similarity_score": round(score, 3)
                }
                for gid, score in similar_games
            ]
        }
        
    except Exception as e:
        logger.error(f"Erreur: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/model/info")
async def get_model_info():
    """Retourne des informations sur le modèle actuel"""
    try:
        from recommendation import recommendation_model
        
        if recommendation_model is None:
            return {
                "status": "not_initialized",
                "message": "Le modèle n'est pas initialisé"
            }
        
        return {
            "status": "operational",
            "is_trained": recommendation_model.is_trained,
            "n_neighbors": recommendation_model.n_neighbors,
            "games_count": len(recommendation_model.game_id_to_idx) if recommendation_model.is_trained else 0,
            "algorithm": "KNN (K-Nearest Neighbors)",
            "metric": "cosine similarity",
            "version": "1.0.0"
        }
        
    except Exception as e:
        logger.error(f"Erreur: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

# ============================================================================
# ENDPOINT POUR TESTS
# ============================================================================

@app.post("/test/recommendations")
async def test_recommendations():
    """
    Endpoint de test avec des données d'exemple
    Utile pour vérifier que tout fonctionne
    """
    from models import UserPurchase
    
    # Créer un utilisateur de test
    test_user = UserData(
        user_id=999,
        purchases=[
            UserPurchase(game_id=200, rating=4.5, playtime_hours=10),
            UserPurchase(game_id=203, rating=4.0, playtime_hours=5),
            UserPurchase(game_id=206, rating=5.0, playtime_hours=20)
        ]
    )
    
    try:
        recommendations = generate_recommendations(
            user_data=test_user,
            num_recommendations=5,
            exclude_owned=True
        )
        
        return {
            "test_user_id": test_user.user_id,
            "test_purchases": [p.game_id for p in test_user.purchases],
            "recommendations": recommendations
        }
        
    except Exception as e:
        logger.error(f"Erreur lors du test: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)