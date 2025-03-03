# Gatcha Game API

A Spring Boot-based Gatcha Game API system that provides a comprehensive backend for a monster collection and battle game.

## Features

### Authentication System

- User registration and login
- JWT-based authentication
- Token auto-renewal mechanism
- Account status management

### Player System

- Player profile management
- Experience and leveling system
- Monster collection management
- Resource management

### Monster System

- Multiple monster types with different elements (Fire, Water, Wind)
- Unique skills and attributes for each monster
- Experience and leveling system
- Skill upgrade system with skill points
- Dynamic stat calculation based on level

### Summon System

- Probability-based monster summoning
- Different rarity levels for monsters
- Summon history tracking
- Guaranteed rare monster mechanics

### Battle System

- Turn-based combat system
- Element-based damage calculation
- Skill cooldown management
- Battle record and replay functionality
- Player vs Player battles

## Technology Stack

- Java 17
- Spring Boot 3.2.3
- MongoDB
- Docker & Docker Compose
- Maven
- JUnit 5 & Mockito for testing

## System Requirements

- Docker
- Docker Compose
- Git
- Java 17 or higher

## Quick Start

1. Clone the repository

```bash
git clone <repository-url>
cd gatcha-api
```

2. Start the services

```bash
docker-compose up -d
```

The services will be available at:

- API Service: http://localhost:8080
- MongoDB: localhost:27017

## Test Account

A test account is created during initialization:

- Username: test_user
- Password: password123

## API Documentation

### Authentication API

#### Register

```
POST /api/auth/register
Content-Type: application/json

Request:
{
    "username": "string",
    "password": "string"
}

Response:
{
    "token": "string",
    "username": "string",
    "expiresIn": long
}
```

#### Login

```
POST /api/auth/login
Content-Type: application/json

Request:
{
    "username": "string",
    "password": "string"
}

Response:
{
    "token": "string",
    "username": "string",
    "expiresIn": long
}
```

### Player API

#### Get Player Profile

```
GET /api/players/profile
Authorization: Bearer {token}

Response:
{
    "id": "string",
    "username": "string",
    "level": int,
    "experience": double,
    "experienceToNextLevel": double,
    "maxMonsters": int,
    "monsterIds": ["string"],
    "updatedAt": long
}
```

### Monster API

#### Get Player's Monsters

```
GET /api/monsters
Authorization: Bearer {token}

Response:
[{
    "id": "string",
    "name": "string",
    "elementType": "FIRE|WATER|WIND",
    "level": int,
    "experience": double,
    "experienceToNextLevel": double,
    "stats": {
        "hp": double,
        "atk": double,
        "def": double,
        "spd": double
    },
    "skills": [{
        "name": "string",
        "baseDamage": double,
        "scalingStat": "HP|ATK|DEF|SPD",
        "scalingRatio": double,
        "cooldown": int,
        "level": int,
        "maxLevel": int
    }],
    "skillPoints": int,
    "updatedAt": long
}]
```

#### Add Monster Experience

```
POST /api/monsters/{monsterId}/experience
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "experience": double
}

Response: MonsterResponse
```

#### Upgrade Monster Skill

```
POST /api/monsters/{monsterId}/skills/upgrade
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "skillIndex": int
}

Response: MonsterResponse
```

### Summon API

#### Summon Monster

```
POST /api/summons
Authorization: Bearer {token}

Response:
{
    "summonId": "string",
    "monsterId": "string",
    "monsterName": "string",
    "elementType": "FIRE|WATER|WIND",
    "summonedAt": long
}
```

### Battle API

#### Start Battle

```
POST /api/battles
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "playerMonsterId": "string",
    "opponentMonsterId": "string"
}

Response:
{
    "battleId": "string",
    "monster1Id": "string",
    "monster2Id": "string",
    "winnerMonsterId": "string",
    "winnerPlayerId": "string",
    "actions": [{
        "monsterId": "string",
        "monsterName": "string",
        "skillIndex": int,
        "skillName": "string",
        "damage": double,
        "timestamp": long
    }],
    "status": "IN_PROGRESS|COMPLETED|ERROR",
    "startTime": long,
    "endTime": long
}
```

## Project Structure

