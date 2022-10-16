/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.base;

import com.roguera.gamelogic.EntityAction;
import com.roguera.resources.Model;

public class Entity extends GameObject {

	private static int EntityCounter = 0;
	public EntityAction action;

	public Entity(Model model, EntityAction action) {
		this.tag = "entity";
		this.action = action;
		this.id = EntityCounter++;
		setModel(model);
	}

	public Entity(Model model) {
		this(model, () -> {
		});
	}
}
