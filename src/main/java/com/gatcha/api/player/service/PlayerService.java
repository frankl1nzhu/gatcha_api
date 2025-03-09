package com.gatcha.api.player.service;

import com.gatcha.api.auth.model.User;

import java.util.List;

public interface PlayerService {
    User getProfile(String username);

    List<String> getMonsters(String username);

    int getLevel(String username);

    User addExperience(String username, int experience);

    User levelUp(String username);

    boolean addMonster(String username, String monsterId);

    boolean removeMonster(String username, String monsterId);
}