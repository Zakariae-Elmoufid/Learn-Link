# Module Gamification - Documentation des Endpoints

## Vue d'ensemble
Le module de gamification récompense les utilisateurs avec des points, des niveaux et des badges pour encourager l'engagement dans la plateforme.

## Architecture

### Entités
- **UserScore**: Score de l'utilisateur (points, niveau, progression)
- **Badge**: Définition des badges disponibles
- **UserBadge**: Association utilisateur-badge avec date d'obtention

### Enums
- **BadgeType**: ACTION, ACHIEVEMENT, MILESTONE
- **BadgeRarity**: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY

## Endpoints

### 1. Gestion des Points et Scores

#### GET `/api/gamification/score`
Récupère le score de l'utilisateur connecté
**Paramètres**: 
- Header: `X-User-Id` (Long)
**Réponse**: UserScoreResponse

```json
{
  "userId": 1,
  "totalPoints": 450,
  "level": 3,
  "currentLevelPoints": 50,
  "pointsForNextLevel": 150,
  "progressPercentage": 33.33
}
```

#### GET `/api/gamification/score/{userId}`
Récupère le score d'un utilisateur
**Réponse**: UserScoreResponse

#### POST `/api/gamification/points`
Ajoute des points à un utilisateur
**Paramètres**:
- `userId` (query)
- `AddPointsRequest` (body)

```json
{
  "points": 50,
  "actionType": "POST_CREATED"
}
```

**Réponse**: UserScoreResponse

### 2. Profil Public

#### GET `/api/gamification/profile/{userId}`
Récupère le profil public d'un utilisateur (niveau, badges, rang)
**Réponse**: UserPublicProfileResponse

```json
{
  "userId": 1,
  "level": 3,
  "totalPoints": 450,
  "rank": 5,
  "badgeCount": 3,
  "badges": [
    {
      "badgeId": 1,
      "code": "FIRST_POST",
      "name": "First Step",
      "iconUrl": "https://...",
      "rarity": "COMMON",
      "earnedAt": "2026-02-10T10:00:00Z"
    }
  ]
}
```

### 3. Gestion des Badges

#### GET `/api/gamification/badges`
Liste tous les badges disponibles

#### GET `/api/gamification/badges/{badgeId}`
Récupère un badge par ID

#### GET `/api/gamification/badges/code/{code}`
Récupère un badge par code

#### GET `/api/gamification/badges/active`
Liste les badges actifs

#### POST `/api/gamification/badges`
Crée un nouveau badge (ADMIN)
```json
{
  "code": "NEW_BADGE",
  "name": "Badge Name",
  "description": "Description",
  "iconUrl": "https://...",
  "type": "ACHIEVEMENT",
  "rarity": "RARE",
  "pointsRequired": 500
}
```

#### PUT `/api/gamification/badges/{badgeId}`
Met à jour un badge

#### DELETE `/api/gamification/badges/{badgeId}`
Désactive un badge

### 4. Badges d'Utilisateur

#### GET `/api/gamification/user-badges/{userId}`
Liste les badges d'un utilisateur

#### GET `/api/gamification/user-badges/{userId}/count`
Compte le nombre de badges d'un utilisateur

#### GET `/api/gamification/user-badges/{userId}/has/{badgeId}`
Vérifie si un utilisateur possède un badge

#### POST `/api/gamification/user-badges/{userId}/award/{badgeId}`
Attribue un badge à un utilisateur

### 5. Leaderboard

#### GET `/api/gamification/leaderboard/global`
Classement global (100 meilleurs par défaut)
**Paramètres**: `limit` (default: 100)

**Réponse**:
```json
[
  {
    "userId": 1,
    "username": "john_doe",
    "level": 5,
    "totalPoints": 1250,
    "rank": 1,
    "badgeCount": 5
  }
]
```

#### GET `/api/gamification/leaderboard/weekly`
Classement hebdomadaire (50 meilleurs par défaut)
**Paramètres**: `limit` (default: 50)

#### GET `/api/gamification/leaderboard/rank/{userId}`
Rang d'un utilisateur
**Réponse**: Integer

#### GET `/api/gamification/leaderboard/rank-percentage/{userId}`
Pourcentage de rang (100% = meilleur)
**Réponse**: Long

## Système de Points

### Calcul des niveaux
- Niveau 1: 100 points
- Niveau 2: 150 points
- Niveau 3: 200 points
- Chaque niveau ajoute 50 points

### Actions récompensées
- Création de post: 8 points
- Réponse acceptée: 25 points
- Like reçu: 2 points
- Nouvelle connexion: 5 points
- Tâche complétée: 10 points

## Badges Pré-configurés

| Code | Nom | Type | Rareté | Points |
|------|-----|------|--------|--------|
| FIRST_POST | First Step | ACTION | COMMON | 0 |
| HELPFUL_EXPERT | Helpful Expert | ACHIEVEMENT | RARE | 500 |
| COMMUNITY_LEADER | Community Leader | MILESTONE | EPIC | 1000 |
| STREAK_MASTER | Streak Master | MILESTONE | RARE | 800 |
| LEVEL_10 | Legend | MILESTONE | LEGENDARY | 2000 |
| QUESTION_MASTER | Question Master | ACHIEVEMENT | RARE | 600 |
| SOCIAL_BUTTERFLY | Social Butterfly | ACHIEVEMENT | UNCOMMON | 300 |
| KNOWLEDGE_SEEKER | Knowledge Seeker | ACHIEVEMENT | UNCOMMON | 400 |

## Exemples d'utilisation cURL

### Ajouter des points
```bash
curl -X POST http://localhost:8080/api/gamification/points \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "points": 50,
    "actionType": "POST_CREATED"
  }'
```

### Récupérer le profil public
```bash
curl http://localhost:8080/api/gamification/profile/1
```

### Obtenir le leaderboard global
```bash
curl "http://localhost:8080/api/gamification/leaderboard/global?limit=50"
```

### Attribuer un badge
```bash
curl -X POST http://localhost:8080/api/gamification/user-badges/1/award/1
```

## Flux d'intégration avec d'autres modules

### Avec le module Community
- Post créé → +8 points
- Réponse acceptée → +25 points
- Like reçu → +2 points

### Avec le module Planner
- Tâche complétée → +10 points

### Avec le module Matching
- Nouvelle connexion → +5 points

## Sécurité

- ✅ Authentification JWT requise
- ✅ Isolation des utilisateurs (users ne voient que leurs données)
- ✅ ADMIN uniquement pour la gestion des badges
- ✅ Validation des entrées
- ✅ Transactions database

## Notes de développement

- Les points ne peuvent pas être négatifs
- Les niveaux sont calculés automatiquement
- Les badges sont attribués une seule fois par utilisateur
- Les leaderboards sont générés à partir de vues SQL optimisées

