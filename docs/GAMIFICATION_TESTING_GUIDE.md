# Guide de Test - Module Gamification

## 🚀 Préparation

### 1. Compiler le projet
```bash
cd C:\Users\Youcode\Desktop\LearnLink
mvn clean compile
```

### 2. Démarrer l'application
```bash
mvn spring-boot:run
```

L'application démarrera sur `http://localhost:8080`

## 🧪 Tests manuels avec cURL

### Prérequis
```bash
# On suppose que vous avez créé un utilisateur avec userId=1
# Assurez-vous que user_scores a une entrée pour userId=1
```

### 1️⃣ Vérifier le score initial
```bash
curl http://localhost:8080/api/gamification/score/1
```

**Réponse attendue:**
```json
{
  "userId": 1,
  "totalPoints": 0,
  "level": 1,
  "currentLevelPoints": 0,
  "pointsForNextLevel": 100,
  "progressPercentage": 0.0
}
```

---

### 2️⃣ Ajouter des points
```bash
curl -X POST http://localhost:8080/api/gamification/points \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "points": 50,
    "actionType": "POST_CREATED"
  }'
```

**Réponse attendue:**
```json
{
  "userId": 1,
  "totalPoints": 50,
  "level": 1,
  "currentLevelPoints": 50,
  "pointsForNextLevel": 100,
  "progressPercentage": 50.0
}
```

---

### 3️⃣ Ajouter plus de points (montée de niveau)
```bash
curl -X POST http://localhost:8080/api/gamification/points \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "points": 60,
    "actionType": "ANSWER_ACCEPTED"
  }'
```

**Réponse attendue:**
```json
{
  "userId": 1,
  "totalPoints": 110,
  "level": 2,           // ← Niveau augmenté!
  "currentLevelPoints": 10,
  "pointsForNextLevel": 150,
  "progressPercentage": 6.67
}
```

---

### 4️⃣ Créer un badge
```bash
curl -X POST http://localhost:8080/api/gamification/badges \
  -H "Content-Type: application/json" \
  -d '{
    "code": "TEST_BADGE",
    "name": "Test Badge",
    "description": "Un badge de test",
    "iconUrl": "https://example.com/test.png",
    "type": "ACHIEVEMENT",
    "rarity": "RARE",
    "pointsRequired": 100
  }'
```

**Réponse attendue:**
```json
{
  "id": 9,
  "code": "TEST_BADGE",
  "name": "Test Badge",
  "description": "Un badge de test",
  "iconUrl": "https://example.com/test.png",
  "type": "ACHIEVEMENT",
  "rarity": "RARE",
  "pointsRequired": 100,
  "active": true,
  "createdAt": "2026-02-16T10:30:45.123456Z"
}
```

---

### 5️⃣ Récupérer tous les badges
```bash
curl http://localhost:8080/api/gamification/badges
```

**Réponse attendue:** Array de 9 badges (8 pré-configurés + TEST_BADGE)

---

### 6️⃣ Attribuer un badge à un utilisateur
```bash
# Supposons que FIRST_POST badge a l'ID 1
curl -X POST http://localhost:8080/api/gamification/user-badges/1/award/1
```

**Réponse attendue:**
```
HTTP 201 Created
```

---

### 7️⃣ Récupérer les badges d'un utilisateur
```bash
curl http://localhost:8080/api/gamification/user-badges/1
```

**Réponse attendue:**
```json
[
  {
    "badgeId": 1,
    "code": "FIRST_POST",
    "name": "First Step",
    "iconUrl": "https://icon.example.com/first_post.png",
    "rarity": "COMMON",
    "earnedAt": "2026-02-16T10:35:20.123456Z"
  }
]
```

---

### 8️⃣ Compter les badges d'un utilisateur
```bash
curl http://localhost:8080/api/gamification/user-badges/1/count
```

**Réponse attendue:**
```
1
```

---

### 9️⃣ Vérifier si un utilisateur a un badge
```bash
curl http://localhost:8080/api/gamification/user-badges/1/has/1
```

**Réponse attendue:**
```
true
```

---

### 🔟 Récupérer le profil public
```bash
curl http://localhost:8080/api/gamification/profile/1
```

**Réponse attendue:**
```json
{
  "userId": 1,
  "level": 2,
  "totalPoints": 110,
  "rank": 1,
  "badgeCount": 1,
  "badges": [
    {
      "badgeId": 1,
      "code": "FIRST_POST",
      "name": "First Step",
      "iconUrl": "https://icon.example.com/first_post.png",
      "rarity": "COMMON",
      "earnedAt": "2026-02-16T10:35:20.123456Z"
    }
  ]
}
```

---

### 1️⃣1️⃣ Leaderboard Global
```bash
curl "http://localhost:8080/api/gamification/leaderboard/global?limit=10"
```

