package com.gatcha.api.battle.service.impl;

import com.gatcha.api.battle.dto.RoyalRumbleResult;
import com.gatcha.api.battle.model.BattleLog;
import com.gatcha.api.battle.repository.BattleLogRepository;
import com.gatcha.api.battle.service.RoyalRumbleService;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.model.Skill;
import com.gatcha.api.monster.service.MonsterService;
import com.gatcha.api.player.service.PlayerService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Royal Rumble Service Implementation
 */
@Service
public class RoyalRumbleServiceImpl implements RoyalRumbleService {

    private final MonsterService monsterService;
    private final BattleLogRepository battleLogRepository;
    private final MongoTemplate mongoTemplate;
    private final PlayerService playerService;

    // Store the experience gained from the most recent royal rumble
    private final Map<String, Integer> rumbleExperienceGained = new HashMap<>();

    public RoyalRumbleServiceImpl(MonsterService monsterService, BattleLogRepository battleLogRepository,
            MongoTemplate mongoTemplate, PlayerService playerService) {
        this.monsterService = monsterService;
        this.battleLogRepository = battleLogRepository;
        this.mongoTemplate = mongoTemplate;
        this.playerService = playerService;
    }

    @Override
    public RoyalRumbleResult startRoyalRumble(String username) {
        // Get all user's monsters
        List<PlayerMonster> monsters = monsterService.getMonstersByUsername(username);

        // If there are fewer than 3 monsters, cannot start royal rumble
        if (monsters.size() < 3) {
            throw new IllegalStateException("At least 3 monsters are required to start a Royal Rumble");
        }

        // Select 3 random monsters for the royal rumble (if user has more than 3
        // monsters)
        List<PlayerMonster> selectedMonsters = new ArrayList<>(monsters);
        if (selectedMonsters.size() > 3) {
            Collections.shuffle(selectedMonsters);
            selectedMonsters = selectedMonsters.subList(0, 3);
        }

        // Get the list of selected monster IDs
        List<String> monsterIds = selectedMonsters.stream()
                .map(PlayerMonster::getId)
                .collect(Collectors.toList());

        // Call the method with the monster ID list
        return startRoyalRumble(username, monsterIds);
    }

