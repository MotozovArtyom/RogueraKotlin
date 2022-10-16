package com.roguera;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.roguera.cinematic.TutorialScene;
import com.roguera.gamemap.Dungeon;
import com.roguera.gamemap.Room;
import com.roguera.mapgenerate.RoomGenerate;
import com.roguera.resources.GameResources;
import com.roguera.view.IViewBlock;
import com.roguera.view.TerminalView;
import com.roguera.view.ViewObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static boolean newGame = true;

	private static boolean newGameWithExistCharacter = false;

	public static ExecutorService autoLogWorker;

	public Main() {
	}

	public static void disableNewGame() {
		newGame = false;
	}

	public static boolean isNewGame() {
		return newGame;
	}

	public static void enableNewGameWithExistCharacter() {
		newGameWithExistCharacter = true;
	}

	public static void disableNewGameWithExistCharacter() {
		newGameWithExistCharacter = false;
	}

	public static boolean isNewGameWithExistCharacter() {
		return newGameWithExistCharacter;
	}

	public static void enableNewGame() {
		newGame = true;
	}

	public static void startSequence() throws IOException {
		autoLogWorker = Executors.newSingleThreadExecutor();

		log.info("[GAME] =================================== START SEQUENCE ===================================");

		GameResources.loadFont();

		TerminalView.initTerminal();

		GameResources.loadResources();

		ViewObjects.LoadViewObjects();

		TerminalView.setGameScreen();

		log.info("[GAME] =================================== RESOURCES LOADED ===================================");

		if (isNewGame()) {
			log.info("[MAIN] New game");
			new TutorialScene(new Room(0, 60, 60, RoomGenerate.RoomSize.BIG)).startSequence();
			Dungeon.generate();
		} else if (isNewGameWithExistCharacter()) {
			log.info("[MAIN] New game with exist character");
			Dungeon.generate();
		} else {
			log.info("[MAIN] Load game from save file");
			Dungeon.reloadDungeonAfterLoad();
		}

		TerminalView.reDrawAll(IViewBlock.empty);

		GameLoop gameLoop = new GameLoop();

		try {
			gameLoop.start();
		} catch (Exception e) {
			e.printStackTrace();
			GameLoop.endGameSequence();
		}

		if (autoLogWorker != null) {
			log.info("[SHUTDOWN] Auto log worker");
			autoLogWorker.shutdownNow();
			try {
				autoLogWorker.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		log.info("=================================== END OF GAME ===================================");
		log.info("\u001b[38;5;200m[SYSTEM] End of main sequence");
	}
}