**Réponse attendue:**
```json
[
  {
    "userId": 1,
    "username": "john_doe",
    "level": 2,
    "totalPoints": 110,
    "rank": 1,
    "badgeCount": 1
  }
]
```

---

### 1️⃣2️⃣ Leaderboard Hebdomadaire
```bash
curl "http://localhost:8080/api/gamification/leaderboard/weekly?limit=10"
```

---

### 1️⃣3️⃣ Rang d'un utilisateur
```bash
curl http://localhost:8080/api/gamification/leaderboard/rank/1
```

**Réponse attendue:**
```
1
```

---

### 1️⃣4️⃣ Pourcentage de rang
```bash
curl http://localhost:8080/api/gamification/leaderboard/rank-percentage/1
```

**Réponse attendue:**
```
100
```

---

## 📋 Checklist de Test Complet

### Tests Gamification de base
- [ ] GET /api/gamification/score/{userId}
- [ ] POST /api/gamification/points
- [ ] Vérifier que level augmente après 100 points
- [ ] Vérifier que currentLevelPoints réinitialise

### Tests Badge
- [ ] GET /api/gamification/badges (voir 8 badges pré-configurés)
- [ ] GET /api/gamification/badges/{id}
- [ ] GET /api/gamification/badges/code/{code}
- [ ] GET /api/gamification/badges/active
- [ ] POST /api/gamification/badges (créer nouveau)
- [ ] PUT /api/gamification/badges/{id} (modifier)
- [ ] DELETE /api/gamification/badges/{id} (désactiver)

### Tests User Badges
- [ ] POST /api/gamification/user-badges/{userId}/award/{badgeId}
- [ ] GET /api/gamification/user-badges/{userId}
- [ ] GET /api/gamification/user-badges/{userId}/count
- [ ] GET /api/gamification/user-badges/{userId}/has/{badgeId}

### Tests Leaderboard
- [ ] GET /api/gamification/leaderboard/global
- [ ] GET /api/gamification/leaderboard/weekly
- [ ] GET /api/gamification/leaderboard/rank/{userId}
- [ ] GET /api/gamification/leaderboard/rank-percentage/{userId}

### Tests Profil
- [ ] GET /api/gamification/profile/{userId}
- [ ] Vérifier que badges sont inclus
- [ ] Vérifier que rank est correct

### Tests Edge Cases
- [ ] Ajouter 0 points (ne devrait pas échouer)
- [ ] Attribuer le même badge deux fois (ne devrait pas créer de duplicate)
- [ ] Récupérer badges pour utilisateur sans badges (liste vide)
- [ ] Leaderboard avec utilisateur absent (devrait retourner tout le monde sauf lui)

## 🐛 Dépannage

### Erreur: Badge not found
```
Exception: BadgeNotFoundException("Badge not found with id: X")
```
**Solution:** Vérifier que l'ID du badge existe dans la base de données

### Erreur: User score not found
```
Exception: UserScoreNotFoundException("User score not found")
```
**Solution:** S'assurer que l'utilisateur existe dans user_scores

### Erreur: Duplicate key value violates unique constraint
```
ERROR: duplicate key value violates unique constraint "user_badges_user_id_badge_id_key"
```
**Solution:** L'utilisateur a déjà ce badge. Vérifier avec GET /has/{badgeId}

## 📊 Inspection de la Base de Données

### Vérifier les tables créées
```sql
-- Pour PostgreSQL
\dt badges
\dt user_badges

-- Voir les données
SELECT * FROM badges;
SELECT * FROM user_badges;
SELECT * FROM user_scores WHERE user_id = 1;
```

### Vérifier les indexes
```sql
SELECT * FROM pg_indexes WHERE tablename IN ('badges', 'user_badges');
```

## 🎯 Scénario de Test Complet

1. **Création utilisateur** → User avec ID 1 existe
2. **Vérification score initial** → Points = 0, Level = 1
3. **Ajouter 50 points** → Progress = 50%
4. **Ajouter 60 points** → Level = 2, Progress réinitié
5. **Attribuer badge** → FIRST_POST
6. **Vérifier badges** → 1 badge
7. **Vérifier profil public** → Montre level, points, badges, rank
8. **Vérifier leaderboard** → Utilisateur 1 est #1
9. **Ajouter deuxième utilisateur** → Points plus bas
10. **Vérifier leaderboard à nouveau** → Utilisateur 1 toujours #1

## 📝 Notes

- **Async:** Les leaderboards peuvent être cachés (à ajouter plus tard)
- **Performance:** Avec peu d'utilisateurs, les requêtes doivent être instantanées
- **Données:** Les badges pré-configurés sont créés via Flyway migration

## ✅ Succès de Test

Tout fonctionne si:
- ✅ Aucune exception lors des créations
- ✅ Les scores sont correctement calculés
- ✅ Les levels augmentent correctement
- ✅ Les badges sont attribués sans duplicatas
- ✅ Le leaderboard affiche les utilisateurs triés

---

**Bon testing! 🚀**

