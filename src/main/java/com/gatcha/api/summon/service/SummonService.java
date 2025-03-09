package com.gatcha.api.summon.service;

import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.summon.model.SummonLog;

import java.util.List;

public interface SummonService {
    PlayerMonster summon(String username);

    List<SummonLog> getSummonHistory(String username);

    void reprocessFailedSummons();
}