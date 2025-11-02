from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime

class UserPurchase(BaseModel):
    """Représente un achat/interaction d'un utilisateur avec un jeu"""
    game_id: int
    rating: Optional[float] = Field(None, ge=0, le=5, description="Note de 0 à 5")
    playtime_hours: Optional[float] = Field(None, ge=0, description="Temps de jeu en heures")
    purchase_date: Optional[datetime] = None

class UserData(BaseModel):
    """Données d'un utilisateur pour la recommandation"""
    user_id: int
    purchases: List[UserPurchase]
    age: Optional[int] = None
    preferred_player_count: Optional[int] = None

class GameFeatures(BaseModel):
    """Caractéristiques d'un jeu de société"""
    game_id: int
    name: str
    categories: List[str] = Field(default_factory=list, description="Ex: Strategy, Family, Party")
    mechanics: List[str] = Field(default_factory=list, description="Ex: Deck Building, Worker Placement")
    min_players: int = Field(default=1, ge=1)
    max_players: int = Field(default=10, ge=1)
    avg_playtime_minutes: int = Field(default=60, ge=1)
    complexity: float = Field(default=2.5, ge=1, le=5, description="Complexité de 1 à 5")
    year_published: Optional[int] = None
    average_rating: Optional[float] = Field(None, ge=0, le=5)

class RecommendationRequest(BaseModel):
    """Requête pour obtenir des recommandations"""
    user_data: UserData
    num_recommendations: int = Field(default=5, ge=1, le=20)
    exclude_owned: bool = Field(default=True, description="Exclure les jeux déjà possédés")

class GameRecommendation(BaseModel):
    """Une recommandation de jeu"""
    game_id: int
    game_name: str
    score: float = Field(description="Score de similarité")
    reason: Optional[str] = Field(None, description="Raison de la recommandation")

class RecommendationResponse(BaseModel):
    """Réponse contenant les recommandations"""
    user_id: int
    recommendations: List[GameRecommendation]
    model_version: str = "KNN-v1.0"