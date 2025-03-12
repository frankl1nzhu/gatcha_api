# Gatcha API

A complete Gatcha game system, including Spring Boot backend and React frontend, providing user authentication, monster summoning, battles, Royal Rumble, and other features.

## Contributor

Yuzhe Zhu

## Features

- User authentication and authorization (Token-based authentication system)
- Player management (level, experience, monster collection)
- Monster management (attributes, skills, upgrades)
- Summoning system (probability-based monster summoning)
- Battle system (automatic battles with detailed battle logs)
- Royal Rumble system (multi-monster battle royale, last surviving monster wins)
- Monster name generation system (generates unique names based on monster ID and element type)

## Technology Stack

### Backend

- Java 17
- Spring Boot 2.7.5
- Spring Security
- Spring Data MongoDB
- JWT authentication
- Docker & Docker Compose
- Maven
- JUnit 5 & Mockito (testing frameworks)

### Frontend

- React
- Ant Design (UI component library)
- Axios (HTTP client)
- TypeScript
- Moment.js (date handling)
- Nginx (static file serving)

## Project Structure

### Backend Structure

```
src/main/java/com/gatcha/api/
├── auth/                  # Authentication related
│   ├── controller/        # Controllers
│   ├── dto/               # Data Transfer Objects
│   ├── model/             # Entity models
│   ├── repository/        # Data access
│   └── service/           # Business logic
├── battle/                # Battle related
│   ├── controller/        # Controllers
│   ├── dto/               # Data Transfer Objects
│   ├── model/             # Entity models
│   ├── repository/        # Data access
│   └── service/           # Business logic
├── config/                # Configuration classes
├── monster/               # Monster related
│   ├── controller/        # Controllers
│   ├── dto/               # Data Transfer Objects
│   ├── model/             # Entity models
│   ├── repository/        # Data access
│   └── service/           # Business logic
├── player/                # Player related
│   ├── controller/        # Controllers
│   ├── dto/               # Data Transfer Objects
│   ├── service/           # Business logic
├── summon/                # Summoning related
│   ├── controller/        # Controllers
│   ├── model/             # Entity models
│   ├── repository/        # Data access
│   └── service/           # Business logic
└── utils/                 # Utility classes
    └── NameGenerator.java # Monster name generator
```

### Frontend Structure

```
frontend/
├── public/                # Static resources
├── src/                   # Source code
│   ├── components/        # Common components
│   ├── pages/             # Page components
│   │   ├── BattlePage.tsx         # Battle page
│   │   ├── Dashboard.tsx          # Dashboard page
│   │   ├── LoginPage.tsx          # Login page
│   │   ├── PlayerPage.tsx         # Player page
│   │   ├── RoyalRumblePage.tsx    # Royal Rumble page
│   │   └── SummoningPage.tsx      # Summoning page
│   ├── services/          # API services
│   │   └── api.ts         # API calls
│   ├── utils/             # Utility functions
│   │   └── nameGenerator.ts # Monster name generator
│   ├── App.tsx            # Application entry
│   └── index.tsx          # Rendering entry
└── package.json           # Dependency configuration
```

## Data Models

### User Model

```java
public class User {
    private String id;
    private String username;
    private String password;
    private int level;
    private int experience;
    private int maxExperience;
    private List<String> monsters;
}
```

### Monster Template Model

```java
public class MonsterTemplate {
    private Integer id;
    private String element;
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private List<Skill> skills;
    private double lootRate;
}
```

### Player Monster Model

```java
public class PlayerMonster {
    private String id;
    private String username;
    private String templateId;
    private String element;
    private int level;
    private int experience;
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private List<Skill> skills;
    private int skillPoints;
}
```

### Skill Model

```java
public class Skill {
    private int num;
    private int dmg;
    private Ratio ratio;
    private int cooldown;
    private int level;
    private int lvlMax;
  
    public static class Ratio {
        private String stat;
        private double percent;
    }
}
```

