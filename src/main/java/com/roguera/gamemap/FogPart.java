/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.gamemap;

import com.roguera.base.GameObject;
import com.roguera.resources.Colors;
import com.roguera.resources.Model;

public class FogPart extends GameObject {

	public FogPart() {
		this.tag = "fog";
		setModel(new Model("fog", Colors.GREY, '#'));
	}

}
