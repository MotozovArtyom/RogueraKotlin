package com.roguera.mapgenerate;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.roguera.base.GameObject;
import com.roguera.gamemap.Cell;
import com.roguera.gamemap.EditorEntity;
import com.roguera.gamemap.Position;
import com.roguera.gamemap.Room;
import com.roguera.input.Input;
import com.roguera.resources.Colors;
import com.roguera.resources.Model;
import com.roguera.view.Draw;
import com.roguera.view.ViewObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.roguera.mapgenerate.MapEditor.BorderObjects;
import static com.roguera.mapgenerate.MapEditor.DrawDirection;
import static com.roguera.mapgenerate.MapEditor.InsertShapeLine;
import static com.roguera.mapgenerate.MapEditor.SetRoomForEdit;
import static com.roguera.mapgenerate.MapEditor.setIntoCell;

/**
 * Попытка переписать алгоритм генерации структуры комнаты по точкам
 * В этот раз каждая точка связана с другой, как эдакая нода в LinkedList
 * Разработка оставлена до вероятной версии игры 0.3.1.
 */
public class PGPv2 {

	private static final Logger log = LoggerFactory.getLogger(PGPv2.class);

	private Point firstPoint = new Point(new Position());

	private Point nextPoint = new Point(new Position()).prevPoint = firstPoint;

	private final EditorEntity Dot = new EditorEntity(new Model("dot", Colors.ORANGE, '.'));

	private final ArrayList<Point> linkedPoints = new ArrayList<>();

	ArrayList<Cell> roomCells = new ArrayList<>();

	private int boundX;

	private int boundY;

	private boolean isUTD = true;

	public PGPv2(Room room) {
		roomCells = room.getCells();
	}

	public PGPv2() {
		Room testRoom = new Room(0, 20, 20, RoomGenerate.RoomSize.MIDDLE);
		roomCells = testRoom.getCells();
		boundX = testRoom.width;
		boundY = testRoom.height;
		SetRoomForEdit(testRoom);
		ViewObjects.mapView.setRoom(testRoom);
	}

	public void generateRoomStructure() {

		Draw.clear();

		System.out.println();
		System.out.println();
		System.out.println();

		log.info("[PGPv2]=== NEW GENERATE ===");
		log.info("[PGPv2] Bound X: " + boundX + " Bound Y: " + boundY);

		placeFirstPoints();

		upToDown();

		isUTD = false;

		downToUp();

		firstPoint.nextPoint = linkedPoints.get(0);

		log.info("[PGPv2] === LIST OF LINKED POINTS ===");

		linkedPoints.forEach(point -> log.info("[PGPv2][Point] " + point.position + " --> nextPoint --> " + (point.nextPoint != null ? point.nextPoint.position : "none")));

		log.info("[PGPv2] === CONNECTING POINTS ===");

		connectPoints();


	}

	private void placeFirstPoints() {
		log.info("[PGPv2] === PLACE FIRST POINTS === ");

		placeDotIntoPoint(firstPoint);

		firstPoint.directionFromPrevPoint = getRandomDirection();

		linkedPoints.add(firstPoint);
	}

	private void upToDown() {
		log.info("[PGPv2] === UP TO DOWN ===");
		do {
			firstPoint = placePoint();
			log.info("[PGPv2][UTD] new first point: " + firstPoint.position);
		} while (firstPoint.position.y < boundY - 3);
	}

	private void downToUp() {
		log.info("[PGPv2] === DOWN TO UP ===");
		do {
			firstPoint = placePoint();
			log.info("[PGPv2][DTU] new first point: " + firstPoint.position);
		} while (firstPoint.position.y > 2 && firstPoint.position.x > 1);
	}

	private Point placePoint() {
		nextPoint = getNextPointByRandom();

		nextPoint.prevPoint = firstPoint;

		firstPoint.nextPoint = nextPoint;

		linkedPoints.add(nextPoint);

		placeDotIntoPoint(nextPoint);

		log.info("[PGPv2][Lenght] get length between firstPoint " + firstPoint.position + " and nextPoint " + nextPoint.position + " = " + getLenghtBetweenPoints(nextPoint));

		return nextPoint;
	}

