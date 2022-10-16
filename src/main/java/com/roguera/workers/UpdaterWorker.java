package com.roguera.workers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import com.roguera.gamemap.Dungeon;
import com.roguera.net.ServerRequests;
import com.roguera.net.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdaterWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(UpdaterWorker.class);

	SessionData sessionData;

	public UpdaterWorker(SessionData sessionData) {
		this.sessionData = sessionData;
	}

	@Override
	public void run() {
		log.info("[UPDATER_WORKER]Update game session worker has started");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				TimeUnit.SECONDS.sleep(3);

				updateSessionData();

				ServerRequests.updateGameSession(sessionData);

			} catch (InterruptedException e) {
				log.info("[UPDATER_WORKER]Error in update game session (" + e.getMessage() + ")");
				break;
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateSessionData() {
		sessionData.setFloor(Dungeon.currentFloorNumber);
		sessionData.setRoom(Dungeon.getCurrentRoom().roomNumber);
		sessionData.setKills(Dungeon.player.getPlayerData().getKills());
		sessionData.setScore(Dungeon.player.getPlayerData().getScore());
	}

	public void updateSessionAndInterrupt() {
		updateSessionData();
		try {
			ServerRequests.updateGameSession(sessionData);
			log.info("[UPDATE_WORKER] Session is updated finally");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
