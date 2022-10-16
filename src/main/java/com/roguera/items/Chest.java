/*
 * Copyright (c) Ksenofontov N. 2020-2021.
 */

package com.roguera.items;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.roguera.base.Entity;
import com.roguera.view.ChestContainsWindow;
import com.roguera.gamelogic.ItemGenerator;
import com.roguera.gamemap.Dungeon;
import com.roguera.resources.Colors;
import com.roguera.resources.Model;

public class Chest extends Entity {

	private final ArrayList<Item> loot = new ArrayList<>();

	public static final Model chest = new Model("chest", Colors.GOLDEN, Colors.B_GREYSCALE_237, '≡');

	public Chest() {
		super(chest);
		putLoot();
		action = () -> {
			String itemList = "";
			for (Item item : loot) {
				itemList = itemList.concat(item.getItemMaterial().getColor()
						+ item.model.get().getCharacter()
						+ Colors.R
						+ item.getName()).concat(" ");
			}
			new ChestContainsWindow(loot).show();
		};
	}

	private void putLoot() {
		int randomLootCount = Math.min((ThreadLocalRandom.current().nextInt(0, 2) * Dungeon.getCurrentFloor().get().getFloorNumber()), 5);
		for (int i = 0; i < randomLootCount; i++) {
			loot.add(ItemGenerator.getRandomWeaponEquipment());
		}
		randomLootCount = Math.min((ThreadLocalRandom.current().nextInt(0, 2) * Dungeon.getCurrentFloor().get().getFloorNumber()), 5);
		for (int i = 0; i < randomLootCount; i++) {
			loot.add(ItemGenerator.getRandomArmorEquipment());
		}
	}
}
