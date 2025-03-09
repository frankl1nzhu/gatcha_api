package com.gatcha.api.battle.service;

import com.gatcha.api.battle.dto.RoyalRumbleResult;

/**
 * Royal Rumble Service Interface
 * All monsters randomly use a skill on another monster until only one monster
 * remains
 */
public interface RoyalRumbleService {

    /**
     * Start a royal rumble
     * 
     * @param username username
     * @return royal rumble result
     */
    RoyalRumbleResult startRoyalRumble(String username);

    /**
     * Get experience reward from the most recent royal rumble
     * 
     * @param rumbleId royal rumble ID
     * @return experience reward
     */
    int getExperienceGained(String rumbleId);
}