### Battle Log Model

```java
public class BattleLog {
    private String id;
    private String monster1Id;
    private String monster2Id;
    private String monster1Element;
    private String monster2Element;
    private String winnerId;
    private Date battleDate;
    private List<BattleAction> actions;

    public static class BattleAction {
        private String monsterId;
        private int skillNum;
        private int damage;
        private String targetId;
        private int remainingHp;
    }
}
```

### Royal Rumble Result Model

```java
public class RoyalRumbleResult {
    private String id;
    private List<String> participantIds;
    private PlayerMonster winner;
    private Date rumbleDate;
    private List<RumbleRound> rounds;
    private int experienceGained;
    private List<String> battleLog;

    public static class RumbleRound {
        private int roundNumber;
        private List<BattleLog.BattleAction> actions;
        private List<String> remainingMonsterIds;
    }
}
```

## API Endpoints

### Authentication API

- `POST /api/auth/login` - User login

  - Request body: `{"username": "user2", "password": "password2"}`
  - Response: `{"token": "base64_encoded_token"}`
  - Example:

    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"username": "user2", "password": "password2"}' http://localhost/api/auth/login
    ```
- `POST /api/auth/validate` - Validate token

  - Request header: `Authorization: Bearer <token>`
  - Response: Username
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/auth/validate
    ```

### Player API

- `GET /api/player/profile` - Get player profile

  - Request header: `Authorization: Bearer <token>`
  - Response: Player profile information
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/player/profile
    ```
- `GET /api/player/monsters` - Get player's monster list

  - Request header: `Authorization: Bearer <token>`
  - Response: Array of monster IDs
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/player/monsters
    ```
- `GET /api/player/level` - Get player level

  - Request header: `Authorization: Bearer <token>`
  - Response: Player level
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/player/level
    ```
- `POST /api/player/experience` - Increase player experience

  - Request header: `Authorization: Bearer <token>`
  - Request body: `{"experience": 20}`
  - Response: Updated player experience
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" -H "Content-Type: application/json" -d '{"experience": 20}' http://localhost/api/player/experience
    ```
- `POST /api/player/levelup` - Player level up

  - Request header: `Authorization: Bearer <token>`
  - Response: Updated player level
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/player/levelup
    ```
- `POST /api/player/monsters/{monsterId}` - Add monster to player's collection

  - Request header: `Authorization: Bearer <token>`
  - Response: Success message
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/player/monsters/67cf4798011d486bbc622a31
    ```
- `DELETE /api/player/monsters/{monsterId}` - Remove monster from player's collection

  - Request header: `Authorization: Bearer <token>`
  - Response: Success message
  - Example:
    ```bash
    curl -X DELETE -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/player/monsters/67cf4798011d486bbc622a31
    ```

### Monster API

- `GET /api/monsters` - Get all player's monsters

  - Request header: `Authorization: Bearer <token>`
  - Response: Array of monster objects
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/monsters
    ```
- `GET /api/monsters/{id}` - Get specific monster details

  - Request header: `Authorization: Bearer <token>`
  - Response: Monster details
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/monsters/67cf2fe98eb1b816c9ee079d
    ```
- `POST /api/monsters/{id}/experience` - Add experience to monster

  - Request header: `Authorization: Bearer <token>`
  - Request body: `{"experience": 50}`
  - Response: Updated monster with new experience
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" -H "Content-Type: application/json" -d '{"experience": 50}' http://localhost/api/monsters/67cf2fe98eb1b816c9ee079d/experience
    ```
- `POST /api/monsters/{id}/skill` - Upgrade monster skill

  - Request header: `Authorization: Bearer <token>`
  - Request body: `{"skillNum": 1}`
  - Response: Updated monster with upgraded skill
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" -H "Content-Type: application/json" -d '{"skillNum": 1}' http://localhost/api/monsters/67cf2fe98eb1b816c9ee079d/skill
    ```

### Summoning API

