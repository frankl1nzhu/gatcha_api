package com.gatcha.api.monster.service;

import com.gatcha.api.monster.model.MonsterTemplate;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.model.Skill;
import com.gatcha.api.monster.repository.MonsterTemplateRepository;
import com.gatcha.api.monster.repository.PlayerMonsterRepository;
import com.gatcha.api.monster.service.impl.MonsterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonsterServiceTest {

    @Mock
    private PlayerMonsterRepository playerMonsterRepository;

    @Mock
    private MonsterTemplateRepository monsterTemplateRepository;

    @InjectMocks
    private MonsterServiceImpl monsterService;

    private PlayerMonster testMonster;
    private MonsterTemplate testTemplate;
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

        Skill skill2 = new Skill();
        skill2.setNum(2);
        skill2.setDmg(20);
        Skill.Ratio ratio2 = new Skill.Ratio();
        ratio2.setStat("atk");
        ratio2.setPercent(2.0);
        skill2.setRatio(ratio2);
        skill2.setCooldown(3);
        skill2.setLevel(0);
        skill2.setLvlMax(3);

        testSkills.add(skill1);
        testSkills.add(skill2);

        // Set up test monster
        testMonster = new PlayerMonster();
        testMonster.setId("monster1");
        testMonster.setUsername("testuser");
        testMonster.setTemplateId("1");
        testMonster.setElement("fire");
        testMonster.setLevel(1);
        testMonster.setExperience(0);
        testMonster.setHp(100);
        testMonster.setAtk(20);
        testMonster.setDef(10);
        testMonster.setVit(5);
        testMonster.setSkills(new ArrayList<>(testSkills));
        testMonster.setSkillPoints(3);

        // Set up test template
        testTemplate = new MonsterTemplate();
        testTemplate.setId(1);
        testTemplate.setElement("fire");
        testTemplate.setHp(100);
        testTemplate.setAtk(20);
        testTemplate.setDef(10);
        testTemplate.setVit(5);
        testTemplate.setSkills(new ArrayList<>(testSkills));
        testTemplate.setLootRate(0.5);
    }

    @Test
    void getMonstersByUsername() {
        // Prepare
        List<PlayerMonster> monsters = Arrays.asList(testMonster);
        when(playerMonsterRepository.findByUsername("testuser")).thenReturn(monsters);

        // Execute
        List<PlayerMonster> result = monsterService.getMonstersByUsername("testuser");

        // Verify
        assertEquals(1, result.size());
        assertEquals("monster1", result.get(0).getId());
        verify(playerMonsterRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getMonsterById() {
        // Prepare
        when(playerMonsterRepository.findByIdAndUsername("monster1", "testuser")).thenReturn(Optional.of(testMonster));

        // Execute
        PlayerMonster result = monsterService.getMonsterById("monster1", "testuser");

        // Verify
        assertNotNull(result);
        assertEquals("monster1", result.getId());
        assertEquals("testuser", result.getUsername());
        verify(playerMonsterRepository, times(1)).findByIdAndUsername("monster1", "testuser");
    }

    @Test
    void getMonsterByIdNotFound() {
        // Prepare
        when(playerMonsterRepository.findByIdAndUsername("nonexistent", "testuser")).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> monsterService.getMonsterById("nonexistent", "testuser"));
        verify(playerMonsterRepository, times(1)).findByIdAndUsername("nonexistent", "testuser");
    }

    @Test
    void addExperience() {
        // Prepare
        when(playerMonsterRepository.findByIdAndUsername("monster1", "testuser")).thenReturn(Optional.of(testMonster));
        when(playerMonsterRepository.save(any(PlayerMonster.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        PlayerMonster result = monsterService.addExperience("monster1", "testuser", 50);

        // Verify
        assertNotNull(result);
        assertEquals(50, result.getExperience());
        verify(playerMonsterRepository, times(1)).save(any(PlayerMonster.class));
    }

    @Test
    void upgradeSkill() {
        // Prepare
        when(playerMonsterRepository.findByIdAndUsername("monster1", "testuser")).thenReturn(Optional.of(testMonster));
        when(playerMonsterRepository.save(any(PlayerMonster.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Record skill state before upgrade
        int originalDmg = testMonster.getSkills().get(1).getDmg();
        double originalPercent = testMonster.getSkills().get(1).getRatio().getPercent();
        int originalCooldown = testMonster.getSkills().get(1).getCooldown();
        int originalSkillPoints = testMonster.getSkillPoints();

        // Execute
        PlayerMonster result = monsterService.upgradeSkill("monster1", "testuser", 2);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getSkills().get(1).getLevel()); // Skill 2's level should increase from 0 to 1
        assertEquals(originalSkillPoints - 1, result.getSkillPoints()); // Skill points should decrease by 1

        // Verify skill effect enhancement
        assertTrue(result.getSkills().get(1).getDmg() > originalDmg); // Damage should increase
        assertTrue(result.getSkills().get(1).getRatio().getPercent() > originalPercent); // Ratio should increase

        // If skill level is a multiple of 2, cooldown should decrease
        if (result.getSkills().get(1).getLevel() % 2 == 0 && originalCooldown > 0) {
            assertTrue(result.getSkills().get(1).getCooldown() < originalCooldown);
        } else {
            assertEquals(originalCooldown, result.getSkills().get(1).getCooldown());
        }

        verify(playerMonsterRepository, times(1)).save(any(PlayerMonster.class));
    }

    @Test
    void createMonsterFromTemplate() {
        // Prepare
        when(monsterTemplateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(playerMonsterRepository.save(any(PlayerMonster.class))).thenAnswer(invocation -> {
            PlayerMonster savedMonster = (PlayerMonster) invocation.getArgument(0);
            savedMonster.setId("newmonster1");
            return savedMonster;
        });

        // Execute
        PlayerMonster result = monsterService.createMonsterFromTemplate(1, "testuser");

        // Verify
        assertNotNull(result);
        assertEquals("newmonster1", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("1", result.getTemplateId());
        assertEquals("fire", result.getElement());
        assertEquals(1, result.getLevel());
        assertEquals(0, result.getExperience());
        assertEquals(100, result.getHp());
        assertEquals(20, result.getAtk());
        assertEquals(10, result.getDef());
        assertEquals(5, result.getVit());
        assertEquals(3, result.getSkillPoints());
        assertEquals(2, result.getSkills().size());
        assertEquals(0, result.getSkills().get(0).getLevel()); // New monster's skill level should be 0
        verify(monsterTemplateRepository, times(1)).findById(1);
        verify(playerMonsterRepository, times(1)).save(any(PlayerMonster.class));
    }

    @Test
    void createMonsterFromTemplateNotFound() {
        // Prepare
        when(monsterTemplateRepository.findById(999)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> monsterService.createMonsterFromTemplate(999, "testuser"));
        verify(monsterTemplateRepository, times(1)).findById(999);
        verify(playerMonsterRepository, never()).save(any(PlayerMonster.class));
    }
}