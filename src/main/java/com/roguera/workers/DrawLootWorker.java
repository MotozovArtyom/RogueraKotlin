package com.roguera.workers;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.roguera.base.GameObject;
import com.roguera.view.Draw;
import com.roguera.view.ViewObjects;
import com.roguera.creatures.Mob;
import com.roguera.gamemap.Dungeon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawLootWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(DrawLootWorker.class);

	@Override
	public void run() {
		log.info("[DRAW_LOOT_WORKER] Start working in room " + Dungeon.getCurrentRoom().roomNumber);
		ArrayList<GameObject> gameObjects = Dungeon.getCurrentRoom().getObjectsSet();
		try {
			while (!Thread.currentThread().isInterrupted()) {
				if (gameObjects.stream().noneMatch(gameObject -> ((gameObject instanceof Mob) && !(((Mob)gameObject).isDead())))) {
					Draw.call(ViewObjects.mapView);
					break;
				} else {
					TimeUnit.MILLISECONDS.sleep(500);
				}
			}
		} catch (InterruptedException e) {
			log.info("[DRAW_LOOT_WORKER] End of thread");
		}
		log.info("[DRAW_LOOT_WORKER] End of work");
	}
}
