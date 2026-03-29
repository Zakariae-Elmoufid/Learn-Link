# LearnLink Backend 🎓


## Overview

LearnLink is a comprehensive backend platform built with **Spring Boot** designed to facilitate collaborative learning and skill-building within educational communities. The system connects learners and mentors, provides task planning and gamification features, and enables real-time communication through messaging and notifications.

---

## 📋 Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Architecture](#project-architecture)
3. [Core Modules](#core-modules)
4. [Getting Started](#getting-started)
5. [Installation & Setup](#installation--setup)
6. [Configuration](#configuration)
7. [Database & Migrations](#database--migrations)
8. [API Documentation](#api-documentation)
9. [Development](#development)
10. [Docker Deployment](#docker-deployment)
11. [Testing](#testing)

---

## 🛠 Technology Stack

### Backend Framework
- **Spring Boot 4.0.2** - Web framework and dependency injection
- **Java 17** - Programming language
- **Maven** - Build tool and dependency management

### Database & Caching
- **PostgreSQL 17** - Relational database
- **Redis 7** - In-memory caching and session management
- **Flyway** - Database migration tool

### External Services
- **AWS S3** - File storage and media management
- **Gmail SMTP** - Email service
- **JWT** - Authentication and authorization

### Additional Libraries
- **Spring Data JPA** - Object-relational mapping
- **Spring Validation** - Input validation
- **Spring Security** - Security and authorization

---

## 🏗 Project Architecture

### Module-Based Architecture

The application follows a modular design pattern where each feature domain is organized into independent modules:

```
src/main/java/org/example/learnlink/modules/
├── admin/           # Administrative features and management
├── auth/            # Authentication, JWT, and security
├── community/       # Community features and discussions
├── email/           # Email service integration
├── gamification/    # Badges, points, and rewards system
├── matching/        # User connection and matching algorithms
├── media/           # File upload and management
├── messaging/       # Direct messaging between users
├── notification/    # Push notifications and alerts
├── planner/         # Task and schedule planning
└── user/            # User profiles and account management
```

### Key Design Patterns
- **Service-oriented architecture**: Each module contains Service, Repository, Controller, and Entity layers
- **DTO pattern**: Data Transfer Objects for request/response handling
- **JWT-based authentication**: Stateless security implementation
- **Event-driven notifications**: Real-time user updates

---

## 📦 Core Modules

### 🔐 Auth Module
Handles user authentication, JWT token management, and security configuration.
- User login/registration
- JWT token generation and validation
- Custom user details service
- Security configurations

**API Base Path**: `/api/auth`

### 👤 User Module
Manages user profiles, preferences, and account settings.
- Profile management
- Subject preferences
- Account settings
- User search and discovery

**API Base Path**: `/api/users`

### 📅 Planner Module
Task and schedule management for students and learners.
- Create/edit/delete tasks
- Task prioritization (LOW, MEDIUM, HIGH)
- Task status tracking (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
- Date range filtering
- Overdue task detection

**API Base Path**: `/api/planner/tasks`

### 🤝 Matching Module
Intelligent matching system to connect learners with tutors.
- Compatibility algorithms
- Connection management
- Request handling
- Match recommendations

**API Base Path**: `/api/matching`

### 💬 Community Module
Social features for group discussions and knowledge sharing.
- Create/delete posts
- Comments and discussions
- Community engagement
- Content moderation

**API Base Path**: `/api/community`

### 📧 Messaging Module
Direct messaging between users.
- Send/receive messages
- Conversation management
- Message history
- Real-time updates via WebSocket or polling

**API Base Path**: `/api/messages`

### 🔔 Notification Module
Push notifications and real-time alerts.
- User notifications
- Event-triggered alerts
- Web push support
- Notification preferences

**API Base Path**: `/api/notifications`

### 🏆 Gamification Module
Engagement and motivation through badges and rewards.
- Badge system
- Point accumulation
- Leaderboards
- Achievement tracking

**API Base Path**: `/api/gamification`

### 📁 Media Module
File upload and storage management.
- Upload files to AWS S3
- Generate signed URLs
- File type validation
- Size limits (10MB max)

**API Base Path**: `/api/media`

### 📧 Email Module
Email notifications and communications.
- Welcome emails
- Password reset emails
- Community notifications
- GMAIL SMTP integration

### 🛡️ Admin Module
Administrative functions and management.
- User management
- System monitoring
- Content moderation
- Analytics

**API Base Path**: `/api/admin`

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+
- Docker & Docker Compose (optional)

### Quick Start

#### Option 1: Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd learn-link-backend
   ```

2. **Set environment variables**
   ```bash
   # Create .env file in project root
   DB_URL=jdbc:postgresql://localhost:5432/learnlink
   DB_USER=postgres
   DB_PASSWORD=your_password
   JWT_SECRET=your_jwt_secret_key
   MAIL_USERNAME=your_gmail@gmail.com
   MAIL_PASSWORD=your_gmail_app_password
   AWS_S3_BUCKET_NAME=your_bucket_name
   AWS_REGION=your_region
   AWS_ACCESS_KEY_ID=your_access_key
   AWS_SECRET_ACCESS_KEY=your_secret_key
   ```

3. **Build the project**
   ```bash
   ./mvnw clean install
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the application**
    - API: `http://localhost:8081`
    - PostgreSQL: `localhost:5432`
    - Redis: `localhost:6379`

#### Option 2: Using Docker Compose

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Docker Services
- **PostgreSQL**: `localhost:5432` (port 5432)
- **PgAdmin**: `http://localhost:9090` (management interface)
- **Redis**: `localhost:6379`
- **Application**: `http://localhost:8081`

---

## ⚙️ Configuration

### Application Properties
All configuration is managed through `application.properties`:

```properties
# Server Configuration
server.port=8081
server.address=0.0.0.0
app.url=http://localhost:8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/learnlink
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT (Authentication)
jwt.secret=your_secret_key
jwt.expiration=86400000          # 24 hours
jwt.refresh-expiration=604800000 # 7 days

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# AWS S3
aws.s3.bucket=${AWS_S3_BUCKET_NAME}
aws.region=${AWS_REGION}
aws.access-key=${AWS_ACCESS_KEY_ID}
aws.secret-key=${AWS_SECRET_ACCESS_KEY}

# Redis
spring.data.redis.host=redis
spring.data.redis.port=6379

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Environment Variables
Create a `.env` file in the project root with necessary credentials and configurations.

---

## 🗄️ Database & Migrations

### Database Schema
LearnLink uses PostgreSQL with **Flyway** for schema versioning and migrations.

### Migration Files
Migrations are located in `src/main/resources/db/migration/`:

- `V1_0_0__Create_Community_Module.sql` - Community tables
- `V1_1_0__Create_Gamification_Badges.sql` - Gamification system
- `V1_2_0__Create_Messages_Table.sql` - Messaging system
- `V1_3_0__Create_Group_Messages_Table.sql` - Group messaging

### Creating New Migrations
1. Create a file with naming convention: `V<version>__<description>.sql`
2. Place in `src/main/resources/db/migration/`
3. Run migrations automatically on application startup

### Database Management
- **PgAdmin**: Access at `http://localhost:9090`
    - Email: `admin@admin.com`
    - Password: `admin`

---

## 📚 API Documentation

### Authentication
Most endpoints require JWT token in the `Authorization` header:
```http
Authorization: Bearer <your_jwt_token>
```

### Core Endpoints

#### Authentication
```
POST   /api/auth/login       - User login
POST   /api/auth/register    - User registration
POST   /api/auth/refresh     - Refresh JWT token
```

#### Planner/Tasks
```
POST   /api/planner/tasks           - Create task
GET    /api/planner/tasks/{taskId}  - Get task by ID
GET    /api/planner/tasks           - Get all user tasks
GET    /api/planner/tasks/today     - Get today's tasks
GET    /api/planner/tasks/active    - Get active tasks
GET    /api/planner/tasks/overdue   - Get overdue tasks
PUT    /api/planner/tasks/{taskId}  - Update task
DELETE /api/planner/tasks/{taskId}  - Delete task
```

#### Task Structure
```json
{
  "id": 1,
  "userId": 10,
  "title": "Study algebra",
  "description": "Chapter 3 exercises",
  "startTime": "2026-03-13T09:00:00",
  "endTime": "2026-03-13T10:30:00",
  "priority": "HIGH",
  "status": "PENDING",
  "completed": false,
  "subject": "Math",
  "tags": ["algebra", "exam"],
  "isOverdue": false,
  "createdAt": "2026-03-13T08:00:00",
  "updatedAt": "2026-03-13T08:00:00"
}
```

#### User Module
```
GET    /api/users/{userId}         - Get user profile
PUT    /api/users/{userId}         - Update user profile
GET    /api/users/search?q=term    - Search users
```

#### Community Module
```
POST   /api/community/posts        - Create post
GET    /api/community/posts        - Get all posts
GET    /api/community/posts/{id}   - Get post by ID
PUT    /api/community/posts/{id}   - Update post
DELETE /api/community/posts/{id}   - Delete post
```

#### Messaging Module
```
POST   /api/messages               - Send message
GET    /api/messages/{userId}      - Get conversation with user
GET    /api/messages               - Get all conversations
```

#### Media Module
```
POST   /api/media/upload           - Upload file to S3
GET    /api/media/{fileId}         - Get file details
DELETE /api/media/{fileId}         - Delete file
```

---

## 💻 Development

### Project Structure
```
├── src/
│   ├── main/
│   │   ├── java/org/example/learnlink/
│   │   │   ├── modules/          # Feature modules
│   │   │   ├── config/           # Spring configurations
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── security/         # JWT & security logic
│   │   │   └── LearnLinkApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/    # Database migrations
│   └── test/                     # Unit and integration tests
├── pom.xml                       # Maven configuration
├── Dockerfile                    # Docker image config
├── docker-compose.yml            # Multi-container setup
└── README.md
```

### Code Conventions
- **Naming**: Use meaningful names for classes, methods, and variables
- **Formatting**: Follow Google Java Style Guide
- **Modules**: Keep concerns separated by module
- **DTOs**: Use for request/response objects
- **Services**: Implement business logic in service layer
- **Repositories**: Use Spring Data JPA for database access

### Building
```bash
# Full build with tests
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests

# Run specific tests
./mvnw test -Dtest=ClassName
```

### Running Tests
```bash
# Run all tests
./mvnw test

# Run tests with coverage report
./mvnw test jacoco:report

# Integration tests
./mvnw verify
```

---

## 🐳 Docker Deployment

### Build Docker Image
```bash
# Build the JAR
./mvnw clean package

# Build Docker image
docker build -t learnlink:latest .
```

### Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Remove all volumes
docker-compose down -v
```

### Services in docker-compose.yml
1. **PostgreSQL** (port 5432)
2. **PgAdmin** (port 9090)
3. **Redis** (port 6379)
4. **LearnLink Application** (port 8081)

---

## 🧪 Testing

### Test Structure
- Location: `src/test/java/org/example/learnlink/`
- Test database config: `src/test/resources/application-test.yml`

### Test Reports
```
target/surefire-reports/
├── TEST-AuthServiceTest.xml
├── TEST-CustomUserDetailsServiceTest.xml
├── TEST-JwtServiceTest.xml
├── TEST-MatchingServiceImplTest.xml
├── TEST-ConnectionServiceImplTest.xml
├── TEST-ProfileServiceIntegrationTest.xml
└── TEST-StudentSubjectServiceIntegrationTest.xml
```

### Running Tests
```bash
# All tests
./mvnw test

# Specific test
./mvnw test -Dtest=AuthServiceTest

# With coverage
./mvnw test jacoco:report
```

---

## 📝 Logging & Monitoring

### Log Levels
Configure in `application.properties`:
```properties
logging.level.org.springframework=INFO
logging.level.org.example.learnlink=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Access Logs
Application logs are streamed to console and can be redirected to file using Docker volume mounts.

---

## 🔒 Security

### JWT Authentication
- Tokens expire after 24 hours (configurable)
- Refresh tokens valid for 7 days
- Secure claims: userId, username, authorities

### CORS Configuration
Configured to allow trusted origins. Update in security configuration as needed.

### Password Security
- Passwords encoded using Spring Security's BCryptPasswordEncoder
- Minimum requirements enforceable through validation

---

## 🐛 Troubleshooting

### Common Issues

**Database Connection Error**
- Verify PostgreSQL is running
- Check DATABASE_URL environment variable
- Ensure credentials are correct

**Redis Connection Error**
- Verify Redis is running (port 6379)
- Check Redis configuration in application.properties

**JWT Token Validation Fails**
- Ensure JWT_SECRET matches between requests
- Check token expiration time
- Verify Authorization header format

**File Upload Fails**
- Verify AWS S3 credentials and region
- Check IAM permissions for S3 bucket
- Ensure file size is under 10MB limit

---

## 📞 Support & Contributing

For issues, questions, or contributions:
1. Check existing issues and documentation
2. Review code in relevant module
3. Follow Java and Spring Boot best practices
4. Write tests for new features
5. Submit pull requests with clear descriptions

---

---
