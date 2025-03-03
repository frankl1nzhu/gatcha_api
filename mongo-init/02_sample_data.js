db = db.getSiblingDB('gatcha_db');

// Create sample users
db.users.insertMany([
    {
        username: "test_user",
        password: "$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQXaCt4EzhQJmQzD.9VcXRf3ICyGOW", // "password123"
        active: true,
        createdAt: new Date().getTime()
    }
]);

// Create sample players
db.players.insertMany([
    {
        userId: db.users.findOne({username: "test_user"})._id.toString(),
        username: "test_user",
        level: 1,
        experience: 0,
        experienceToNextLevel: 50,
        maxMonsters: 10,
        monsterIds: [],
        createdAt: new Date().getTime(),
        updatedAt: new Date().getTime()
    }
]);

// Create sample monster templates
const fireMonsterSkills = [
    {
        name: "Flame Strike",
        baseDamage: 125,
        scalingStat: "ATK",
        scalingRatio: 0.25,
        cooldown: 0,
        currentCooldown: 0,
        level: 1,
        maxLevel: 5
    },
    {
        name: "Rage of Fire",
        baseDamage: 250,
        scalingStat: "ATK",
        scalingRatio: 0.275,
        cooldown: 2,
        currentCooldown: 0,
        level: 1,
        maxLevel: 7
    },
    {
        name: "Hellfire",
        baseDamage: 425,
        scalingStat: "ATK",
        scalingRatio: 0.40,
        cooldown: 5,
        currentCooldown: 0,
        level: 1,
        maxLevel: 5
    }
];

const windMonsterSkills = [
    {
        name: "Defense Counter",
        baseDamage: 200,
        scalingStat: "DEF",
        scalingRatio: 0.10,
        cooldown: 0,
        currentCooldown: 0,
        level: 1,
        maxLevel: 4
    },
    {
        name: "Guardian Strike",
        baseDamage: 315,
        scalingStat: "DEF",
        scalingRatio: 0.175,
        cooldown: 2,
        currentCooldown: 0,
        level: 1,
        maxLevel: 5
    },
    {
        name: "Iron Wall Rush",
        baseDamage: 525,
        scalingStat: "DEF",
        scalingRatio: 0.20,
        cooldown: 6,
        currentCooldown: 0,
        level: 1,
        maxLevel: 7
    }
];

const waterMonsterSkills1 = [
    {
        name: "Life Drain",
        baseDamage: 150,
        scalingStat: "HP",
        scalingRatio: 0.05,
        cooldown: 0,
        currentCooldown: 0,
        level: 1,
        maxLevel: 7
    },
    {
        name: "Life Burst",
        baseDamage: 350,
        scalingStat: "HP",
        scalingRatio: 0.07,
        cooldown: 2,
        currentCooldown: 0,
        level: 1,
        maxLevel: 4
    },
    {
        name: "Water Blade Slash",
        baseDamage: 250,
        scalingStat: "ATK",
        scalingRatio: 0.12,
        cooldown: 5,
        currentCooldown: 0,
        level: 1,
        maxLevel: 5
    }
];

const waterMonsterSkills2 = [
    {
        name: "Water Blade",
        baseDamage: 150,
        scalingStat: "ATK",
        scalingRatio: 0.275,
        cooldown: 0,
        currentCooldown: 0,
        level: 1,
        maxLevel: 6
    },
    {
        name: "Torrent Slash",
        baseDamage: 285,
        scalingStat: "ATK",
        scalingRatio: 0.275,
        cooldown: 2,
        currentCooldown: 0,
        level: 1,
        maxLevel: 9
    },
    {
        name: "Wrath of the Sea God",
        baseDamage: 550,
        scalingStat: "ATK",
        scalingRatio: 0.60,
        cooldown: 4,
        currentCooldown: 0,
        level: 1,
        maxLevel: 4
    }
];

db.monster_templates.insertMany([
    {
        name: "Fire Demon Warrior",
        elementType: "FIRE",
        summonRate: 0.3,
        baseStats: {
            HP: 1200,
            ATK: 450,
            DEF: 300,
            SPD: 85
        },
        skillTemplates: fireMonsterSkills
    },
    {
        name: "Wind Guardian",
        elementType: "WIND",
        summonRate: 0.3,
        baseStats: {
            HP: 1500,
            ATK: 200,
            DEF: 450,
            SPD: 80
        },
        skillTemplates: windMonsterSkills
    },
    {
        name: "Deep Sea Beast",
        elementType: "WATER",
        summonRate: 0.3,
        baseStats: {
            HP: 2500,
            ATK: 150,
            DEF: 200,
            SPD: 70
        },
        skillTemplates: waterMonsterSkills1
    },
    {
        name: "Water Sword Saint",
        elementType: "WATER",
        summonRate: 0.1,
        baseStats: {
            HP: 1200,
            ATK: 550,
            DEF: 350,
            SPD: 80
        },
        skillTemplates: waterMonsterSkills2
    }
]);