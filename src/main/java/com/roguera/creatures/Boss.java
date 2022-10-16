package com.roguera.creatures;

import java.util.concurrent.ThreadLocalRandom;

import com.roguera.gamemap.Dungeon;
import com.roguera.gamelogic.ItemGenerator;
import com.roguera.gamelogic.RogueraGameSystem;
import com.roguera.resources.Colors;
import com.roguera.resources.GameResources;

public class Boss extends Mob {

	private static final String[] bossNames = {"Alpha", "Omega", "OmegaBig", "Null", "Zero"};

	public Boss() {
		super(100, bossNames[ThreadLocalRandom.current().nextInt(bossNames.length)]);

		this.tag += ".boss." + name;

		this.model.changeModel(GameResources.getModel(this.name).get().getCharacter());

		this.model.changeColor(Colors.MAGENTA);

		this.baseATK = RogueraGameSystem.getMobBaseATK();

		this.baseDEF = RogueraGameSystem.getMobBaseDEF();

		this.HP = 50 + (int)RogueraGameSystem.getBaseFloorProgression() * 2;

		calculateXP();

		creatureInventory.add(ItemGenerator.getSpecialBossWeapon());

		creatureInventory.add(ItemGenerator.getSpecialBossArmor());
	}

	private void calculateXP() {
		experiencePoints = (int)Math.round(Dungeon.getCurrentFloor().get().getFloorNumber() * Dungeon.getCurrentRoom().roomNumber + Math.pow(10, 1.4d));
	}
}