    @Override
    public RoyalRumbleResult startRoyalRumble(String username, List<String> monsterIds) {
        System.out.println("Starting royal rumble for user: " + username + " with monster IDs: " + monsterIds);

        // Validate monster count
        if (monsterIds.size() < 3) {
            throw new IllegalStateException("At least 3 monsters are required to start a Royal Rumble");
        }

        // Verify all monsters belong to the user
        List<String> userMonsterIds = monsterService.getMonstersByUsername(username)
                .stream()
                .map(PlayerMonster::getId)
                .collect(Collectors.toList());

        for (String monsterId : monsterIds) {
            if (!userMonsterIds.contains(monsterId)) {
                throw new IllegalArgumentException("Monster " + monsterId + " does not belong to user " + username);
            }
        }

        // Get details of selected monsters
        List<PlayerMonster> selectedMonsters = new ArrayList<>();
        for (String monsterId : monsterIds) {
            try {
                PlayerMonster monster = monsterService.getMonsterById(monsterId, username);
                selectedMonsters.add(monster);
            } catch (Exception e) {
                System.out.println("Error getting monster " + monsterId + ": " + e.getMessage());
                throw new IllegalArgumentException("Error getting monster " + monsterId, e);
            }
        }

        // Create royal rumble result
        RoyalRumbleResult result = new RoyalRumbleResult();
        result.setId(UUID.randomUUID().toString());
        result.setRumbleDate(new Date());
        result.setParticipantIds(monsterIds);
        result.setRounds(new ArrayList<>());

        // Copy monster attributes for battle
        Map<String, Integer> monsterHp = new HashMap<>();
        Map<String, Map<Integer, Integer>> monsterCooldowns = new HashMap<>();

        // Initialize monster HP and skill cooldowns
        for (PlayerMonster monster : selectedMonsters) {
            monsterHp.put(monster.getId(), monster.getHp());
            monsterCooldowns.put(monster.getId(), initCooldowns(monster));
        }

        // List of alive monster IDs
        List<String> aliveMonsterIds = new ArrayList<>(monsterHp.keySet());

        // Round counter
        int roundNumber = 0;

        // Royal rumble loop, until only one monster remains
        while (aliveMonsterIds.size() > 1) {
            roundNumber++;

            // Create current round
            RoyalRumbleResult.RumbleRound round = new RoyalRumbleResult.RumbleRound();
            round.setRoundNumber(roundNumber);
            round.setActions(new ArrayList<>());

            // Add round start log
            result.getBattleLog()
                    .add("Round " + roundNumber + " begins, remaining monsters: " + aliveMonsterIds.size());

            // Randomly order monsters to determine attack sequence
            Collections.shuffle(aliveMonsterIds);

            // Each monster performs one attack
            for (int i = 0; i < aliveMonsterIds.size(); i++) {
                String attackerId = aliveMonsterIds.get(i);

                // If attacker is already dead, skip
                if (!aliveMonsterIds.contains(attackerId)) {
                    continue;
                }

                // Randomly select a target (cannot be self)
                List<String> possibleTargets = new ArrayList<>(aliveMonsterIds);
                possibleTargets.remove(attackerId);

                // If there are no targets to attack, skip
                if (possibleTargets.isEmpty()) {
                    continue;
                }

                String targetId = possibleTargets.get(new Random().nextInt(possibleTargets.size()));

                // Get attacker and target monsters
                PlayerMonster attacker = getMonsterById(selectedMonsters, attackerId);
                PlayerMonster defender = getMonsterById(selectedMonsters, targetId);

                // Add log for attacker selecting target
                result.getBattleLog()
                        .add(generateMonsterName(attacker) + " chooses to attack " + generateMonsterName(defender));

                // Perform attack
                int defenderHp = performAttack(attacker, defender, monsterHp.get(attackerId),
                        monsterHp.get(targetId), monsterCooldowns.get(attackerId), round.getActions());

                // Update target HP
                monsterHp.put(targetId, defenderHp);

                // Get the last battle action
                if (!round.getActions().isEmpty()) {
                    BattleLog.BattleAction lastAction = round.getActions().get(round.getActions().size() - 1);

                    // Add log for attack result
                    String skillName = lastAction.getSkillNum() == 0 ? "Basic Attack"
                            : "Skill " + lastAction.getSkillNum();
                    result.getBattleLog().add(generateMonsterName(attacker) + " uses " + skillName +
                            " on " + generateMonsterName(defender) + " dealing " +
                            lastAction.getDamage() + " damage, " +
                            generateMonsterName(defender) + " remaining HP: " +
                            lastAction.getRemainingHp());
                }

                // If target dies, remove from alive list
                if (defenderHp <= 0) {
                    aliveMonsterIds.remove(targetId);
                    // Add log for monster defeat
                    result.getBattleLog().add(generateMonsterName(defender) + " has been defeated!");
                }
            }

            // Update all monsters' skill cooldowns
            for (String monsterId : aliveMonsterIds) {
                updateCooldowns(monsterCooldowns.get(monsterId));
            }

            // Set remaining monsters after the round
            round.setRemainingMonsterIds(new ArrayList<>(aliveMonsterIds));

            // Add round to result
            result.getRounds().add(round);

            // Add round end log
            result.getBattleLog().add("Round " + roundNumber + " ends, remaining monsters: " + aliveMonsterIds.size());
        }

        // Set winner
        String winnerId = aliveMonsterIds.get(0);
        PlayerMonster winner = getMonsterById(selectedMonsters, winnerId);
        result.setWinner(winner);

        // Add winner log
        result.getBattleLog().add(generateMonsterName(winner) + " is the final victor!");

        // Calculate experience gained: base experience (50) + number of participating
        // monsters * 10
        int expGained = 50 + (selectedMonsters.size() * 10);

        // Add experience to the winning monster
        monsterService.addExperience(winnerId, username, expGained);

        // Store experience gained from this royal rumble
        rumbleExperienceGained.put(result.getId(), expGained);
        result.setExperienceGained(expGained);

        // Remove all losing monsters from player's collection
        List<String> loserIds = new ArrayList<>(result.getParticipantIds());
        loserIds.remove(winnerId); // Remove winner from the list
        for (String loserId : loserIds) {
            playerService.removeMonster(username, loserId);
        }

        // Save royal rumble result to MongoDB (as a custom document)
        mongoTemplate.save(result, "royalRumbles");

        return result;
    }