```
gatcha-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── gatcha/
│   │   │           └── api/
│   │   │               ├── config/
│   │   │               ├── controller/
│   │   │               ├── dto/
│   │   │               ├── model/
│   │   │               ├── repository/
│   │   │               ├── security/
│   │   │               ├── service/
│   │   │               └── GatchaApiApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── gatcha/
│                   └── api/
│                       └── service/
│                           ├── AuthServiceTest.java
│                           └── MonsterServiceTest.java
├── mongo-init/
│   ├── init.js
│   └── sample_data.js
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## Development Guide

1. Local Development Setup

```bash
./mvnw spring-boot:run
```

2. Run Tests

```bash
./mvnw test
```

3. Build Docker Image

```bash
./mvnw clean package
docker build -t gatcha-api .
```

## Testing

The project includes comprehensive unit tests for all major components. Tests are written using JUnit 5 and Mockito.

### Running Tests

To run all tests:

```bash
mvn clean test
```

### Test Coverage

The test suite covers the following areas:

#### Authentication Service Tests

- User registration with validation
- User login with credentials verification
- Token generation and validation
- Error handling for invalid credentials

#### Player Service Tests

- Player profile management
- Experience system
- Monster collection management
- Resource management validation

#### Monster Service Tests

- Monster creation and initialization
- Experience and leveling system
- Skill upgrade mechanics
- Stat calculation validation
- Access control validation

#### Summon Service Tests

- Monster summoning mechanics
  - Successful summoning when player has space
  - Exception handling when at max capacity
  - Probability-based template selection
- Summon history tracking
- Unprocessed summons handling

Example test for summon mechanics:

```java
@Test
void summonMonster_WhenPlayerHasSpace_ShouldSummonSuccessfully() {
    // Setup monster template with base stats
    Map<StatType, Double> baseStats = new HashMap<>();
    baseStats.put(StatType.HP, 100.0);
    baseStats.put(StatType.ATK, 10.0);
    baseStats.put(StatType.DEF, 10.0);
    baseStats.put(StatType.SPD, 10.0);
    monsterTemplate.setBaseStats(baseStats);

    // Create expected monster
    Monster monster = new Monster();
    monster.setId("generatedId");
    monster.setPlayerId(PLAYER_ID);
    monster.setName(monsterTemplate.getName());
    monster.setElementType(monsterTemplate.getElementType());
    monster.initializeStats(baseStats);

    // Setup mocks
    when(monsterTemplateRepository.findAllByOrderBySummonRateDesc())
        .thenReturn(Arrays.asList(monsterTemplate));
    when(monsterRepository.countByPlayerId(PLAYER_ID)).thenReturn(5L);
    when(monsterRepository.save(any(Monster.class))).thenReturn(monster);
    when(summonRecordRepository.save(any(SummonRecord.class)))
        .thenAnswer(i -> i.getArgument(0));
    when(playerService.addMonster(anyString(), anyString()))
        .thenReturn(new PlayerResponse());

    // Execute and verify
    SummonResponse response = summonService.summonMonster(PLAYER_ID);

    assertNotNull(response);
    assertNotNull(response.getMonsterId());
    assertEquals(monsterTemplate.getName(), response.getMonsterName());
    assertEquals(monsterTemplate.getElementType(), response.getElementType());
    verify(monsterRepository).save(any(Monster.class));
    verify(summonRecordRepository, times(2)).save(any(SummonRecord.class));
    verify(playerService).addMonster(PLAYER_ID, monster.getId());
}

@Test
void summonMonster_WhenPlayerAtMaxCapacity_ShouldThrowException() {
    when(monsterRepository.countByPlayerId(PLAYER_ID)).thenReturn(10L);

    assertThrows(IllegalStateException.class, 
        () -> summonService.summonMonster(PLAYER_ID));
    verify(monsterRepository, never()).save(any());
    verify(summonRecordRepository, never()).save(any());
    verify(playerService, never()).addMonster(anyString(), anyString());
}
```

#### Battle Service Tests

- Battle initialization
- Turn-based combat mechanics
- Damage calculation
- Battle record creation
- Battle history tracking

### Test Best Practices

The test suite follows these best practices:

- Each test method focuses on a single functionality
- Clear test method naming using the pattern: methodName_scenario_expectedBehavior
- Proper use of @BeforeEach for test setup
- Comprehensive assertions to validate all aspects of the response
- Proper mock setup and verification
- Error case testing with appropriate assertions
- Clean and maintainable test code with no unnecessary stubbings