- `POST /api/summon` - Summon new monster

  - Request header: `Authorization: Bearer <token>`
  - Response: Summoned monster details
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/summon
    ```
  - Example Response:
    ```json
    {
      "id": "67cf4798011d486bbc622a31",
      "username": "user2",
      "templateId": "2",
      "element": "wind",
      "level": 1,
      "experience": 0,
      "hp": 1500,
      "atk": 200,
      "def": 450,
      "vit": 80,
      "skills": [
        {
          "num": 1,
          "dmg": 200,
          "ratio": {
            "stat": "def",
            "percent": 10.0
          },
          "cooldown": 0,
          "level": 0,
          "lvlMax": 4
        },
        ...
      ],
      "skillPoints": 3
    }
    ```
- `GET /api/summon/history` - Get summoning history

  - Request header: `Authorization: Bearer <token>`
  - Response: Array of summon log objects
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/summon/history
    ```
- `POST /api/summon/reprocess` - Reprocess failed summons

  - Request header: `Authorization: Bearer <token>`
  - Response: Number of reprocessed summons
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/summon/reprocess
    ```

### Battle API

- `POST /api/battles` - Conduct battle

  - Request header: `Authorization: Bearer <token>`
  - Request body: `{"monster1Id": "id1", "monster2Id": "id2"}`
  - Response: Contains battle log, winning monster info, and experience gained
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" -H "Content-Type: application/json" -d '{"monster1Id": "67cf26fdc194747fad2518df", "monster2Id": "67cf4798011d486bbc622a31"}' http://localhost/api/battles
    ```
  - Example Response:
    ```json
    {
      "id": "67cf47a8011d486bbc622a32",
      "monster1Id": "67cf26fdc194747fad2518df",
      "monster2Id": "67cf4798011d486bbc622a31",
      "monster1Element": "water",
      "monster2Element": "wind",
      "winnerId": "67cf4798011d486bbc622a31",
      "battleDate": "2025-03-10T20:12:24.418+00:00",
      "actions": [
        {
          "monsterId": "67cf26fdc194747fad2518df",
          "skillNum": 1,
          "damage": 137,
          "targetId": "67cf4798011d486bbc622a31",
          "remainingHp": 1363
        },
        ...
      ],
      "winner": {
        "id": "67cf4798011d486bbc622a31",
        "username": "user2",
        "templateId": "2",
        "element": "wind",
        "level": 1,
        "experience": 40,
        "hp": 1500,
        "atk": 200,
        "def": 450,
        "vit": 80,
        "skills": [...]
      }
    }
    ```
- `GET /api/battles/{battleId}` - Get battle details

  - Request header: `Authorization: Bearer <token>`
  - Response: Battle log details
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/battles/67cf47a8011d486bbc622a32
    ```
- `GET /api/battles/monster/{monsterId}` - Get monster's battle history

  - Request header: `Authorization: Bearer <token>`
  - Response: Array of battle logs involving the monster
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/battles/monster/67cf26fdc194747fad2518df
    ```
