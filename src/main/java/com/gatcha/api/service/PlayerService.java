package com.gatcha.api.service;

import com.gatcha.api.dto.PlayerResponse;
import com.gatcha.api.model.Player;
import com.gatcha.api.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerResponse getPlayerProfile(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Player not found"));
        return convertToResponse(player);
    }

    public PlayerResponse addExperience(String username, double experience) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Player not found"));

        player.addExperience(experience);
        player = playerRepository.save(player);
        return convertToResponse(player);
    }

    public PlayerResponse addMonster(String username, String monsterId) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Player not found"));

        player.addMonster(monsterId);
        player = playerRepository.save(player);
        return convertToResponse(player);
    }

    public PlayerResponse removeMonster(String username, String monsterId) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Player not found"));

        player.removeMonster(monsterId);
        player = playerRepository.save(player);
        return convertToResponse(player);
    }

    private PlayerResponse convertToResponse(Player player) {
        PlayerResponse response = new PlayerResponse();
        response.setId(player.getId());
        response.setUsername(player.getUsername());
        response.setLevel(player.getLevel());
        response.setExperience(player.getExperience());
        response.setExperienceToNextLevel(player.getExperienceToNextLevel());
        response.setMaxMonsters(player.getMaxMonsters());
        response.setMonsterIds(player.getMonsterIds());
        response.setUpdatedAt(player.getUpdatedAt());
        return response;
    }
}