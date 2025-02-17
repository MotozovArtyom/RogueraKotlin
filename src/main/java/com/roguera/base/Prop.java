/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.base;

import com.roguera.resources.Model;

public abstract class Prop extends GameObject {

	public static int PropCounter = 0;

	public Prop(Model model) {
		this.id = PropCounter++;
		this.tag = "prop";
		setModel(model);
	}
}
