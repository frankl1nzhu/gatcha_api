package com.gatcha.api.service;

import com.gatcha.api.dto.SummonResponse;
import com.gatcha.api.dto.PlayerResponse;
import com.gatcha.api.model.Monster;
import com.gatcha.api.model.MonsterTemplate;
import com.gatcha.api.model.Player;
import com.gatcha.api.model.SummonRecord;
import com.gatcha.api.model.ElementType;
import com.gatcha.api.model.StatType;
import com.gatcha.api.repository.MonsterRepository;
import com.gatcha.api.repository.MonsterTemplateRepository;
import com.gatcha.api.repository.PlayerRepository;
import com.gatcha.api.repository.SummonRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SummonServiceTest {

    @Mock
    private MonsterTemplateRepository monsterTemplateRepository;

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private SummonRecordRepository summonRecordRepository;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private SummonService summonService;

    private Player player;
    private MonsterTemplate monsterTemplate;
    private static final String USERNAME = "testuser";
    private static final String PLAYER_ID = "1";

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(PLAYER_ID);
        player.setUsername(USERNAME);
        player.setMaxMonsters(10);

        monsterTemplate = new MonsterTemplate();
        monsterTemplate.setId("template1");
        monsterTemplate.setName("Test Monster");
        monsterTemplate.setElementType(ElementType.FIRE);
        monsterTemplate.setSummonRate(0.5);
    }

    @Test
    void summonMonster_WhenPlayerHasSpace_ShouldSummonSuccessfully() {
        Map<StatType, Double> baseStats = new HashMap<>();
        baseStats.put(StatType.HP, 100.0);
        baseStats.put(StatType.ATK, 10.0);
        baseStats.put(StatType.DEF, 10.0);
        baseStats.put(StatType.SPD, 10.0);
        monsterTemplate.setBaseStats(baseStats);

        Monster monster = new Monster();
        monster.setId("generatedId");
        monster.setPlayerId(PLAYER_ID);
        monster.setName(monsterTemplate.getName());
        monster.setElementType(monsterTemplate.getElementType());
        monster.initializeStats(baseStats);

        when(monsterTemplateRepository.findAllByOrderBySummonRateDesc()).thenReturn(Arrays.asList(monsterTemplate));
        when(monsterRepository.countByPlayerId(PLAYER_ID)).thenReturn(5L);
        when(monsterRepository.save(any(Monster.class))).thenReturn(monster);
        when(summonRecordRepository.save(any(SummonRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(playerService.addMonster(anyString(), anyString())).thenReturn(new PlayerResponse());

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

        assertThrows(IllegalStateException.class, () -> summonService.summonMonster(PLAYER_ID));
        verify(monsterRepository, never()).save(any());
        verify(summonRecordRepository, never()).save(any());
        verify(playerService, never()).addMonster(anyString(), anyString());
    }

    @Test
    void getPlayerSummons_ShouldReturnSummonHistory() {
        when(summonRecordRepository.findByPlayerIdOrderByCreatedAtDesc(PLAYER_ID))
                .thenReturn(Arrays.asList(createTestSummon()));

        var responses = summonService.getPlayerSummons(PLAYER_ID);

        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        verify(summonRecordRepository).findByPlayerIdOrderByCreatedAtDesc(PLAYER_ID);
    }

    private SummonRecord createTestSummon() {
        SummonRecord summon = new SummonRecord();
        summon.setId("summon1");
        summon.setPlayerId(PLAYER_ID);
        summon.setMonsterId("monster1");
        summon.setMonsterName("Test Monster");
        summon.setElementType(ElementType.FIRE);
        summon.setCreatedAt(System.currentTimeMillis());
        return summon;
    }
}