- `GET /api/battles/history` - Get all battle history

  - Request header: `Authorization: Bearer <token>`
  - Response: Array of all battle logs
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/battles/history
    ```

### Royal Rumble API

- `POST /api/royal-rumble` - Start Royal Rumble

  - Request header: `Authorization: Bearer <token>`
  - Request body: `{"monsterIds": ["id1", "id2", "id3", ...]}`
  - Response: Contains round battle records, winning monster info, and experience gained
  - Example:
    ```bash
    curl -X POST -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" -H "Content-Type: application/json" -d '{"monsterIds": ["67cf2752c194747fad2518e5", "67cf2fe98eb1b816c9ee079d", "67cf2fe98eb1b816c9ee079f"]}' http://localhost/api/royal-rumble
    ```
  - Example Response:
    ```json
    {
      "id": "41da43b8-0cee-4675-aab5-5fee750ed26a",
      "participantIds": ["67cf2752c194747fad2518e5", "67cf2fe98eb1b816c9ee079d", "67cf2fe98eb1b816c9ee079f"],
      "winner": {
        "id": "67cf2fe98eb1b816c9ee079d",
        "username": "user2",
        "templateId": "2",
        "element": "wind",
        "level": 1,
        "experience": 0,
        "hp": 1500,
        "atk": 200,
        "def": 450,
        "vit": 80,
        "skills": [...]
      },
      "rumbleDate": "2025-03-10T20:14:36.664+00:00",
      "rounds": [...],
      "experienceGained": 80,
      "battleLog": [...]
    }
    ```
- `GET /api/royal-rumble/experience/{rumbleId}` - Get experience gained from specific Royal Rumble

  - Request header: `Authorization: Bearer <token>`
  - Response: Experience points gained
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/royal-rumble/experience/41da43b8-0cee-4675-aab5-5fee750ed26a
    ```
  - Example Response: `80`
- `GET /api/royal-rumble` - Get all Royal Rumble history

  - Request header: `Authorization: Bearer <token>`
  - Response: Array of Royal Rumble results
  - Example:
    ```bash
    curl -X GET -H "Authorization: Bearer dXNlcjEtMjAyNS8wMy8xMC0yMDoxMDoyMA==" http://localhost/api/royal-rumble
    ```

## How to Run

### Prerequisites

- Docker
- Docker Compose

### Startup Steps

1. Clone the repository:

```bash
git clone https://github.com/frankl1nzhu/gatcha_api.git
cd gatcha_api
```

2. Build backend target

   ```bash
   mvn clean install
   ```
3. Build frontend

   ```bash
   cd frontend
   npm install
   npm run build
   ```
4. Start the application using Docker Compose:

```bash
cd ..
docker-compose up -d
```

3. The application will run at:
   - Frontend: `http://localhost`
   - Backend API: `http://localhost/api`

### Initial Users

The system comes with a test user:

- Username: `user2`, Password: `password2`

## Database

The MongoDB database contains the following collections:

- `users` - User information
- `authTokens` - Authentication tokens
- `monsterTemplates` - Monster templates
- `playerMonsters` - Player-owned monsters
- `summonLogs` - Summoning records
- `battleLogs` - Battle records
- `royalRumbles` - Royal Rumble records

## Testing

### Automated Testing

The project contains comprehensive unit tests and integration tests using JUnit 5 and Mockito frameworks. Tests cover all major services and controllers:

- **AuthServiceTest** - Tests authentication service, including login and token validation
- **MonsterServiceTest** - Tests monster service, including retrieving monsters, upgrading skills, and creating monsters
- **PlayerServiceTest** - Tests player service, including profile retrieval, leveling up, and monster management
- **SummonServiceTest** - Tests summoning service, including monster summoning and log processing
- **BattleServiceTest** - Tests battle service, including battles and battle record retrieval
- **RoyalRumbleServiceTest** - Tests Royal Rumble service, including starting rumbles and experience calculation
- **AuthControllerIntegrationTest** - Tests authentication controller API endpoints

Run unit tests:

```bash
mvn test
```

Run test suite:

```bash
mvn test -Dtest=GatchaApiTestSuite
```

Test coverage:

- Service layer: 90%+
- Controller layer: 80%+

### Manual Testing

Manual testing has been performed on all API endpoints using curl commands and the frontend interface. The testing confirmed:

- All API endpoints are accessible and return expected responses
- Error handling works correctly for invalid inputs
- Authentication and authorization are properly enforced
- Data persistence works correctly across all operations
- Frontend components correctly display data from the API
- User interactions (battles, summons, etc.) produce expected results

## Performance Considerations

- The application is designed to handle multiple concurrent users
- MongoDB provides efficient data storage and retrieval
- Docker containerization ensures consistent performance across environments
- API responses are optimized to minimize data transfer
- Frontend components use lazy loading to improve initial load time
