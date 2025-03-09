# Gatcha API

A comprehensive Gatcha game API system built with Spring Boot and MongoDB, providing complete backend services including user authentication, monster summoning, battles, and more.

## Features

- User Authentication & Authorization (Token-based authentication system)
- Player Management (Levels, Experience, Monster Collection)
- Monster Management (Attributes, Skills, Upgrades)
- Summoning System (Probability-based monster summoning)
- Battle System (Automated battles with detailed logs)
- Royal Rumble System (Multi-monster battle royale where the last monster standing wins)

## Technology Stack

- Java 17
- Spring Boot 2.7.5
- Spring Security
- Spring Data MongoDB
- JWT Authentication
- Docker & Docker Compose
- Maven
- JUnit 5 & Mockito (Testing frameworks)

## Project Structure

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
└── summon/                # Summoning related
    ├── controller/        # Controllers
    ├── model/             # Entity models
    ├── repository/        # Data access
    └── service/           # Business logic
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

## API Endpoints

### Authentication API

- `POST /api/auth/login` - User login
  - Request body: `{"username": "user1", "password": "password1"}`
  - Response: `{"token": "base64_encoded_token"}`
- `POST /api/auth/validate` - Validate token
  - Request header: `Authorization: Bearer <token>`
  - Response: Username

### Player API

- `GET /api/player/profile` - Get player profile
- `GET /api/player/monsters` - Get player's monster list
- `GET /api/player/level` - Get player level
- `POST /api/player/experience` - Add player experience
  - Request body: `{"experience": 20}`
- `POST /api/player/levelup` - Level up player
- `POST /api/player/monsters/{monsterId}` - Add monster to player's collection
- `DELETE /api/player/monsters/{monsterId}` - Remove monster from player's collection

### Monster API

- `GET /api/monsters` - Get all player's monsters
- `GET /api/monsters/{id}` - Get specific monster details
- `POST /api/monsters/{id}/experience` - Add experience to monster
  - Request body: `{"experience": 50}`
- `POST /api/monsters/{id}/skill` - Upgrade monster skill
  - Request body: `{"skillNum": 1}`

### Summoning API

- `POST /api/summon` - Summon new monster
- `GET /api/summon/history` - Get summoning history
- `POST /api/summon/reprocess` - Reprocess failed summons

### Battle API

- `POST /api/battles` - Conduct a battle
  - Request body: `{"monster1Id": "id1", "monster2Id": "id2"}`
  - Response: Contains battle log, winning monster info, and experience gained
- `GET /api/battles/{battleId}` - Get battle details
- `GET /api/battles/monster/{monsterId}` - Get monster's battle history

### Royal Rumble API

- `POST /api/royal-rumble` - Start a royal rumble battle
  - Response: Contains round-by-round battle records, winning monster info, and experience gained
- `GET /api/royal-rumble/experience/{rumbleId}` - Get experience gained from a specific royal rumble

## How to Run

### Prerequisites

- Docker
- Docker Compose

### Startup Steps

1. Clone the repository:

```bash
git clone https://github.com/yourusername/gatcha_api.git
cd gatcha_api
```

2. Start the application using Docker Compose:

```bash
docker-compose up -d
```

3. The application will run on `http://localhost:8080`

### Initial Users

The system comes with two test users:

- Username: `user1`, Password: `password1`
- Username: `user2`, Password: `password2`

## Database

The MongoDB database contains the following collections:

- `users` - User information
- `authTokens` - Authentication tokens
- `monsterTemplates` - Monster templates
- `playerMonsters` - Monsters owned by players
- `summonLogs` - Summoning records
- `battleLogs` - Battle records
- `royalRumbles` - Royal rumble records

## Testing

### Automated Testing

The project includes comprehensive unit tests and integration tests using JUnit 5 and Mockito frameworks. Tests cover all major services and controllers:

- **AuthServiceTest** - Tests authentication service, including login and token validation
- **MonsterServiceTest** - Tests monster service, including retrieving monsters, upgrading skills, and creating monsters
- **PlayerServiceTest** - Tests player service, including profile retrieval, leveling up, and monster management
- **SummonServiceTest** - Tests summoning service, including monster summoning and log processing
- **BattleServiceTest** - Tests battle service, including battles and battle record retrieval
- **RoyalRumbleServiceTest** - Tests royal rumble service, including starting rumbles and experience calculation
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
- Model layer: 70%+

