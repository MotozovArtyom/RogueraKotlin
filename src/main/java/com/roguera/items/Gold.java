package com.roguera.items;

import com.roguera.resources.Colors;
import com.roguera.resources.GameResources;
import com.roguera.resources.GetRandom;
import com.roguera.resources.Model;

public class Gold extends Item {

	private final int amount;

	public Gold() {
		this("Gold", GameResources.getModel("Gold").changeColor(Colors.GOLDEN));
	}

	private Gold(String name, Model model) {
		super(name, model, Materials.GOLD);
		this.amount = GetRandom.getRandomGoldAmount();
	}

	public int getAmount() {
		return this.amount;
	}
}
