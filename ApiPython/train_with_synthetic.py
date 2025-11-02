import json
import requests

print("📂 Chargement de synthetic_games.json...")

with open('synthetic_games.json', 'r', encoding='utf-8') as f:
    games = json.load(f)

print(f"📦 {len(games)} jeux chargés")
print("🚀 Envoi à l'API pour entraînement...\n")

response = requests.post(
    'http://localhost:8001/admin/train',
    json={'games': games},
    timeout=30
)

if response.status_code == 200:
    result = response.json()
    print(f"✅ Entraînement réussi!")
    print(f"   📦 {result.get('games_count')} jeux")
    print(f"   📊 Status: {result.get('status')}")
    print("\n🧪 Testez maintenant:")
    print("   python test_api.py")
    print("   curl http://localhost:8001/model/info")
else:
    print(f"❌ Erreur: {response.text}")
