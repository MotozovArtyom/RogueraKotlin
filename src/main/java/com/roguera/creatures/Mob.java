/*
 * Copyright (c) Ksenofontov N. 2020-2021.
 */

package com.roguera.creatures;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import com.roguera.gamemap.Cell;
import com.roguera.gamemap.Dungeon;
import com.roguera.items.Item;
import com.roguera.gamelogic.ItemGenerator;
import com.roguera.gamelogic.RogueraGameSystem;
import com.roguera.resources.Colors;

/**
 * <p>
 * Класс противника в игре.
 * Наследуется от {@link Creature}
 * </p>
 */
public class Mob extends Creature {

	private boolean dead = false;

	public boolean isDead() {
		return this.dead;
	}

	private void dead() {
		this.dead = true;
	}

	protected int experiencePoints;

	@Override
	public int getDamageByEquipment() {
		return super.getDamageByEquipment();
	}

	@Override
	public void getHit(int incomingDamage) {
		int fullDef = getDefenceByEquipment();
		int deltaDmg = incomingDamage - fullDef;
		this.HP -= Math.max(0, deltaDmg);
	}

	public Mob(int HP, String name) {
		super(HP, name);

		this.tag += ".mob";

		this.baseATK = RogueraGameSystem.getMobBaseATK();

		this.baseDEF = RogueraGameSystem.getMobBaseDEF();

		this.model.changeModel(name.charAt(0));

		this.model.changeColor(Colors.RED_BRIGHT);

		calculateEXP();

		initialPutInventory();
		//log.info(this.toString());
	}

	private void calculateEXP() {
		experiencePoints = (int)Math.round(
				Dungeon.getCurrentFloor().get().getFloorNumber() * Dungeon.getCurrentRoom().roomNumber + Math.pow(10, 1.2d)
		);
	}

	public int getExperiencePoints() {
		return this.experiencePoints;
	}

	@Override
	public String getName() {
		return Colors.RED_BRIGHT + this.name + Colors.R;
	}

	private void initialPutInventory() {
		this.creatureInventory.add(ItemGenerator.getRandomWeaponEquipment());
		this.creatureInventory.add(ItemGenerator.getRandomArmorEquipment());
		this.creatureInventory.add(ItemGenerator.getRandomGold());
	}

	public void dropLoot() {
		Cell[] cells = Dungeon.getCurrentRoom().getCell(this.cellPosition).getCellsAround();
		for (Item item : creatureInventory) {
			int randomCellIndex = ThreadLocalRandom.current().nextInt(8);
			if (!cells[randomCellIndex].isWall())
				cells[randomCellIndex].putIntoCell(item);
		}
		dead();
	}

	@Override
	public String toString() {
		return "[INFO]Mob: ".concat(this.getName()).concat("\n\t")
				.concat("HP:" + this.getHP())
				.concat("\n\t")
				.concat("Base ATK: " + this.baseATK)
				.concat("\n\t")
				.concat("Base DEF: " + this.baseDEF)
				.concat("\n\t")
				.concat("DMG: " + this.getDamageByEquipment())
				.concat("\n\t")
				.concat("Items in inventory:")
				.concat("\n------------------------")
				.concat("\n")
				.concat(Arrays.deepToString(this.creatureInventory.toArray()))
				.concat("\n------------------------");
	}
}
