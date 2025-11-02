"""
Script de test pour l'API de recommandation
"""
import requests
import json

# Configuration
API_URL = "http://localhost:8001"

def test_health():
    """Test du endpoint de santé"""
    print("\n=== Test Health Check ===")
    response = requests.get(f"{API_URL}/health")
    print(f"Status: {response.status_code}")
    print(f"Response: {response.json()}")

def test_model_info():
    """Test des infos du modèle"""
    print("\n=== Test Model Info ===")
    response = requests.get(f"{API_URL}/model/info")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

def test_simple_recommendation():
    """Test avec des données utilisateur simples"""
    print("\n=== Test Simple Recommendation ===")
    
    user_data = {
        "user_id": 123,
        "purchases": [
            {"game_id": 200, "rating": 4.5, "playtime_hours": 10},  # Gloomhaven
            {"game_id": 203, "rating": 4.0, "playtime_hours": 5}    # Wingspan
        ]
    }
    
    response = requests.post(
        f"{API_URL}/recommendations/simple/",
        json=user_data,
        params={"num_recommendations": 5}
    )
    
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        if result:
            print(f"Response: {json.dumps(result, indent=2)}")
            print(f"\n✅ {len(result)} recommandations trouvées")
        else:
            print("⚠️  Aucune recommandation (normal si les jeux sont trop différents)")
    else:
        print(f"❌ Erreur: {response.text}")

def test_full_recommendation():
    """Test avec le format complet de requête"""
    print("\n=== Test Full Recommendation ===")
    
    request_data = {
        "user_data": {
            "user_id": 456,
            "purchases": [
                {"game_id": 200, "rating": 5.0, "playtime_hours": 20},  # Gloomhaven
                {"game_id": 205, "rating": 4.5, "playtime_hours": 15},  # Scythe
                {"game_id": 210, "rating": 4.0, "playtime_hours": 10}   # Catan
            ],
            "age": 30,
            "preferred_player_count": 4
        },
        "num_recommendations": 5,
        "exclude_owned": True
    }
    
    response = requests.post(
        f"{API_URL}/recommendations/",
        json=request_data
    )
    
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print(f"Response: {json.dumps(result, indent=2)}")
        if result.get('recommendations'):
            print(f"\n✅ {len(result['recommendations'])} recommandations trouvées")
        else:
            print("⚠️  Aucune recommandation")
    else:
        print(f"❌ Erreur: {response.text}")

def test_similar_games():
    """Test de recherche de jeux similaires"""
    print("\n=== Test Similar Games ===")
    
    game_id = 200  # Premier jeu synthétique (Gloomhaven)
    response = requests.get(
        f"{API_URL}/games/{game_id}/similar",
        params={"limit": 5}
    )
    
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print(f"Response: {json.dumps(result, indent=2)}")
        if result.get('similar_games'):
            print(f"\n✅ {len(result['similar_games'])} jeux similaires trouvés")
        else:
            print("⚠️  Aucun jeu similaire trouvé")
    else:
        print(f"❌ Erreur: {response.text}")

def test_multiple_similar_games():
    """Test de plusieurs jeux similaires"""
    print("\n=== Test Multiple Similar Games ===")
    
    # Tester 3 jeux différents
    test_games = [200, 205, 210]  # Gloomhaven, Scythe, Catan
    
    for game_id in test_games:
        response = requests.get(
            f"{API_URL}/games/{game_id}/similar",
            params={"limit": 3}
        )
        
        if response.status_code == 200:
            result = response.json()
            similar = result.get('similar_games', [])
            if similar:
                print(f"\n🎲 Jeu {game_id} - {len(similar)} jeux similaires:")
                for game in similar[:3]:
                    print(f"   • ID {game['game_id']}: score {game['similarity_score']:.3f}")
            else:
                print(f"\n🎲 Jeu {game_id} - Aucun jeu similaire")

def test_recommendations_with_ratings():
    """Test avec différents ratings pour voir l'impact"""
    print("\n=== Test Recommendations avec différents ratings ===")
    
    # Utilisateur qui aime beaucoup certains jeux
    user_data = {
        "user_id": 789,
        "purchases": [
            {"game_id": 200, "rating": 5.0, "playtime_hours": 50},  # Très aimé
            {"game_id": 201, "rating": 4.8, "playtime_hours": 40},  # Très aimé
            {"game_id": 202, "rating": 3.0, "playtime_hours": 5},   # Peu aimé
        ]
    }
    
    response = requests.post(
        f"{API_URL}/recommendations/simple/",
        json=user_data,
        params={"num_recommendations": 5}
    )
    
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        if result:
            print(f"Recommandations basées sur les ratings:")
            for i, rec in enumerate(result[:5], 1):
                print(f"  {i}. ID {rec['game_id']}: {rec.get('game_name', 'N/A')} (score: {rec['score']:.3f})")
        else:
            print("⚠️  Aucune recommandation")

def test_list_available_games():
    """Liste quelques jeux disponibles"""
    print("\n=== Jeux disponibles (échantillon) ===")
    
    # Charger le fichier pour voir les jeux
    try:
        with open('synthetic_games.json', 'r', encoding='utf-8') as f:
            games = json.load(f)
        
        print(f"Total: {len(games)} jeux disponibles (IDs {games[0]['game_id']} à {games[-1]['game_id']})\n")
        print("📊 Premiers jeux:")
        for i, game in enumerate(games[:10], 1):
            print(f"  {i:2}. ID {game['game_id']:3} - {game['name']:30} | {game['complexity']}/5 | {game['average_rating']}/5")
    except FileNotFoundError:
        print("⚠️  Fichier synthetic_games.json non trouvé")

def test_endpoint_test():
    """Test du endpoint de test intégré"""
    print("\n=== Test Endpoint Test ===")
    
    response = requests.post(f"{API_URL}/test/recommendations")
    
    print(f"Status: {response.status_code}")
    result = response.json()
    print(f"Response: {json.dumps(result, indent=2)}")
    
    if result.get('recommendations'):
        print(f"\n✅ {len(result['recommendations'])} recommandations")
    else:
        print("\n⚠️  Aucune recommandation (les IDs de test ne correspondent peut-être pas)")

if __name__ == "__main__":
    print("🧪 Démarrage des tests de l'API de recommandation")
    print("=" * 70)
    
    try:
        # Tests de base
        test_health()
        test_model_info()
        test_list_available_games()
        
        # Tests de recommandations
        test_simple_recommendation()
        test_full_recommendation()
        test_recommendations_with_ratings()
        
        # Tests de similarité
        test_similar_games()
        test_multiple_similar_games()
        
        # Test de l'endpoint intégré
        test_endpoint_test()
        
        print("\n" + "=" * 70)
        print("✅ Tous les tests sont terminés !")
        
    except requests.exceptions.ConnectionError:
        print("\n❌ ERREUR: Impossible de se connecter à l'API")
        print("Assurez-vous que l'API est démarrée avec: uvicorn main:app --reload --port 8001")
    except Exception as e:
        print(f"\n❌ ERREUR: {str(e)}")