package com.gatcha.api.service;

import com.gatcha.api.dto.BattleResponse;
import com.gatcha.api.model.BattleAction;
import com.gatcha.api.model.BattleRecord;
import com.gatcha.api.model.Monster;
import com.gatcha.api.model.Skill;
import com.gatcha.api.model.StatType;
import com.gatcha.api.repository.BattleRecordRepository;
import com.gatcha.api.repository.MonsterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BattleService {
    private final BattleRecordRepository battleRecordRepository;
    private final MonsterRepository monsterRepository;

    @Transactional
    public BattleResponse startBattle(String playerId, String playerMonsterId, String opponentMonsterId) {
        Monster playerMonster = monsterRepository.findByIdAndPlayerId(playerMonsterId, playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player monster not found"));

        Monster opponentMonster = monsterRepository.findById(opponentMonsterId)
                .orElseThrow(() -> new IllegalArgumentException("Opponent monster not found"));

        BattleRecord record = new BattleRecord();
        record.setPlayer1Id(playerId);
        record.setPlayer2Id(opponentMonster.getPlayerId());
        record.setMonster1Id(playerMonsterId);
        record.setMonster2Id(opponentMonsterId);

        // Execute battle logic
        simulateBattle(record, playerMonster, opponentMonster);

        record = battleRecordRepository.save(record);
        return convertToResponse(record);
    }

    public List<BattleResponse> getPlayerBattles(String playerId) {
        return battleRecordRepository.findByPlayer1IdOrPlayer2IdOrderByStartTimeDesc(playerId, playerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BattleResponse getBattle(String battleId) {
        BattleRecord record = battleRecordRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));
        return convertToResponse(record);
    }

    private void simulateBattle(BattleRecord record, Monster monster1, Monster monster2) {
        try {
            double hp1 = monster1.getStat(StatType.HP);
            double hp2 = monster2.getStat(StatType.HP);

            // Determine who goes first based on speed
            boolean monster1First = monster1.getStat(StatType.SPD) >= monster2.getStat(StatType.SPD);
            Monster firstMonster = monster1First ? monster1 : monster2;
            Monster secondMonster = monster1First ? monster2 : monster1;
            double firstHp = monster1First ? hp1 : hp2;
            double secondHp = monster1First ? hp2 : hp1;

            while (firstHp > 0 && secondHp > 0) {
                // First monster's turn
                BattleAction action1 = executeMonsterTurn(firstMonster, secondMonster, firstHp);
                record.addAction(action1);
                secondHp -= action1.getDamage();

                if (secondHp <= 0) {
                    record.complete(firstMonster.getId(), firstMonster.getPlayerId());
                    break;
                }

                // Second monster's turn
                BattleAction action2 = executeMonsterTurn(secondMonster, firstMonster, secondHp);
                record.addAction(action2);
                firstHp -= action2.getDamage();

                if (firstHp <= 0) {
                    record.complete(secondMonster.getId(), secondMonster.getPlayerId());
                    break;
                }

                // Reduce skill cooldowns
                firstMonster.reduceCooldowns();
                secondMonster.reduceCooldowns();
            }
        } catch (Exception e) {
            record.error();
            throw e;
        }
    }

    private BattleAction executeMonsterTurn(Monster attacker, Monster defender, double currentHp) {
        BattleAction action = new BattleAction();
        action.setMonsterId(attacker.getId());
        action.setMonsterName(attacker.getName());

        // Select the available skill with highest damage
        Skill selectedSkill = null;
        double maxDamage = 0;
        int selectedIndex = -1;

        for (int i = 0; i < attacker.getSkills().size(); i++) {
            Skill skill = attacker.getSkills().get(i);
            if (skill.isReady()) {
                double damage = calculateDamage(attacker, defender, skill);
                if (damage > maxDamage) {
                    maxDamage = damage;
                    selectedSkill = skill;
                    selectedIndex = i;
                }
            }
        }

        if (selectedSkill == null) {
            // If no skills are available, use normal attack
            action.setSkillName("Normal Attack");
            action.setDamage(calculateBasicAttack(attacker, defender));
        } else {
            selectedSkill.use();
            action.setSkillIndex(selectedIndex);
            action.setSkillName(selectedSkill.getName());
            action.setDamage(maxDamage);
        }

        return action;
    }

    private double calculateDamage(Monster attacker, Monster defender, Skill skill) {
        double attackStat = attacker.getStat(skill.getScalingStat());
        double baseDamage = skill.calculateDamage(attackStat);
        double defense = defender.getStat(StatType.DEF);
        return Math.max(0, baseDamage * (1 - defense / (defense + 100)));
    }

    private double calculateBasicAttack(Monster attacker, Monster defender) {
        double attack = attacker.getStat(StatType.ATK);
        double defense = defender.getStat(StatType.DEF);
        return Math.max(0, attack * (1 - defense / (defense + 100)));
    }

    private BattleResponse convertToResponse(BattleRecord record) {
        BattleResponse response = new BattleResponse();
        response.setBattleId(record.getId());
        response.setMonster1Id(record.getMonster1Id());
        response.setMonster2Id(record.getMonster2Id());
        response.setWinnerMonsterId(record.getWinnerMonsterId());
        response.setWinnerPlayerId(record.getWinnerPlayerId());
        response.setActions(record.getActions());
        response.setStatus(record.getStatus());
        response.setStartTime(record.getStartTime());
        response.setEndTime(record.getEndTime());
        return response;
    }
}