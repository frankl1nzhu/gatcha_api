package com.gatcha.api.service;

import com.gatcha.api.dto.BattleResponse;
import com.gatcha.api.model.BattleRecord;
import com.gatcha.api.model.Monster;
import com.gatcha.api.model.Player;
import com.gatcha.api.repository.BattleRecordRepository;
import com.gatcha.api.repository.MonsterRepository;
import com.gatcha.api.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BattleServiceTest {

    @Mock
    private BattleRecordRepository battleRecordRepository;

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private BattleService battleService;

    private Player player;
    private Monster playerMonster;
    private Monster opponentMonster;
    private BattleRecord battleRecord;
    private static final String USERNAME = "testuser";
    private static final String PLAYER_ID = "1";
    private static final String MONSTER_ID = "monster1";
    private static final String OPPONENT_MONSTER_ID = "monster2";
    private static final String BATTLE_ID = "battle1";

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(PLAYER_ID);
        player.setUsername(USERNAME);

        playerMonster = new Monster();
        playerMonster.setId(MONSTER_ID);
        playerMonster.setPlayerId(PLAYER_ID);
        playerMonster.setLevel(1);

        opponentMonster = new Monster();
        opponentMonster.setId(OPPONENT_MONSTER_ID);
        opponentMonster.setPlayerId("opponent_id");
        opponentMonster.setLevel(1);

        battleRecord = new BattleRecord();
        battleRecord.setId(BATTLE_ID);
        battleRecord.setMonster1Id(MONSTER_ID);
        battleRecord.setMonster2Id(OPPONENT_MONSTER_ID);
        battleRecord.setStatus(BattleRecord.BattleStatus.IN_PROGRESS);
        battleRecord.setStartTime(System.currentTimeMillis());
    }

    @Test
    void startBattle_ShouldCreateNewBattle() {
        when(monsterRepository.findByIdAndPlayerId(MONSTER_ID, PLAYER_ID)).thenReturn(Optional.of(playerMonster));
        when(monsterRepository.findById(OPPONENT_MONSTER_ID)).thenReturn(Optional.of(opponentMonster));
        when(battleRecordRepository.save(any(BattleRecord.class))).thenAnswer(i -> {
            BattleRecord b = i.getArgument(0);
            b.setId(BATTLE_ID);
            b.setStatus(BattleRecord.BattleStatus.IN_PROGRESS);
            return b;
        });

        BattleResponse response = battleService.startBattle(PLAYER_ID, MONSTER_ID, OPPONENT_MONSTER_ID);

        assertNotNull(response);
        assertEquals(BATTLE_ID, response.getBattleId());
        assertEquals(BattleRecord.BattleStatus.IN_PROGRESS, response.getStatus());
        verify(battleRecordRepository).save(any(BattleRecord.class));
    }

    @Test
    void getPlayerBattles_ShouldReturnBattleList() {
        when(battleRecordRepository.findByPlayer1IdOrPlayer2IdOrderByStartTimeDesc(USERNAME, USERNAME))
                .thenReturn(Arrays.asList(battleRecord));

        var responses = battleService.getPlayerBattles(USERNAME);

        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(BATTLE_ID, responses.get(0).getBattleId());
        verify(battleRecordRepository).findByPlayer1IdOrPlayer2IdOrderByStartTimeDesc(USERNAME, USERNAME);
    }

    @Test
    void getBattle_ShouldReturnBattleDetails() {
        when(battleRecordRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battleRecord));

        BattleResponse response = battleService.getBattle(BATTLE_ID);

        assertNotNull(response);
        assertEquals(BATTLE_ID, response.getBattleId());
        assertEquals(MONSTER_ID, response.getMonster1Id());
        assertEquals(OPPONENT_MONSTER_ID, response.getMonster2Id());
    }
}