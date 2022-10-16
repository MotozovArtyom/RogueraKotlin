/*
 * Copyright (c) Ksenofontov N. 2020-2021.
 */

package com.roguera.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.roguera.creatures.Creature;
import com.roguera.creatures.Mob;
import com.roguera.gamelogic.Events;
import com.roguera.gamelogic.PathFinder;
import com.roguera.gamemap.Cell;
import com.roguera.gamemap.Dungeon;
import com.roguera.gamemap.FogPart;
import com.roguera.gamemap.Position;
import com.roguera.gamemap.Room;
import com.roguera.player.Player;
import com.roguera.resources.Colors;
import com.roguera.view.Animation;
import com.roguera.view.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.roguera.view.ViewObjects.logView;

public class AIController implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(AIController.class);

	private final Creature creature;

	private Position targetPosition;

	private Creature target;

	private final PathFinder pathFinder = new PathFinder();

	private final Room currentRoom;

	public AIController(Creature creature) {
		this.creature = creature;
		this.currentRoom = Dungeon.getCurrentRoom();
	}

	@Override
	public void run() {
		behaviorLoop();
		log.info("[AI] Mob " + this.creature.getName() + " behavior loop is over. Cause by " + (isDead() ? "Mob dead" : (isTargetDead() ? " target dead" : " interrupted")));
	}

	private boolean isDead() {
		return this.creature.getHP() <= 0;
	}

	private void behaviorLoop() {
		while (!isDead() && !Thread.currentThread().isInterrupted()) {
			try {
				if (fogUncover() && !Window.isOpen()) {
					if (checkPlayer()) {
						moveToTarget();
						if (isTargetDead()) {
							break;
						}
					}
				}
				idle();
			} catch (InterruptedException e) {
				log.info("[AI]Creature thread ".concat(creature.getName()).concat(" has been interrupted"));
				break;
			}
		}
		if (isDead()) {
			mobDead();
		}
	}

	private void mobDead() {
		logView.playerAction("kill a " + this.creature.model.getModelColorName() + "!");

		int scoreForMob = ThreadLocalRandom.current().nextInt(3, 10) * this.creature.getDamageByEquipment();

		Dungeon.player.getPlayerData().setScore(scoreForMob);

		Dungeon.player.getPlayerData().setKill();

		Dungeon.player.getPlayerData().setExp(((Mob)this.creature).getExperiencePoints());

		log.info(Colors.VIOLET + "[SCORE] Score for mob: " + scoreForMob + " (equipment dmg " + this.creature.getDamageByEquipment() + ")");

		new Animation().deadAnimation(this.creature);

		((Mob)this.creature).dropLoot();

		//Thread.currentThread().interrupt();
	}

	private boolean fogUncover() {
		Cell[] cellsAround = Dungeon.getCurrentRoom().getCell(this.creature.cellPosition).getCellsAround();
		return !Arrays.stream(cellsAround).allMatch(cell -> cell.getFromCell() instanceof FogPart);
	}

	private boolean isTargetDead() {
		if (target != null)
			return this.target.getHP() <= 0;
		else {
			return true;
		}
	}

	private boolean checkPlayer() {
		ArrayList<Cell> cells = Dungeon.getCurrentRoom().getCells();

		if (cells.stream().anyMatch(cell -> cell.getFromCell() instanceof Player)) {
			target = (Creature)cells.stream().filter(cell -> cell.getFromCell() instanceof Player).findAny().get().getFromCell();
			targetPosition = target.cellPosition;
			return true;
		}
		return false;
	}

	private void moveToTarget() throws InterruptedException {
		ArrayList<Position> pathToTarget = updatePath();

		int steps = 0;
		while (!Window.isOpen() && !isTargetNear() && !isDead() && !Thread.currentThread().isInterrupted()) {
			try {
				Position nextPosition = pathToTarget.get(steps);

				if (!(Dungeon.getCurrentRoom().getCell(nextPosition).getFromCell() instanceof Creature)) {
					creature.moveTo(nextPosition);
				}

				TimeUnit.MILLISECONDS.sleep(350);

				steps++;
				if (!isTargetNotMove() && steps >= pathToTarget.size() / 2) {
					break;
				}

			} catch (IndexOutOfBoundsException e) {
				log.info(Colors.RED_BRIGHT + "[AI][Mob] " + creature.getName() + " something goes wrong with index ");
				break;
			}
		}
		try {
			if (isTargetNear()) {
				Events.encounter(this.creature, this.target);
			}
		} catch (NullPointerException e) {
			log.info(Colors.RED_BRIGHT + "[AI][Mob] Exception in target finder (null)");
		}
	}

	private ArrayList<Position> updatePath() {
		return pathFinder.getPathToTarget(currentRoom, targetPosition, creature.cellPosition);
	}

	private boolean isTargetNear() {
		try {
			return Arrays.stream(currentRoom.getCell(creature.cellPosition).getCellsAround()).anyMatch(cell -> cell.getFromCell() instanceof Player);
		} catch (NullPointerException e) {
			log.info(Colors.RED_BRIGHT + "[AI][Mob] Exception in target finder (null)");
			return false;
		}
	}

	private boolean isTargetNotMove() {
		return target.cellPosition.equals(targetPosition);
	}

	private boolean isTargetOnFront(Position targetPosition) {
		return creature.cellPosition.getRelative(Position.FRONT).equals(targetPosition);
	}

	private void idle() throws InterruptedException {
		//log.info("[AI][Mob] "+creature.getName() + " waiting...");
		TimeUnit.MILLISECONDS.sleep(400);
	}
}
