/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.input;

import java.io.IOException;
import java.util.Optional;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.roguera.view.TerminalView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Input {

	private static final Logger log = LoggerFactory.getLogger(Input.class);

	private static KeyStroke GetKey() {
		try {
			return TerminalView.terminal.readInput();
		} catch (IOException | RuntimeException e) {
			log.info("[INPUT] Input closed");
			return KeyStroke.fromString(" ");
		}
	}

	public static Optional<KeyStroke> waitForInput() {
		return Optional.ofNullable(GetKey());
	}

	public static boolean keyNotNull(KeyStroke key) {
		return key != null;
	}

	public static boolean keyIsEscape(KeyStroke key) {
		return key.getKeyType().equals(KeyType.Escape);
	}
}
