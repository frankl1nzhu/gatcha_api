package com.gatcha.api;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

@SuppressWarnings("deprecation")
@RunWith(JUnitPlatform.class)
@SelectPackages({
        "com.gatcha.api.auth.service",
        "com.gatcha.api.battle.service",
        "com.gatcha.api.monster.service",
        "com.gatcha.api.player.service",
        "com.gatcha.api.summon.service",
        "com.gatcha.api.controller"
})
public class GatchaApiTestSuite {
}