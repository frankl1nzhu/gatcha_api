package com.gatcha.api.battle.service;

import com.gatcha.api.battle.model.BattleLog;
import com.gatcha.api.battle.repository.BattleLogRepository;
import com.gatcha.api.battle.service.impl.BattleServiceImpl;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.model.Skill;
import com.gatcha.api.monster.service.MonsterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BattleServiceTest {

    @Mock
    private BattleLogRepository battleLogRepository;

    @Mock
    private MonsterService monsterService;

    @InjectMocks
    private BattleServiceImpl battleService;

    private PlayerMonster monster1;
    private PlayerMonster monster2;
    private BattleLog battleLog;
    private List<Skill> skills;

    @BeforeEach
    void setUp() {
        // Set up test skills
        skills = new ArrayList<>();
        Skill skill1 = new Skill();
        skill1.setNum(1);
        skill1.setDmg(10);
        Skill.Ratio ratio1 = new Skill.Ratio();
        ratio1.setStat("atk");
        ratio1.setPercent(1.5);
        skill1.setRatio(ratio1);
        skill1.setCooldown(2);
        skill1.setLevel(1);
        skill1.setLvlMax(5);
        skills.add(skill1);

        // Set up test monster 1
        monster1 = new PlayerMonster();
        monster1.setId("monster1");
        monster1.setUsername("testuser");
        monster1.setTemplateId("1");
        monster1.setElement("fire");
        monster1.setLevel(1);
        monster1.setExperience(0);
        monster1.setHp(100);
        monster1.setAtk(20);
        monster1.setDef(10);
        monster1.setVit(10); // Higher speed, attacks first
        monster1.setSkills(new ArrayList<>(skills));
        monster1.setSkillPoints(3);

        // Set up test monster 2
        monster2 = new PlayerMonster();
        monster2.setId("monster2");
        monster2.setUsername("testuser");
        monster2.setTemplateId("2");
        monster2.setElement("water");
        monster2.setLevel(2); // Set to level 2 for experience calculation testing
        monster2.setExperience(0);
        monster2.setHp(90);
        monster2.setAtk(15);
        monster2.setDef(15);
        monster2.setVit(5); // Lower speed, attacks second
        monster2.setSkills(new ArrayList<>(skills));
        monster2.setSkillPoints(3);

        // Set up test battle log
        battleLog = new BattleLog();
        battleLog.setId("battle1");
        battleLog.setMonster1Id("monster1");
        battleLog.setMonster2Id("monster2");
        battleLog.setBattleDate(new Date());
        battleLog.setWinnerId("monster1");
        battleLog.setActions(new ArrayList<>());
    }

    @Test
    void battle() {
        // Arrange
        when(monsterService.getMonsterById("monster1", "testuser")).thenReturn(monster1);
        when(monsterService.getMonsterById("monster2", "testuser")).thenReturn(monster2);
        when(battleLogRepository.save(any(BattleLog.class))).thenAnswer(invocation -> {
            BattleLog savedLog = (BattleLog) invocation.getArgument(0);
            savedLog.setId("battle1");
            return savedLog;
        });
        when(monsterService.addExperience(anyString(), anyString(), anyInt())).thenReturn(monster1);

        // Act
        BattleLog result = battleService.battle("monster1", "monster2", "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("battle1", result.getId());
        assertEquals("monster1", result.getMonster1Id());
        assertEquals("monster2", result.getMonster2Id());
        assertNotNull(result.getActions());
        assertFalse(result.getActions().isEmpty());
        verify(monsterService, times(1)).getMonsterById("monster1", "testuser");
        verify(monsterService, times(1)).getMonsterById("monster2", "testuser");
        verify(battleLogRepository, times(1)).save(any(BattleLog.class));

        // Verify that the winning monster receives experience
        // Experience calculation formula: base experience(20) + defeated monster
        // level(2) * 10 = 40
        verify(monsterService, times(1)).addExperience(eq(result.getWinnerId()), eq("testuser"), anyInt());
    }

    @Test
    void battleWithExperienceGained() {
        // Arrange
        when(monsterService.getMonsterById("monster1", "testuser")).thenReturn(monster1);
        when(monsterService.getMonsterById("monster2", "testuser")).thenReturn(monster2);
        when(battleLogRepository.save(any(BattleLog.class))).thenAnswer(invocation -> {
            BattleLog savedLog = (BattleLog) invocation.getArgument(0);
            savedLog.setId("battle1");
            return savedLog;
        });
        when(monsterService.addExperience(anyString(), anyString(), anyInt())).thenReturn(monster1);

        // Act
        BattleLog result = battleService.battle("monster1", "monster2", "testuser");
        int experienceGained = battleService.getExperienceGained(result.getId());

        // Assert
        // Experience calculation formula: base experience(20) + defeated monster
        // level(2) * 10 = 40
        assertEquals(40, experienceGained);
        verify(monsterService, times(1)).addExperience(eq(result.getWinnerId()), eq("testuser"), eq(40));
    }

    @Test
    void getBattleById() {
        // Arrange
        when(battleLogRepository.findById("battle1")).thenReturn(Optional.of(battleLog));

        // Act
        BattleLog result = battleService.getBattleById("battle1");

        // Assert
        assertNotNull(result);
        assertEquals("battle1", result.getId());
        assertEquals("monster1", result.getMonster1Id());
        assertEquals("monster2", result.getMonster2Id());
        verify(battleLogRepository, times(1)).findById("battle1");
    }

    @Test
    void getBattleByIdNotFound() {
        // Arrange
        when(battleLogRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> battleService.getBattleById("nonexistent"));
        verify(battleLogRepository, times(1)).findById("nonexistent");
    }

    @Test
    void getBattlesByMonsterId() {
        // Arrange
        List<BattleLog> logs = Arrays.asList(battleLog);
        when(battleLogRepository.findByMonster1IdOrMonster2Id("monster1", "monster1")).thenReturn(logs);

        // Act
        List<BattleLog> result = battleService.getBattlesByMonsterId("monster1");

        // Assert
        assertEquals(1, result.size());
        assertEquals("battle1", result.get(0).getId());
        verify(battleLogRepository, times(1)).findByMonster1IdOrMonster2Id("monster1", "monster1");
    }

    @Test
    void getExperienceGainedNonExistent() {
        // Act
        int experienceGained = battleService.getExperienceGained("nonexistent");

        // Assert
        assertEquals(0, experienceGained);
    }
}