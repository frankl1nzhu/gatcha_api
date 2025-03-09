package com.gatcha.api.battle.service;

import com.gatcha.api.battle.dto.RoyalRumbleResult;
import com.gatcha.api.battle.repository.BattleLogRepository;
import com.gatcha.api.battle.service.impl.RoyalRumbleServiceImpl;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.model.Skill;
import com.gatcha.api.monster.service.MonsterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoyalRumbleServiceTest {

    @Mock
    private MonsterService monsterService;

    @Mock
    private BattleLogRepository battleLogRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RoyalRumbleServiceImpl royalRumbleService;

    private List<PlayerMonster> testMonsters;
    private List<Skill> testSkills;

    @BeforeEach
    void setUp() {
        // Set up test skills
        testSkills = new ArrayList<>();
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
        testSkills.add(skill1);

        // Set up test monsters
        testMonsters = new ArrayList<>();

        // Monster 1
        PlayerMonster monster1 = new PlayerMonster();
        monster1.setId("monster1");
        monster1.setUsername("testuser");
        monster1.setTemplateId("1");
        monster1.setElement("fire");
        monster1.setLevel(1);
        monster1.setExperience(0);
        monster1.setHp(100);
        monster1.setAtk(20);
        monster1.setDef(10);
        monster1.setVit(10);
        monster1.setSkills(new ArrayList<>(testSkills));
        monster1.setSkillPoints(3);
        testMonsters.add(monster1);

        // Monster 2
        PlayerMonster monster2 = new PlayerMonster();
        monster2.setId("monster2");
        monster2.setUsername("testuser");
        monster2.setTemplateId("2");
        monster2.setElement("water");
        monster2.setLevel(2);
        monster2.setExperience(0);
        monster2.setHp(90);
        monster2.setAtk(15);
        monster2.setDef(15);
        monster2.setVit(5);
        monster2.setSkills(new ArrayList<>(testSkills));
        monster2.setSkillPoints(3);
        testMonsters.add(monster2);

        // Monster 3
        PlayerMonster monster3 = new PlayerMonster();
        monster3.setId("monster3");
        monster3.setUsername("testuser");
        monster3.setTemplateId("3");
        monster3.setElement("wind");
        monster3.setLevel(3);
        monster3.setExperience(0);
        monster3.setHp(80);
        monster3.setAtk(25);
        monster3.setDef(5);
        monster3.setVit(15);
        monster3.setSkills(new ArrayList<>(testSkills));
        monster3.setSkillPoints(3);
        testMonsters.add(monster3);
    }

    @Test
    void startRoyalRumble() {
        // Arrange
        when(monsterService.getMonstersByUsername("testuser")).thenReturn(testMonsters);
        when(mongoTemplate.save(any(RoyalRumbleResult.class), eq("royalRumbles"))).thenReturn(new RoyalRumbleResult());
        when(monsterService.addExperience(anyString(), anyString(), anyInt())).thenReturn(testMonsters.get(0));

        // Act
        RoyalRumbleResult result = royalRumbleService.startRoyalRumble("testuser");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getRumbleDate());
        assertEquals(3, result.getParticipantIds().size());
        assertNotNull(result.getRounds());
        assertFalse(result.getRounds().isEmpty());
        assertNotNull(result.getWinner());
        assertTrue(result.getExperienceGained() > 0);

        // Verify calls
        verify(monsterService, times(1)).getMonstersByUsername("testuser");
        verify(mongoTemplate, times(1)).save(any(RoyalRumbleResult.class), eq("royalRumbles"));
        verify(monsterService, times(1)).addExperience(anyString(), eq("testuser"), anyInt());
    }

    @Test
    void startRoyalRumbleNotEnoughMonsters() {
        // Arrange
        when(monsterService.getMonstersByUsername("testuser")).thenReturn(testMonsters.subList(0, 2));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> royalRumbleService.startRoyalRumble("testuser"));
        assertEquals("At least 3 monsters are required to start a Royal Rumble", exception.getMessage());

        // Verify calls
        verify(monsterService, times(1)).getMonstersByUsername("testuser");
        verify(mongoTemplate, never()).save(any(), anyString());
        verify(monsterService, never()).addExperience(anyString(), anyString(), anyInt());
    }

    @Test
    void getExperienceGained() {
        // Arrange
        String rumbleId = "rumble1";

        // Act
        int experienceGained = royalRumbleService.getExperienceGained(rumbleId);

        // Assert
        assertEquals(0, experienceGained);
    }
}