    @Override
    public int getExperienceGained(String rumbleId) {
        // First try to get from memory
        if (rumbleExperienceGained.containsKey(rumbleId)) {
            return rumbleExperienceGained.get(rumbleId);
        }

        // If not in memory, query from MongoDB
        Query query = new Query(Criteria.where("id").is(rumbleId));
        RoyalRumbleResult result = mongoTemplate.findOne(query, RoyalRumbleResult.class, "royalRumbles");

        if (result != null) {
            // Cache result in memory
            rumbleExperienceGained.put(rumbleId, result.getExperienceGained());
            return result.getExperienceGained();
        }

        return 0;
    }

    @Override
    public List<RoyalRumbleResult> getAllRumbles() {
        // Get all royal rumble records from MongoDB
        return mongoTemplate.findAll(RoyalRumbleResult.class, "royalRumbles");
    }

    /**
     * Get monster by ID from monster list
     */
    private PlayerMonster getMonsterById(List<PlayerMonster> monsters, String id) {
        return monsters.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Monster not found: " + id));
    }

    /**
     * Initialize skill cooldowns
     */
    private Map<Integer, Integer> initCooldowns(PlayerMonster monster) {
        Map<Integer, Integer> cooldowns = new HashMap<>();
        for (Skill skill : monster.getSkills()) {
            cooldowns.put(skill.getNum(), 0);
        }
        return cooldowns;
    }

    /**
     * Update skill cooldowns
     */
    private void updateCooldowns(Map<Integer, Integer> cooldowns) {
        for (Map.Entry<Integer, Integer> entry : cooldowns.entrySet()) {
            if (entry.getValue() > 0) {
                cooldowns.put(entry.getKey(), entry.getValue() - 1);
            }
        }
    }

    /**
     * Perform attack
     */
    private int performAttack(PlayerMonster attacker, PlayerMonster defender, int attackerHp, int defenderHp,
            Map<Integer, Integer> cooldowns, List<BattleLog.BattleAction> actions) {
        // Sort skills by skill number in descending order
        List<Skill> skills = new ArrayList<>(attacker.getSkills());
        skills.sort(Comparator.comparing(Skill::getNum).reversed());

        // Select available skill
        Skill selectedSkill = null;
        for (Skill skill : skills) {
            if (cooldowns.get(skill.getNum()) == 0) {
                selectedSkill = skill;
                break;
            }
        }

        if (selectedSkill == null) {
            // All skills are on cooldown, use basic attack
            int damage = attacker.getAtk() - (defender.getDef() / 2);
            damage = Math.max(1, damage); // At least 1 damage

            defenderHp -= damage;

            // Record battle action
            BattleLog.BattleAction action = new BattleLog.BattleAction();
            action.setMonsterId(attacker.getId());
            action.setSkillNum(0); // Basic attack
            action.setDamage(damage);
            action.setTargetId(defender.getId());
            action.setRemainingHp(Math.max(0, defenderHp));

            actions.add(action);
        } else {
            // Use selected skill
            int baseDamage = selectedSkill.getDmg();

            // Calculate stat bonus
            double statBonus = 0;
            String statType = selectedSkill.getRatio().getStat();
            double percent = selectedSkill.getRatio().getPercent() / 100.0;

            switch (statType) {
                case "atk":
                    statBonus = attacker.getAtk() * percent;
                    break;
                case "def":
                    statBonus = attacker.getDef() * percent;
                    break;
                case "hp":
                    statBonus = attackerHp * percent;
                    break;
                case "vit":
                    statBonus = attacker.getVit() * percent;
                    break;
            }

            int totalDamage = (int) (baseDamage + statBonus);
            totalDamage = Math.max(1, totalDamage - (defender.getDef() / 3)); // Defense reduction

            defenderHp -= totalDamage;

            // Set skill cooldown
            cooldowns.put(selectedSkill.getNum(), selectedSkill.getCooldown());

            // Record battle action
            BattleLog.BattleAction action = new BattleLog.BattleAction();
            action.setMonsterId(attacker.getId());
            action.setSkillNum(selectedSkill.getNum());
            action.setDamage(totalDamage);
            action.setTargetId(defender.getId());
            action.setRemainingHp(Math.max(0, defenderHp));

            actions.add(action);
        }

        return Math.max(0, defenderHp);
    }

    /**
     * Generate a friendly name for the monster
     */
    private String generateMonsterName(PlayerMonster monster) {
        return com.gatcha.api.utils.NameGenerator.generateName(monster.getId(), monster.getElement());
    }
}