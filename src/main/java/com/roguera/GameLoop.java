/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.googlecode.lanterna.input.KeyType;
import com.roguera.gamelogic.ItemGenerator;
import com.roguera.gamelogic.SaveLoadSystem;
import com.roguera.gamemap.Dungeon;
import com.roguera.gamemap.Floor;
import com.roguera.gamemap.Room;
import com.roguera.input.Input;
import com.roguera.items.Potion;
import com.roguera.net.PlayerDTO;
import com.roguera.net.ServerRequests;
import com.roguera.net.SessionData;
import com.roguera.resources.Colors;
import com.roguera.resources.GameResources;
import com.roguera.utils.HashToString;
import com.roguera.utils.SavePlayerDTO;
import com.roguera.view.Animation;
import com.roguera.view.Draw;
import com.roguera.view.Message;
import com.roguera.view.TerminalView;
import com.roguera.view.ViewObjects;
import com.roguera.workers.AutoSaveLogWorker;
import com.roguera.workers.PlayerMovementWorker;
import com.roguera.workers.UpdaterWorker;
import net.arikia.dev.drpc.DiscordRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.roguera.view.ViewObjects.getTrimString;
import static com.roguera.view.ViewObjects.logView;

public class GameLoop {

	private static final Logger log = LoggerFactory.getLogger(GameLoop.class);

	public static int playTime;

	public static ExecutorService updWrk;

	public static ExecutorService movingWrk;

	public static PlayerDTO playerDTO = new PlayerDTO();

	public static SessionData sessionData = new SessionData();

	public static UpdaterWorker updaterWorker;

	public GameLoop() {

	}