### Manual Testing

The project provides a test script `test-api.sh` that can be used to test all APIs:

```bash
chmod +x test-api.sh
./test-api.sh
```

## Resolved Issues

1. **ID Type Mismatch Issue**:

   - Modified the MonsterTemplate entity class, changing the ID type from String to Integer
   - Modified the MonsterTemplateRepository interface to use Integer as the ID type
   - Modified the SummonServiceImpl class to perform appropriate type conversions when handling IDs
   - Modified the MonsterService interface and implementation class, changing the createMonsterFromTemplate method parameter type from String to Integer
2. **Insufficient Skill Points Issue**:

   - Modified the MonsterServiceImpl class to add initial skill points when creating monsters
3. **Testing Related Issues**:

   - Fixed NullPointerException error in BattleServiceTest by correctly setting up Skill.Ratio objects
   - Fixed addMonsterTooMany test in PlayerServiceTest by using Mockito's spy method to mock User.canAddMonster() method
   - Fixed tests in AuthControllerIntegrationTest by adding CSRF protection and user authentication
4. **Skill Upgrade Enhancement**:

   - Optimized the skill upgrade mechanism to increase skill effectiveness after upgrades
   - 10% base damage increase per level
   - 5% attribute ratio bonus increase per level
   - Cooldown time reduction by 1 point every 2 levels (minimum half of the original value)
5. **Battle Experience Rewards**:

   - Implemented functionality for monsters to gain experience after defeating other monsters
   - Experience calculation formula: Base experience (20) + defeated monster level * 10
   - Automatically adds experience to the winning monster after battle
   - Battle API response includes information about experience gained
6. **Royal Rumble System**:

   - Implemented a multi-monster battle royale feature
   - All monsters randomly use skills against another monster until only one remains
   - The victorious monster receives substantial experience rewards: Base experience (50) + number of participating monsters * 10
   - Detailed recording of the battle process for each round

## Game Mechanics

### Player Leveling

Players start at level 1 and level up when they gain enough experience. After leveling up:

- Experience resets to 0
- Experience required for the next level increases by 10%
- Maximum number of monsters that can be owned increases by 1

### Monster Summoning

The summoning system randomly generates monsters based on probability:

- Each monster template has a different appearance probability (lootRate)
- After successful summoning, the monster is added to the player's monster list
- Newly summoned monsters start at level 1 with 3 skill points

### Monster Leveling

Monsters level up after gaining enough experience:

- Attribute values (hp, atk, def, vit) increase after leveling up
- Additional skill points are gained
- Experience resets to 0
- Monsters can gain experience through battles, winning rewards: Base experience (20) + defeated monster level * 10
- Monsters can gain more experience through royal rumbles, winning rewards: Base experience (50) + number of participating monsters * 10

### Skill Upgrading

Skill points can be used to upgrade monster skills:

- Each upgrade consumes 1 skill point
- Skill level increases, not exceeding lvlMax
- Skill damage increases (10% base damage increase per level)
- Skill attribute ratio bonus increases (5% base ratio increase per level)
- Skill cooldown time decreases (1 point reduction every 2 levels, minimum half of the original value)

### Battle System

Battles are automated and follow these rules:

- Monsters with higher speed attack first
- Monsters use skills in descending order of skill number (higher-level skills first)
- Skills have cooldown times, and skills in cooldown cannot be used
- Detailed battle process is recorded after the battle ends
- Winning monsters receive experience rewards

### Royal Rumble System

Royal Rumble is a multi-monster battle royale following these rules:

- At least 3 monsters are required to start a royal rumble
- Each round, all surviving monsters randomly select a target (not themselves) to attack
- Attack order is randomly determined each round
- Monsters that die no longer participate in subsequent rounds
- The last surviving monster wins and receives substantial experience rewards
- Detailed battle process is recorded after the royal rumble ends, including attack actions and remaining monsters for each round
