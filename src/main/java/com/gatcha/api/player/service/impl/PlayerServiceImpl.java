package com.gatcha.api.player.service.impl;

import com.gatcha.api.auth.model.User;
import com.gatcha.api.auth.repository.UserRepository;
import com.gatcha.api.player.service.PlayerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final UserRepository userRepository;

    public PlayerServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<String> getMonsters(String username) {
        User user = getProfile(username);
        return user.getMonsters();
    }

    @Override
    public int getLevel(String username) {
        User user = getProfile(username);
        return user.getLevel();
    }

    @Override
    public User addExperience(String username, int experience) {
        User user = getProfile(username);
        user.addExperience(experience);
        return userRepository.save(user);
    }

    @Override
    public User levelUp(String username) {
        User user = getProfile(username);
        if (user.getExperience() >= user.getMaxExperience() && user.getLevel() < 50) {
            user.addExperience(user.getMaxExperience()); // Trigger level up
            return userRepository.save(user);
        }
        throw new RuntimeException("Cannot level up, not enough experience or already at max level");
    }

    @Override
    public boolean addMonster(String username, String monsterId) {
        User user = getProfile(username);
        if (user.canAddMonster()) {
            user.getMonsters().add(monsterId);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeMonster(String username, String monsterId) {
        User user = getProfile(username);
        boolean removed = user.getMonsters().remove(monsterId);
        if (removed) {
            userRepository.save(user);
        }
        return removed;
    }
}