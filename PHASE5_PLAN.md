# 📬 PHASE 5 - NOTIFICATIONS & EVENTS SYSTEM

## 🎯 Vue d'Ensemble

**Phase**: 5/7  
**Titre**: Notifications & Events System  
**Statut**: 🚀 EN COURS  
**Date de Démarrage**: 13 Février 2026

---

## 📋 Objectifs de la Phase 5

### Fonctionnalités Principales
1. ✅ Système d'événements déclenché par les actions utilisateur
2. ✅ Notifications in-app (stockées en base de données)
3. ✅ Notifications email (via SMTP)
4. ✅ Service de notification asynchrone
5. ✅ Préférences de notifications utilisateur
6. ✅ Centre de notifications avec UI backend

---

## 🏗️ Architecture Phase 5

### Entités à Créer
```
Event (Entité événement)
├─ EventType (Enum)
└─ EventStatus (Enum)

Notification (Entité notification)
├─ NotificationType (Enum)
├─ NotificationStatus (Enum)
└─ NotificationPriority (Enum)

UserNotificationPreference (Entité préférences)
└─ NotificationChannel (Enum: IN_APP, EMAIL, PUSH)
```

### Services à Créer
```
EventPublisher (Service d'événements)
├─ publishEvent()
├─ subscribeTo()
└─ handleEvent()

NotificationService (Service notifications)
├─ sendInAppNotification()
├─ sendEmailNotification()
├─ sendPushNotification()
└─ getNotifications()

EmailService (Service email)
├─ sendEmail()
├─ sendEmailTemplate()
└─ verifyEmail()

NotificationPreferenceService
├─ updatePreferences()
└─ getPreferences()
```

### Controllers à Créer
```
NotificationController (REST API)
├─ GET /notifications
├─ GET /notifications/{id}
├─ PUT /notifications/{id}/read
├─ DELETE /notifications/{id}
└─ POST /notifications/mark-all-read

NotificationPreferenceController
├─ GET /preferences
├─ PUT /preferences
└─ GET /preferences/{channel}

EventController (Admin)
├─ GET /events
├─ GET /events/{id}
└─ POST /events (test)
```

---

## 📊 Types d'Événements & Notifications

### Événements Phase 4 (Gamification)
```
CHALLENGE_CREATED
├─ Notification: Nouveau challenge disponible
└─ Destinataire: Tous les utilisateurs

CHALLENGE_STARTED
├─ Notification: Vous avez démarré un challenge
└─ Destinataire: Utilisateur

CHALLENGE_PROGRESS_UPDATED
├─ Notification: Progression du challenge
└─ Destinataire: Utilisateur

CHALLENGE_COMPLETED
├─ Notification: 🎉 Challenge complété!
├─ Points gagnés: XX points
└─ Destinataire: Utilisateur

ACHIEVEMENT_UNLOCKED
├─ Notification: Nouvel achievement débloqué!
└─ Destinataire: Utilisateur

LEADERBOARD_RANK_CHANGED
├─ Notification: Vous êtes maintenant #X du leaderboard
└─ Destinataire: Utilisateur (si changement significatif)
```

### Autres Événements
```
USER_REGISTERED
├─ Notification: Bienvenue sur LearnLink
└─ Email de bienvenue

USER_PROFILE_UPDATED
├─ Notification: Profil mis à jour avec succès
└─ In-app seulement

CONNECTION_REQUEST_RECEIVED
├─ Notification: Nouvelle demande de connexion
└─ Destinataire: Utilisateur

CONNECTION_ACCEPTED
├─ Notification: Demande de connexion acceptée
└─ Destinataire: Demandeur

STUDY_SESSION_CREATED
├─ Notification: Nouvelle session d'étude créée
└─ Destinataire: Utilisateurs intéressés
```

---

## 🗄️ Schéma Base de Données Phase 5

### Table: events
```sql
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    event_status VARCHAR(20),
    source_user_id UUID,
    target_user_id UUID,
    related_entity_type VARCHAR(50),
    related_entity_id VARCHAR(100),
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);
```

### Table: notifications
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    event_id BIGINT FK,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    content TEXT,
    channel VARCHAR(20), -- IN_APP, EMAIL, PUSH
    priority VARCHAR(10), -- LOW, MEDIUM, HIGH, URGENT
    status VARCHAR(20), -- PENDING, SENT, DELIVERED, READ, FAILED
    metadata JSONB,
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
```

### Table: user_notification_preferences
```sql
CREATE TABLE user_notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    email_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT FALSE,
    challenge_notifications BOOLEAN DEFAULT TRUE,
    achievement_notifications BOOLEAN DEFAULT TRUE,
    connection_notifications BOOLEAN DEFAULT TRUE,
    email_digest_frequency VARCHAR(20), -- IMMEDIATE, DAILY, WEEKLY, NEVER
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 📧 Configuration Email

### Fichier: application.properties
```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Notification Configuration
learnlink.notification.from-email=noreply@learnlink.com
learnlink.notification.from-name=LearnLink Platform
learnlink.notification.logo-url=https://learnlink.com/logo.png
learnlink.notification.support-email=support@learnlink.com
```

---

## 🔧 Fichiers à Créer (Phase 5)

### Entités (3 fichiers)
- [ ] Event.java
- [ ] Notification.java
- [ ] UserNotificationPreference.java

### Enums (4 fichiers)
- [ ] EventType.java
- [ ] EventStatus.java
- [ ] NotificationType.java
- [ ] NotificationStatus.java

### Repositories (3 fichiers)
- [ ] EventRepository.java
- [ ] NotificationRepository.java
- [ ] UserNotificationPreferenceRepository.java

