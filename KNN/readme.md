# API de Recommandation - KNN

## Installation
```bash
cd KNN
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Démarrage
```bash
python -m uvicorn main:app --port 8001
```

## Entraînement du modèle
```bash
python train_with_synthetic.py
```

## Endpoints principaux

- **Documentation** : http://localhost:8001/docs
- **Health** : `GET /health`
- **Recommandations** : `POST /recommendations/simple/`
- **Jeux similaires** : `GET /games/{id}/similar`
- **Info modèle** : `GET /model/info`

## Test
```bash
python test_api.py
```

## Données

100 jeux synthétiques (IDs 200-299) dans `synthetic_games.json`
