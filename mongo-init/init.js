db = db.getSiblingDB('gatcha_db');

// Create users collection and index
db.createCollection('users');
db.users.createIndex({ "username": 1 }, { unique: true });

// Create players collection and index
db.createCollection('players');
db.players.createIndex({ "userId": 1 }, { unique: true });

// Create monsters collection and indexes
db.createCollection('monsters');
db.monsters.createIndex({ "playerId": 1 });
db.monsters.createIndex({ "monsterId": 1 });

// Create monster templates collection
db.createCollection('monster_templates');
db.monster_templates.createIndex({ "templateId": 1 }, { unique: true });

// Create battles collection
db.createCollection('battles');
db.battles.createIndex({ "battleId": 1 }, { unique: true });

// Create summons collection
db.createCollection('summons');
db.summons.createIndex({ "summonId": 1 }, { unique: true });

// Create auth tokens collection
db.createCollection('auth_tokens');
db.auth_tokens.createIndex({ "token": 1 }, { unique: true });
db.auth_tokens.createIndex({ "expirationDate": 1 }, { expireAfterSeconds: 0 }); 