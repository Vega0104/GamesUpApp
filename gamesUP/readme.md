# GamesUp API

API REST pour la gestion d'un catalogue de jeux de société.

## Technologies

- Java 17+
- Spring Boot
- Spring Data JPA
- Lombok
- H2

## Installation

1. Cloner le repository
2. Configurer la base de données dans `application.properties`
3. Lancer l'application : `mvn spring-boot:run`

## Endpoints

### Games

#### Détails d'un jeu
```
GET /details?gameID={id}
```

#### Ajouter un jeu
```
POST /add
```
**Paramètres** : `title`, `price`, `stock`, `authorID`, `categoryID`, `publisherID`

#### Filtrer les jeux
```
GET /filter?category={name}&author={name}&publisher={name}&name={title}
```

## Exemple
```bash
curl http://localhost:8080/details?gameID=1
```

## Base de données

L'API gère les entités suivantes :
- Game
- Category
- Publisher
- Author
- User
- Review
- PurchaseLine
