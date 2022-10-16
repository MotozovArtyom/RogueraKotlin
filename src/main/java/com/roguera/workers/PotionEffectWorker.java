package com.roguera.workers;

import com.roguera.gamemap.Dungeon;
import com.roguera.items.Potion;
import com.roguera.view.Draw;
import com.roguera.view.ViewObjects;
import com.roguera.view.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PotionEffectWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(PotionEffectWorker.class);

	private final int EFFECT_SECONDS_MINIMUM_TIME = 10;

	private final Potion potion;

	private final String type;

	public PotionEffectWorker(Potion potion, String type) {
		this.potion = potion;
		this.type = type;
	}

	@Override
	public void run() {
		try {
			switch (type) {
				case "HEAL":
					Dungeon.player.getPlayerData().setHP(Math.min(Dungeon.player.getPlayerData().getMaxHP(), Dungeon.player.getHP() + potion.getAmount()));
					break;
				case "ATK_BUF":
					Dungeon.player.getPlayerData().set_atkPotionBonus(Dungeon.player.getPlayerData().get_atkPotionBonus() + potion.getAmount());
					startTimer();
					Dungeon.player.getPlayerData().set_atkPotionBonus(Dungeon.player.getPlayerData().get_atkPotionBonus() - potion.getAmount());
					break;
				case "DEF_BUF":
					Dungeon.player.getPlayerData().set_defPotionBonus(Dungeon.player.getPlayerData().get_defPotionBonus() + potion.getAmount());
					startTimer();
					Dungeon.player.getPlayerData().set_defPotionBonus(Dungeon.player.getPlayerData().get_defPotionBonus() - potion.getAmount());
					break;
				case "SCORE_BUF":
					Dungeon.player.getPlayerData().addScoreMultiplier(potion.getAmount());
					break;
			}
		} catch (InterruptedException e) {
			e.getMessage();
		}

		if (!Window.isOpen()) {
			Draw.call(ViewObjects.infoGrid.getFirstBlock());
		}
		log.info("[POTION_EFFECT_WORKER] Worker of " + potion.getName() + " was ended");
	}

	private void startTimer() throws InterruptedException {
		int timer = EFFECT_SECONDS_MINIMUM_TIME;

		Draw.call(ViewObjects.infoGrid.getThirdBlock());

		potion.setEffectTimer(timer);

		ViewObjects.infoGrid.putPotionBonusInfo(potion);

		log.info("[POTION_EFFECT_WORKER] Worker of " + potion.getName() + " was started");

		if (!Window.isOpen()) {
			Draw.call(ViewObjects.infoGrid.getFirstBlock());
		}

		while (timer > 0) {

			ViewObjects.infoGrid.drawPotionTimer(potion);

			timer--;
		}
		ViewObjects.infoGrid.clearTimers();
	}
}
