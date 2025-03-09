package com.gatcha.api.summon.service;

import com.gatcha.api.monster.model.MonsterTemplate;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.model.Skill;
import com.gatcha.api.monster.repository.MonsterTemplateRepository;
import com.gatcha.api.monster.service.MonsterService;
import com.gatcha.api.player.service.PlayerService;
import com.gatcha.api.summon.model.SummonLog;
import com.gatcha.api.summon.repository.SummonLogRepository;
import com.gatcha.api.summon.service.impl.SummonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SummonServiceTest {

    @Mock
    private MonsterTemplateRepository monsterTemplateRepository;

    @Mock
    private SummonLogRepository summonLogRepository;

    @Mock
    private MonsterService monsterService;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private SummonServiceImpl summonService;

    private MonsterTemplate template1;
    private MonsterTemplate template2;
    private PlayerMonster playerMonster;
    private SummonLog summonLog;
    private List<MonsterTemplate> templates;

    @BeforeEach
    void setUp() {
        // Set up test skills
        List<Skill> skills = new ArrayList<>();
        Skill skill = new Skill();
        skill.setNum(1);
        skill.setDmg(10);
        Skill.Ratio ratio = new Skill.Ratio();
        ratio.setStat("atk");
        ratio.setPercent(1.5);
        skill.setRatio(ratio);
        skill.setCooldown(2);
        skill.setLevel(0);
        skill.setLvlMax(3);
        skills.add(skill);

        // Set up test templates
        template1 = new MonsterTemplate();
        template1.setId(1);
        template1.setElement("fire");
        template1.setHp(100);
        template1.setAtk(20);
        template1.setDef(10);
        template1.setVit(5);
        template1.setSkills(new ArrayList<>(skills));
        template1.setLootRate(0.7);

        template2 = new MonsterTemplate();
        template2.setId(2);
        template2.setElement("water");
        template2.setHp(90);
        template2.setAtk(15);
        template2.setDef(15);
        template2.setVit(8);
        template2.setSkills(new ArrayList<>(skills));
        template2.setLootRate(0.3);

        templates = Arrays.asList(template1, template2);

        // Set up test player monster
        playerMonster = new PlayerMonster();
        playerMonster.setId("monster1");
        playerMonster.setUsername("testuser");
        playerMonster.setTemplateId("1");
        playerMonster.setElement("fire");
        playerMonster.setLevel(1);
        playerMonster.setExperience(0);
        playerMonster.setHp(100);
        playerMonster.setAtk(20);
        playerMonster.setDef(10);
        playerMonster.setVit(5);
        playerMonster.setSkills(new ArrayList<>(skills));
        playerMonster.setSkillPoints(3);

        // Set up test summon log
        summonLog = new SummonLog();
        summonLog.setId("summon1");
        summonLog.setUsername("testuser");
        summonLog.setTemplateId("1");
        summonLog.setMonsterId("monster1");
        summonLog.setProcessed(true);
    }

    @Test
    void summonSuccess() {
        // Prepare
        when(monsterTemplateRepository.findAll()).thenReturn(templates);
        when(summonLogRepository.save(any(SummonLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(monsterService.createMonsterFromTemplate(anyInt(), anyString())).thenReturn(playerMonster);
        when(playerService.addMonster(anyString(), anyString())).thenReturn(true);

        // Execute
        PlayerMonster result = summonService.summon("testuser");

        // Verify
        assertNotNull(result);
        assertEquals("monster1", result.getId());
        assertEquals("testuser", result.getUsername());
        verify(monsterTemplateRepository, times(1)).findAll();
        verify(summonLogRepository, times(2)).save(any(SummonLog.class)); // Saved twice: initial and update
        verify(monsterService, times(1)).createMonsterFromTemplate(anyInt(), eq("testuser"));
        verify(playerService, times(1)).addMonster("testuser", "monster1");
    }

    @Test
    void summonFailAddMonster() {
        // Prepare
        when(monsterTemplateRepository.findAll()).thenReturn(templates);
        when(summonLogRepository.save(any(SummonLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(monsterService.createMonsterFromTemplate(anyInt(), anyString())).thenReturn(playerMonster);
        when(playerService.addMonster(anyString(), anyString())).thenReturn(false);

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> summonService.summon("testuser"));
        verify(monsterTemplateRepository, times(1)).findAll();
        verify(summonLogRepository, times(2)).save(any(SummonLog.class)); // Saved twice: initial and failure update
        verify(monsterService, times(1)).createMonsterFromTemplate(anyInt(), eq("testuser"));
        verify(playerService, times(1)).addMonster("testuser", "monster1");
    }

    @Test
    void summonFailCreateMonster() {
        // Prepare
        when(monsterTemplateRepository.findAll()).thenReturn(templates);
        when(summonLogRepository.save(any(SummonLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(monsterService.createMonsterFromTemplate(anyInt(), anyString()))
                .thenThrow(new RuntimeException("Test exception"));

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> summonService.summon("testuser"));
        verify(monsterTemplateRepository, times(1)).findAll();
        verify(summonLogRepository, times(2)).save(any(SummonLog.class)); // Saved twice: initial and failure update
        verify(monsterService, times(1)).createMonsterFromTemplate(anyInt(), eq("testuser"));
        verify(playerService, never()).addMonster(anyString(), anyString());
    }

    @Test
    void getSummonHistory() {
        // Prepare
        List<SummonLog> logs = Arrays.asList(summonLog);
        when(summonLogRepository.findByUsername("testuser")).thenReturn(logs);

        // Execute
        List<SummonLog> result = summonService.getSummonHistory("testuser");

        // Verify
        assertEquals(1, result.size());
        assertEquals("summon1", result.get(0).getId());
        assertEquals("testuser", result.get(0).getUsername());
        verify(summonLogRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void reprocessFailedSummons() {
        // Prepare
        SummonLog failedLog1 = new SummonLog();
        failedLog1.setId("failed1");
        failedLog1.setUsername("testuser");
        failedLog1.setTemplateId("1");
        failedLog1.setProcessed(false);

        SummonLog failedLog2 = new SummonLog();
        failedLog2.setId("failed2");
        failedLog2.setUsername("testuser");
        failedLog2.setTemplateId("2");
        failedLog2.setProcessed(false);

        List<SummonLog> failedLogs = Arrays.asList(failedLog1, failedLog2);

        when(summonLogRepository.findByProcessed(false)).thenReturn(failedLogs);
        when(monsterService.createMonsterFromTemplate(eq(1), anyString())).thenReturn(playerMonster);
        when(monsterService.createMonsterFromTemplate(eq(2), anyString()))
                .thenThrow(new RuntimeException("Test exception"));
        when(playerService.addMonster(anyString(), anyString())).thenReturn(true);
        when(summonLogRepository.save(any(SummonLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        summonService.reprocessFailedSummons();

        // Verify
        verify(summonLogRepository, times(1)).findByProcessed(false);
        verify(monsterService, times(2)).createMonsterFromTemplate(anyInt(), anyString());
        verify(playerService, times(1)).addMonster(anyString(), anyString());
        verify(summonLogRepository, times(1)).save(any(SummonLog.class)); // Only one successful update
    }
}