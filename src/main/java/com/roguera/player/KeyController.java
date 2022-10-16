/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.player;

import java.util.HashMap;
import java.util.Optional;

import com.roguera.resources.GameResources;

public class KeyController {

	private static final HashMap<Character, Runnable> KEY_MAP = GameResources.getKeyMap();

	public static void getKey(Character key) {
		Optional<Runnable> keyAction = Optional.ofNullable(KEY_MAP.get(key));
		keyAction.ifPresent(Runnable::run);
	}
}