	public void start() throws InterruptedException, URISyntaxException, IOException {

		Dungeon.player.getPlayerData().updBaseATK();

		Dungeon.player.getPlayerData().updBaseDEF();

		updWrk = Executors.newSingleThreadExecutor();

		movingWrk = Executors.newSingleThreadExecutor();

		Potion.effectWrks = Executors.newCachedThreadPool();

		if (Main.isNewGame()) {
			logView.playerAction("entered the dungeon... Good luck!");

			try {
				if (Roguera.INSTANCE.isOnline()) {
					Long playerId = Long.parseLong(ServerRequests.createNewUser(getTrimString(Dungeon.player.getPlayerData().getPlayerName())));

					Dungeon.player.getPlayerData().setPlayerID(playerId.intValue());

					playerDTO.setNickName(ViewObjects.getTrimString(Dungeon.player.getName()));

					playerDTO.setId(playerId);

					MessageDigest hashGen = MessageDigest.getInstance("SHA-256");

					hashGen.update(ServerRequests.getSecretKey());

					hashGen.update(playerId.toString().getBytes(StandardCharsets.UTF_8));

					hashGen.update(playerDTO.getNickName().getBytes(StandardCharsets.UTF_8));

					playerDTO.setPlayerHash(HashToString.convert(hashGen.digest()));

					log.info(playerDTO.getPlayerHash());

					SavePlayerDTO.save(playerDTO);
				} else {
					Dungeon.player.getPlayerData().setPlayerID(0);
				}

				log.info("[HTTP_POST][GAME_LOOP] User created and got id = " + playerDTO.getId());
			} catch (URISyntaxException | IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			Dungeon.player.putUpItem(ItemGenerator.getRandomWeaponEquipment());

			Dungeon.player.getPlayerData().updRequiredEXP();

			//Events.putTestItemIntoPos.action(new Position(3,3));
		}

		if (Main.isNewGameWithExistCharacter()) {
			Dungeon.player.putUpItem(ItemGenerator.getRandomWeaponEquipment());

			Dungeon.player.getPlayerData().updRequiredEXP();
		}

		if (Roguera.INSTANCE.isOnline()) {

			String playerName = ViewObjects.getTrimString(Dungeon.player.getName());

			int pId = Dungeon.player.getPlayerData().getPlayerID();

			log.info("[GAME_LOOP] Creating a new game session for player " + playerName);

			sessionData.setPlayerName(playerName);

			sessionData.setPlayerID(Integer.toUnsignedLong(pId));

			byte[] sessionKey = ServerRequests.createGameSession(sessionData);

			String sessionToken = HashToString.convert(sessionKey);

			sessionData.setSessionKey(sessionKey);

			sessionData.setSessionToken(sessionToken);

		} else {
			//gameSessionId = ThreadLocalRandom.current().nextInt(100000, 999999);
		}

		Main.autoLogWorker.execute(new AutoSaveLogWorker());

		Main.autoLogWorker.shutdown();

		if (Roguera.INSTANCE.isOnline()) {
			updaterWorker = new UpdaterWorker(sessionData);

			updWrk.execute(updaterWorker);

			updWrk.shutdown();
		}

		Draw.call(ViewObjects.mapView);

		Draw.call(ViewObjects.infoGrid);

		movingWrk.execute(new PlayerMovementWorker());

		movingWrk.shutdown();

		while (/*isNotEscapePressed() && */isNotClosed() && !Thread.currentThread().isInterrupted()) {

			Dungeon.player.updateRichPresence();

			if (TerminalView.keyStroke.getKeyType().equals(KeyType.EOF)) {
				break;
			}

			if (!isNotEscapePressed()) {
				if (Roguera.INSTANCE.isOnline()) {
					new Message("Score of this game session will be saved on server.").show();
				} else {
					new Message("Score of this game session will not be saved: no connection").show();
				}
				break;
			}

			if (Dungeon.player.getPlayerData().getHP() <= 0) {

				new Animation().deadAnimation(Dungeon.player);

				logView.playerAction(Colors.RED_BRIGHT + "are dead. Game over!");

				logView.action(Colors.WHITE_BRIGHT + "Press enter to quit.");

				movingWrk.shutdownNow();

				Input.waitForInput().get().getKeyType().equals(KeyType.Enter);

				if (SaveLoadSystem.deleteSaveFile()) {
					log.info("[GAME_LOOP] Save file deleted");
				}

				break;
			}

			Dungeon.player.checkNewLevel();

			DiscordRPC.discordRunCallbacks();

			TimeUnit.MILLISECONDS.sleep(100);
		}

		if (Roguera.INSTANCE.isOnline() || Roguera.tryToConnect()) {
			if (!Roguera.INSTANCE.isOnline()) {
				log.info("[NETWORK][GAME_SESSION] Try to create new user and GS");

				String data = ServerRequests.createNewUser(getTrimString(Dungeon.player.getPlayerData().getPlayerName()));

				Dungeon.player.getPlayerData().setPlayerID(Integer.parseInt(data.split(",")[0]));

				//ServerRequests.createGameSession();
			}

			updaterWorker.updateSessionAndInterrupt();

			ServerRequests.finalizeGameSession(sessionData);

			sessionData = new SessionData();
		}

		log.info(Colors.VIOLET + "[SYSTEM]End of the game session");

		endGameSequence();
	}

	public static void endGameSequence() {
		log.info("[SYSTEM] Starting shutdown sequence");

		if (Dungeon.player != null && Dungeon.player.getHP() > 0) {
			try {
				log.info("[SHUTDOWN] Saving game...");
				SaveLoadSystem.saveGame();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (updWrk != null) {
			log.info("[SHUTDOWN] Update game session worker");
			updWrk.shutdownNow();
		}

		if (Potion.effectWrks != null) {
			log.info("[SHUTDOWN] potion effect workers");
			Potion.effectWrks.shutdownNow();
		}

		log.info("[SHUTDOWN] Player moving worker");
		movingWrk.shutdownNow();

		if (Dungeon.rooms != null && Dungeon.floors != null) {
			log.info("[SHUTDOWN] End mob threads");
			Dungeon.rooms.forEach(Room::endMobAIThreads);

			log.info("[SHUTDOWN] Clear rooms and floors");

			Dungeon.rooms.forEach(room -> {
				room.getObjectsSet().clear();
				room.getCells().clear();
			});

			Dungeon.floors.clear();

			Dungeon.rooms.clear();

		}

		log.info("[SHUTDOWN] Reset dungeon variables");
		Dungeon.resetVariables();

		log.info("[SHUTDOWN] Reset floor counter");
		Floor.resetCounter();

		log.info("[SHUTDOWN] Clear game resources");
		GameResources.clearResources();

		log.info("[SHUTDOWN] Dispose terminal");
		TerminalView.dispose();

		log.info("[SHUTDOWN] Calling for garbage collector");
		System.gc();
	}

	private boolean isNotClosed() {
		return TerminalView.terminal != null;
	}

	private boolean isNotEscapePressed() {
		KeyType keyType = TerminalView.keyStroke.getKeyType();
		return keyType != KeyType.Escape;
	}
}