### DTOs (8 fichiers)
- [ ] EventResponse.java
- [ ] NotificationResponse.java
- [ ] UserNotificationPreferenceResponse.java
- [ ] CreateNotificationRequest.java
- [ ] UpdateNotificationPreferenceRequest.java
- [ ] NotificationStatisticsResponse.java
- [ ] EventStatisticsResponse.java
- [ ] EmailNotificationRequest.java

### Services (5 fichiers)
- [ ] EventPublisher.java
- [ ] NotificationService.java
- [ ] EmailService.java
- [ ] NotificationPreferenceService.java
- [ ] NotificationScheduler.java

### Controllers (3 fichiers)
- [ ] NotificationController.java
- [ ] NotificationPreferenceController.java
- [ ] EventController.java (Admin)

### Listeners (2 fichiers)
- [ ] GamificationEventListener.java (écoute les events de Phase 4)
- [ ] UserEventListener.java (écoute les events utilisateur)

### Templates Email (5 fichiers)
- [ ] WelcomeEmail.html
- [ ] ChallengeCompleted.html
- [ ] AchievementUnlocked.html
- [ ] ConnectionRequest.html
- [ ] NotificationDigest.html

### Database Migration (1 fichier)
- [ ] V1_1_4__Create_Notification_System.sql

---

## 📡 Architecture du Flux d'Événements

```
Action Utilisateur
    ↓
Controller/Service émet un événement
    ↓
ApplicationEventPublisher.publishEvent()
    ↓
EventListener reçoit l'événement
    ↓
NotificationService.createNotifications()
    ↓
Crée des Notification records
    ↓
Envoie via les canaux configurés:
    ├─ In-App (BD + WebSocket)
    ├─ Email (SMTP)
    └─ Push (FCM - optionnel)
    ↓
Utilisateur reçoit notification
```

---

## 🎯 28 Endpoints Phase 5

### Notification Endpoints (7)
```
GET    /api/v1/notifications
GET    /api/v1/notifications/{id}
GET    /api/v1/notifications/stats
PUT    /api/v1/notifications/{id}/read
PUT    /api/v1/notifications/mark-all-read
DELETE /api/v1/notifications/{id}
DELETE /api/v1/notifications/clear-all
```

### Notification Preference Endpoints (6)
```
GET    /api/v1/notification-preferences
PUT    /api/v1/notification-preferences
GET    /api/v1/notification-preferences/{channel}
PUT    /api/v1/notification-preferences/{channel}
POST   /api/v1/notification-preferences/send-test
GET    /api/v1/notification-preferences/digest-history
```

### Event Endpoints (Admin only) (6)
```
GET    /api/v1/admin/events
GET    /api/v1/admin/events/{id}
GET    /api/v1/admin/events/stats
POST   /api/v1/admin/events/test/{eventType}
POST   /api/v1/admin/events/trigger-digest
GET    /api/v1/admin/events/replay/{eventId}
```

### Email Testing Endpoints (2)
```
POST   /api/v1/admin/email/test-welcome
POST   /api/v1/admin/email/test-challenge-completed
```

### WebSocket Endpoints (3)
```
SUBSCRIBE /user/notifications
SUBSCRIBE /user/notifications/{userId}
SEND      /app/notifications/mark-read
```

---

## 📊 Calendrier Phase 5

```
Jour 1: Architecture & Planning
├─ Créer entités
├─ Créer repositories
└─ Créer DTOs

Jour 2: Services & Events
├─ Créer EventPublisher
├─ Créer NotificationService
├─ Créer EmailService
└─ Implémenter listeners

Jour 3: Controllers & API
├─ Créer NotificationController
├─ Créer PreferenceController
├─ Implémenter WebSocket
└─ Tester tous les endpoints

Jour 4: Email Templates & Scheduler
├─ Créer templates HTML
├─ Configurer SMTP
├─ Implémenter scheduler
└─ Tester envoi emails

Jour 5: Tests & Documentation
├─ Tests unitaires
├─ Tests d'intégration
├─ Documentation complète
└─ Vérification finale
```

---

## ✅ Checklist Phase 5

**Entités**
- [ ] Event entity created
- [ ] Notification entity created
- [ ] UserNotificationPreference entity created

**Services**
- [ ] EventPublisher implemented
- [ ] NotificationService implemented
- [ ] EmailService implemented
- [ ] NotificationPreferenceService implemented

**Controllers**
- [ ] NotificationController (7 endpoints)
- [ ] NotificationPreferenceController (6 endpoints)
- [ ] EventController (6 admin endpoints)

**WebSocket**
- [ ] WebSocket configuration
- [ ] Real-time notifications
- [ ] User subscriptions

**Email**
- [ ] Email templates created
- [ ] SMTP configured
- [ ] Email service working

**Events Listeners**
- [ ] GamificationEventListener
- [ ] UserEventListener

**Database**
- [ ] Migration script ready
- [ ] All tables created
- [ ] All indexes created

**Documentation**
- [ ] API documentation
- [ ] Architecture guide
- [ ] Deployment guide
- [ ] Testing guide

**Tests**
- [ ] Unit tests (20+)
- [ ] Integration tests (10+)
- [ ] API tests (28 endpoints)

---

## 🚀 Prochaines Actions

1. **Créer toutes les entités Phase 5**
2. **Implémenter EventPublisher**
3. **Créer NotificationService**
4. **Implémenter tous les controllers**
5. **Configurer email SMTP**
6. **Créer email templates**
7. **Implémenter WebSocket**
8. **Créer listeners**
9. **Tester tous les endpoints**
10. **Documenter complètement**

---

**Phase 5 est prêt à commencer! 🚀**

Voulez-vous que je commence par les entités ou une autre partie?

