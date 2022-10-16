/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.gamelogic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.roguera.gamemap.Dungeon;
import com.roguera.gamemap.Position;
import com.roguera.items.Chest;
import com.roguera.items.Item;
import com.roguera.items.Weapon;
import com.roguera.view.Draw;
import com.roguera.view.Effect;
import com.roguera.view.ViewObjects;
import com.roguera.creatures.Creature;
import com.roguera.resources.Colors;
import com.roguera.resources.GameResources;
import com.roguera.resources.GetRandom;
import com.roguera.resources.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Events {

	private static final Logger log = LoggerFactory.getLogger(Events.class);

	public static Event<Position> putTestItemIntoPos = (position) -> {
		Model wp = GameResources.getModel("ShortSword");
		Dungeon.getCurrentRoom().getCell(position).putIntoCell(new Weapon(wp.getModelName(), wp, Item.Materials.DIAMOND, 10));
	};

	public static Event<Position> putChestIntoPos = (position) -> {
		//Model chest = new Model("chest", Colors.GOLDEN, Colors.B_GREYSCALE_237, '≡');
		Dungeon.getCurrentRoom().getCell(position).putIntoCell(new Chest());
	};

	private static final Consumer<Creature> hitEffectRun = target -> {

		target.model.changeBColor(Colors.B_RED_BRIGHT);

		Draw.call(ViewObjects.mapView);

		try {
			TimeUnit.MILLISECONDS.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		target.model.resetBColor();

		Draw.call(ViewObjects.mapView);
	};

	public static void encounter(Creature attacker, Creature victim) {
		log.info("[EVENT]Event encounter: " +
				"\n\t attacker: " + attacker.getName() + "ATK: " + attacker.getDamageByEquipment() + "| DEF: " + attacker.getDefenceByEquipment() +
				"\n\t victim:" + victim.getName() + "ATK: " + victim.getDamageByEquipment() + "| DEF: " + victim.getDefenceByEquipment());

		ExecutorService ef = Executors.newSingleThreadExecutor();

		int dmg = attacker.getDamageByEquipment();

		int fullDef = victim.getDefenceByEquipment();

		victim.getHit(dmg);

		ef.execute(new Effect<>(victim, hitEffectRun));

		ef.shutdown();

		ViewObjects.logView.action(victim.getName()
				.concat(" get ")
				.concat("" + Math.max(0, dmg - fullDef))
				.concat(" damage")
				.concat("(DEF:").concat(String.valueOf(victim.getDefenceByEquipment())).concat(")")
				.concat(" from ")
				.concat(attacker.getName())
				.concat(" ")
				.concat(GetRandom.getRandomWoops()));

		Draw.call(ViewObjects.infoGrid.getFirstBlock());
	}
}
