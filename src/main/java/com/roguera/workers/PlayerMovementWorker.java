package com.roguera.workers;

import java.util.Optional;

import com.googlecode.lanterna.input.KeyType;
import com.roguera.gamemap.Dungeon;
import com.roguera.input.Input;
import com.roguera.player.KeyController;
import com.roguera.player.MoveController;
import com.roguera.view.TerminalView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerMovementWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(PlayerMovementWorker.class);

	@Override
	public void run() {
		log.info("[PLAYER_MOVEMENT_WORKER] Start of worker");
		while (Dungeon.player.getHP() > 0) {
			Input.waitForInput().ifPresent(keyStroke -> TerminalView.keyStroke = keyStroke);

			if (!isNotEscapePressed() || Thread.currentThread().isInterrupted()) {
				break;
			}

			KeyController.getKey(TerminalView.keyStroke.getCharacter());

			MoveController.movePlayer(Optional.ofNullable(TerminalView.keyStroke));
		}
		log.info("[PLAYER_MOVEMENT_WORKER] End of worker");
	}

	private boolean isNotEscapePressed() {
		KeyType keyType = TerminalView.keyStroke.getKeyType();
		return keyType != KeyType.Escape;
	}
}
