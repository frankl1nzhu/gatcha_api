db = db.getSiblingDB('gatcha');

// Create users collection
db.createCollection('users');
db.users.insertMany([
  {
    username: 'user1',
    password: 'password1',
    level: 1,
    experience: 0,
    maxExperience: 50,
    monsters: []
  },
  {
    username: 'user2',
    password: 'password2',
    level: 5,
    experience: 20,
    maxExperience: 73,
    monsters: []
  }
]);

// Create monster templates collection
db.createCollection('monsterTemplates');
// Data imported from monsters.js file will be automatically loaded during Docker startup

// Create player monsters collection
db.createCollection('playerMonsters');

// Create authentication tokens collection
db.createCollection('authTokens');

// Create summon logs collection
db.createCollection('summonLogs');

// Create battle logs collection
db.createCollection('battleLogs');

// Create royal rumble collection
db.createCollection('royalRumbles'); 