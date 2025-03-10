package com.gatcha.api.battle.service;

import com.gatcha.api.battle.dto.RoyalRumbleResult;
import java.util.List;

/**
 * Royal Rumble Service Interface
 * All monsters randomly use a skill on another monster until only one monster
 * remains
 */
public interface RoyalRumbleService {

    /**
     * Start a royal rumble with all user's monsters
     * 
     * @param username username
     * @return royal rumble result
     */
    RoyalRumbleResult startRoyalRumble(String username);

    /**
     * Start a royal rumble with specific monsters
     * 
     * @param username   username
     * @param monsterIds list of monster IDs to participate
     * @return royal rumble result
     */
    RoyalRumbleResult startRoyalRumble(String username, List<String> monsterIds);

    /**
     * Get experience reward from the most recent royal rumble
     * 
     * @param rumbleId royal rumble ID
     * @return experience reward
     */
    int getExperienceGained(String rumbleId);

    /**
     * Get all royal rumble history
     * 
     * @return list of royal rumble results
     */
    List<RoyalRumbleResult> getAllRumbles();
}