package com.gatcha.api.service;

import com.gatcha.api.dto.MonsterResponse;
import com.gatcha.api.model.Monster;
import com.gatcha.api.model.MonsterStats;
import com.gatcha.api.model.Player;
import com.gatcha.api.model.Skill;
import com.gatcha.api.repository.MonsterRepository;
import com.gatcha.api.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonsterServiceTest {

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private MonsterService monsterService;

    private Player player;
    private Monster monster;
    private static final String USERNAME = "testuser";
    private static final String PLAYER_ID = "1";
    private static final String MONSTER_ID = "1";

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(PLAYER_ID);
        player.setUsername(USERNAME);

        monster = new Monster();
        monster.setId(MONSTER_ID);
        monster.setPlayerId(PLAYER_ID);
        monster.setLevel(1);
        monster.setExperience(0);
        monster.setExperienceToNextLevel(50);
        monster.setSkillPoints(0);

        MonsterStats stats = new MonsterStats();
        stats.setHp(100);
        stats.setAtk(10);
        stats.setDef(10);
        stats.setSpd(10);
        monster.setStats(stats);
    }

    @Test
    void getPlayerMonsters_ShouldReturnMonsterList() {
        when(playerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(player));
        when(monsterRepository.findByPlayerId(PLAYER_ID)).thenReturn(Arrays.asList(monster));

        List<MonsterResponse> responses = monsterService.getPlayerMonsters(USERNAME);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(monsterRepository).findByPlayerId(PLAYER_ID);
    }

    @Test
    void getMonster_WhenValidAccess_ShouldReturnMonster() {
        when(playerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(player));
        when(monsterRepository.findById(MONSTER_ID)).thenReturn(Optional.of(monster));

        MonsterResponse response = monsterService.getMonster(USERNAME, MONSTER_ID);

        assertNotNull(response);
        assertEquals(MONSTER_ID, response.getId());
    }

    @Test
    void getMonster_WhenInvalidAccess_ShouldThrowException() {
        Monster otherMonster = new Monster();
        otherMonster.setId(MONSTER_ID);
        otherMonster.setPlayerId("other_player_id");

        when(playerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(player));
        when(monsterRepository.findById(MONSTER_ID)).thenReturn(Optional.of(otherMonster));

        assertThrows(IllegalArgumentException.class,
                () -> monsterService.getMonster(USERNAME, MONSTER_ID));
    }

    @Test
    void addExperience_WhenEnoughForLevelUp_ShouldLevelUp() {
        when(playerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(player));
        when(monsterRepository.findById(MONSTER_ID)).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any(Monster.class))).thenReturn(monster);

        MonsterResponse response = monsterService.addExperience(USERNAME, MONSTER_ID, 100);

        assertNotNull(response);
        assertTrue(monster.getLevel() > 1);
        assertTrue(monster.getSkillPoints() > 0);
        verify(monsterRepository).save(monster);
    }

    @Test
    void upgradeSkill_WhenValidAndHasPoints_ShouldUpgradeSkill() {
        monster.setSkillPoints(1);
        Skill skill = new Skill();
        skill.setLevel(1);
        skill.setMaxLevel(5);
        monster.setSkills(Arrays.asList(skill));

        when(playerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(player));
        when(monsterRepository.findById(MONSTER_ID)).thenReturn(Optional.of(monster));
        when(monsterRepository.save(any(Monster.class))).thenReturn(monster);

        MonsterResponse response = monsterService.upgradeSkill(USERNAME, MONSTER_ID, 0);

        assertNotNull(response);
        assertEquals(0, monster.getSkillPoints());
        assertEquals(2, monster.getSkills().get(0).getLevel());
        verify(monsterRepository).save(monster);
    }

    @Test
    void upgradeSkill_WhenNoPoints_ShouldThrowException() {
        monster.setSkillPoints(0);

        when(playerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(player));
        when(monsterRepository.findById(MONSTER_ID)).thenReturn(Optional.of(monster));

        assertThrows(IllegalArgumentException.class,
                () -> monsterService.upgradeSkill(USERNAME, MONSTER_ID, 0));
    }
}