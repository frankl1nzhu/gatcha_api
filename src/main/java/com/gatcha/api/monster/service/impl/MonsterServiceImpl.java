package com.gatcha.api.monster.service.impl;

import com.gatcha.api.monster.model.MonsterTemplate;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.model.Skill;
import com.gatcha.api.monster.repository.MonsterTemplateRepository;
import com.gatcha.api.monster.repository.PlayerMonsterRepository;
import com.gatcha.api.monster.service.MonsterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonsterServiceImpl implements MonsterService {

    private final PlayerMonsterRepository playerMonsterRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;

    public MonsterServiceImpl(PlayerMonsterRepository playerMonsterRepository,
            MonsterTemplateRepository monsterTemplateRepository) {
        this.playerMonsterRepository = playerMonsterRepository;
        this.monsterTemplateRepository = monsterTemplateRepository;
    }

    @Override
    public List<PlayerMonster> getMonstersByUsername(String username) {
        return playerMonsterRepository.findByUsername(username);
    }

    @Override
    public PlayerMonster getMonsterById(String id, String username) {
        return playerMonsterRepository.findByIdAndUsername(id, username)
                .orElseThrow(() -> new RuntimeException("Monster not found"));
    }

    @Override
    public PlayerMonster addExperience(String id, String username, int experience) {
        PlayerMonster monster = getMonsterById(id, username);
        monster.addExperience(experience);
        return playerMonsterRepository.save(monster);
    }

    @Override
    public PlayerMonster upgradeSkill(String id, String username, int skillNum) {
        PlayerMonster monster = getMonsterById(id, username);
        monster.upgradeSkill(skillNum);
        return playerMonsterRepository.save(monster);
    }

    @Override
    public PlayerMonster createMonsterFromTemplate(Integer templateId, String username) {
        MonsterTemplate template = monsterTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Monster template not found"));

        PlayerMonster monster = new PlayerMonster();
        monster.setUsername(username);
        monster.setTemplateId(String.valueOf(templateId));
        monster.setElement(template.getElement());
        monster.setLevel(1);
        monster.setExperience(0);
        monster.setHp(template.getHp());
        monster.setAtk(template.getAtk());
        monster.setDef(template.getDef());
        monster.setVit(template.getVit());
        monster.setSkillPoints(3); // Initially provide 3 skill points for testing

        // Copy skills, but initialize level to 0
        List<Skill> skills = template.getSkills().stream()
                .map(skill -> {
                    Skill newSkill = new Skill();
                    newSkill.setNum(skill.getNum());
                    newSkill.setDmg(skill.getDmg());
                    newSkill.setRatio(skill.getRatio());
                    newSkill.setCooldown(skill.getCooldown());
                    newSkill.setLevel(0);
                    newSkill.setLvlMax(skill.getLvlMax());
                    return newSkill;
                })
                .collect(Collectors.toList());

        monster.setSkills(skills);

        return playerMonsterRepository.save(monster);
    }
}