	private Point getNextPointByRandom() {

		int x0 = firstPoint.position.x;

		int y0 = firstPoint.position.y;

		Point tempPoint = new Point();

		do {
			if (!isOutOfBounds(tempPoint.position) && (tempPoint.position.y >= boundY - 2 || tempPoint.position.x >= boundX - 3)) {
				break;
			}

			tempPoint.position = new Position(x0, y0);

			tempPoint.directionFromPrevPoint = getRandomDirection();

			log.info("[PGPv2][getNPBR] get direction: " + tempPoint.directionFromPrevPoint.name());

			tempPoint.position = tempPoint.position.getRelative(getPositionByDirection(tempPoint.directionFromPrevPoint));

			log.info("[PGPv2][getNPBR] get position: " + tempPoint.position);

		} while (pointIsNotPassRules(tempPoint));

		tempPoint.objectOnPoint = tempPoint.directionFromPrevPoint.getCell();

		return tempPoint;
	}

	private DrawDirection getRandomDirection() {
		return DrawDirection.values()[ThreadLocalRandom.current().nextInt(DrawDirection.values().length)];
	}

	private Position getPositionByDirection(DrawDirection drawDirection) {

		int offset;
		int x0 = firstPoint.position.x;

		int y0 = firstPoint.position.y;

		int minX, minY;

		minX = minY = 3;

		int maxX, maxY;

		maxX = 5;
		maxY = 5;

		log.info("[PGPv2][GetPosByDir] firstPoint: " + firstPoint.position);

		switch (drawDirection) {
			case DOWN: {
				if (isUTD) {
					offset = ThreadLocalRandom.current().nextInt(minY, maxY);
					return new Position(0, offset);
				}
				break;
			}
			case RIGHT: {
				offset = ThreadLocalRandom.current().nextInt(minX, maxX);
				return new Position(offset, 0);

			}
			case LEFT: {
				if (!isUTD) {
					offset = ThreadLocalRandom.current().nextInt(minX, maxX);
					return new Position(-offset, 0);
				}
				break;
			}
			case UP: {
				if (!isUTD) {
					offset = ThreadLocalRandom.current().nextInt(minY, maxY);
					return new Position(0, -offset);
				}
				break;
			}
		}
		return new Position(0, 0);
	}

	private boolean pointIsNotPassRules(Point point) {
		return isOutOfBounds(point.position) || isSamePoint(point.position) || isPassHalfOfLenght(point.position);
	}

	private boolean isOutOfBounds(Position position) {
		return this.roomCells.stream().noneMatch(cell -> cell.position.equals(position));
	}

	private boolean isSamePoint(Position position) {
		return this.linkedPoints.stream().anyMatch(point -> point.position.equals(position));
	}

	private boolean isPassHalfOfLenght(Position position) {
		return !isUTD && position.x < boundX / 2;
	}

	private void placeDotIntoPoint(Point point) {
		setIntoCell(Dot, point.position);
	}

	private void connectPoints() {
		linkedPoints.forEach(point -> {
			InsertShapeLine(this.roomCells, point.directionFromPrevPoint, getLenghtBetweenPoints(point), (point.prevPoint != null ? point.prevPoint.position : new Position()));
			Input.waitForInput();
			Draw.call(ViewObjects.mapView);
		});
	}

	private void placeCorner(Point point) {
		if (point.prevPoint == null || point.nextPoint == null) {
			return;
		}

		if (point.directionFromPrevPoint.equals(point.prevPoint.directionFromPrevPoint) && point.directionFromPrevPoint.equals(point.nextPoint.directionFromPrevPoint)) {
			return;
		}
		boolean isReverse = false;

		if (!point.directionFromPrevPoint.equals(point.nextPoint.directionFromPrevPoint)) {
			isReverse = point.directionFromPrevPoint.equals(DrawDirection.LEFT);
			setIntoCell(point.directionFromPrevPoint.getCorner(isReverse), point.position);
		} else if (point.prevPoint.objectOnPoint == BorderObjects.HWall && point.nextPoint.objectOnPoint == BorderObjects.HWall) {
			if (point.objectOnPoint == BorderObjects.VWall) {
				setIntoCell(BorderObjects.BCenter, point.position);
			}
		}
	}


	private int getLenghtBetweenPoints(Point point) {
		Point pPrev = point.prevPoint;
		Point pNext = point.nextPoint;

		int lenghtX = (pPrev != null ? Math.abs(point.position.x - pPrev.position.x) : 0);
		int lenghtY = (pPrev != null ? Math.abs(point.position.y - pPrev.position.y) : 0);

		if (lenghtX > 0 && lenghtY == 0) {
			return lenghtX;
		} else {
			return lenghtY;
		}
	}

	private class Point {
		Position position = new Position();

		Point prevPoint = null;

		Point nextPoint = null;

		DrawDirection directionFromPrevPoint;

		GameObject objectOnPoint;

		public Point(Position pointPosition) {
			this.position = pointPosition;
		}

		public Point() {
		}
	}
}

