package com.roguera.gamelogic;

import com.roguera.creatures.Boss;
import com.roguera.creatures.Mob;
import com.roguera.resources.GetRandom;

public class MobFactory {
	public static Mob newMob() {
		int randomMobHP = GetRandom.getRandomMobHP();
		return new Mob(randomMobHP, GetRandom.getRandomMobName());
	}

	public static Mob newBoss() {
		return new Boss();
	}
}
