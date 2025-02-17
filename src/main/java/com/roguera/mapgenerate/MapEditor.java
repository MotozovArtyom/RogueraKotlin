/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.mapgenerate;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import com.roguera.base.GameObject;
import com.roguera.gamemap.Border;
import com.roguera.gamemap.Cell;
import com.roguera.gamemap.Position;
import com.roguera.gamemap.Room;
import com.roguera.gamemap.Scan;
import com.roguera.resources.GameResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapEditor {

    private static final Logger log = LoggerFactory.getLogger(MapEditor.class);

	private static Room currentRoomForEdit;

	public static void SetRoomForEdit(Room room) {
		currentRoomForEdit = room;
	}

	enum DrawDirection {
		UP {
			@Override
			public GameObject getCell() {
				return BorderObjects.VWall;
			}

			@Override
			public GameObject getCorner(boolean IsReverse) {
				return BorderObjects.RBCorner;
			}
		},
		DOWN {
			@Override
			public GameObject getCell() {
				return BorderObjects.VWall;
			}

			@Override
			public GameObject getCorner(boolean IsReverse) {
				if (IsReverse)
					return BorderObjects.RTCorner;
				else
					return BorderObjects.LBCorner;
			}
		},
		LEFT {
			@Override
			public GameObject getCell() {
				return BorderObjects.HWall;
			}

			@Override
			public GameObject getCorner(boolean IsReverse) {
				if (IsReverse)
					return BorderObjects.RTCorner;
				else
					return BorderObjects.LTCorner;
			}
		},
		RIGHT {
			@Override
			public GameObject getCell() {
				return BorderObjects.HWall;
			}

			@Override
			public GameObject getCorner(boolean IsReverse) {
				if (IsReverse)
					return BorderObjects.LBCorner;
				else
					return BorderObjects.RTCorner;
			}
		};

		public abstract GameObject getCell();

		public abstract GameObject getCorner(boolean IsReverse);
	}

	public static final char EmptyCell = GameResources.EMPTY_CELL;

	private static final Random rnd = new Random();

	public static GameObject[] DrawLine(DrawDirection drawDirection, int Length) {

		GameObject[] lineBuffer = new GameObject[Length];

		//log.info("[MAP_EDITOR] Draw line: dir."+drawDirection.name() + " lenght: " +Length);

		for (int i = 0; i < Length; i++) {
			lineBuffer[i] = drawDirection.getCell();
		}

		//log.info("[MAP_EDITOR] Line buffer: " + Arrays.toString(lineBuffer));

		return lineBuffer;
	}

	static void InsertShapeLine(ArrayList<Cell> CurrentRoomCells, DrawDirection drawDirection, int Length, int X_a, int Y_a) {

		GameObject[] shape = DrawLine(drawDirection, Length);

		switch (drawDirection) {
			case UP: {
				for (GameObject gObj : shape) {
					InsertLine(CurrentRoomCells, gObj, X_a, Y_a);
					Y_a--;
				}
				break;
			}
			case DOWN: {
				for (GameObject gObj : shape) {
					InsertLine(CurrentRoomCells, gObj, X_a, Y_a);
					Y_a++;
				}
				break;
			}
			case LEFT: {
				for (GameObject gObj : shape) {
					InsertLine(CurrentRoomCells, gObj, X_a, Y_a);
					X_a--;
				}
				break;
			}
			case RIGHT: {
				for (GameObject gObj : shape) {
					InsertLine(CurrentRoomCells, gObj, X_a, Y_a);
					X_a++;
				}
				break;
			}
		}
	}

	static void InsertShapeLine(ArrayList<Cell> CurrentRoomCells, DrawDirection drawDirection, int Length, Position FromPosition) {
		InsertShapeLine(CurrentRoomCells, drawDirection, Length, FromPosition.x, FromPosition.y);
	}

	static void InsertLine(ArrayList<Cell> CurrentRoomCells, GameObject gObj, int x, int y) {
		//log.info("[MAP_EDITOR] Insert shape " + gObj.model.toString() + " into position "+ x + "|" + y);
		if (GenerateRules.IsNotOutOfBoundsRule(new Position(x, y), CurrentRoomCells)) {
			setIntoCell(CurrentRoomCells, gObj, new Position(x, y));
		}
	}

	static void PlaceDoors(Room room, ArrayList<Cell> CurrentRoomCells, Position ExitPoint) {

		Cell cell = CurrentRoomCells.stream().filter(cell1 -> cell1.position.equals(ExitPoint)).findFirst().orElse(new Cell(new Position(1, 0)));

		cell.unsetWall();

		if (!Scan.checkWall(cell) && !Scan.checkCorner(cell)) {
			setIntoCell(room, cell, ExitPoint);
			setIntoCell(room.getNextDoor(), ExitPoint);
		} else {
			int x_shift = FindWall(room.getCell(ExitPoint));
			currentRoomForEdit.getCell(ExitPoint.getRelative(x_shift, 0)).unsetWall();
			setIntoCell(room.getNextDoor(), ExitPoint.getRelative(x_shift, 0));
		}
		if (room.roomNumber > 1) {
			int x_shift = FindWall(room.getCell(room.getTopCenterCellPosition()));
			setIntoCell(room.getCells(), room.getBackDoor(), room.getTopCenterCellPosition().getRelative(x_shift, 0));
			room.getCell(room.getTopCenterCellPosition()).unsetWall();
		}
	}

	private static int FindWall(Cell cell) {

		int x_shift = 1;

		if (Scan.checkCorner(cell)) {

			int shift = GetShiftDirection(cell);

			Position shiftedPos = cell.position.getRelative(x_shift *= shift, 0);

			Cell nextCell = currentRoomForEdit.getCell(shiftedPos);
			while (!Scan.checkWall(nextCell) && Scan.checkCorner(nextCell)) {
				shift = GetShiftDirection(cell);
				nextCell = currentRoomForEdit.getCell(shiftedPos.getRelative(x_shift *= shift, 0));
				x_shift++;
			}
			return x_shift;
		}
		return 0;
	}

	private static int GetShiftDirection(Cell cell) {
		return Scan.checkCorner(cell, "RB") || Scan.checkCorner(cell, "RT") ? -1 : 1;
	}

	public static void setIntoCell(GameObject gameObject, int y, int x) {
		//log.info("[MAP_EDITOR] Insert object " + gameObject.model.toString() + " into position "+ x + "|" + y);
		currentRoomForEdit.getCell(x, y).putIntoCell(gameObject);
	}

	public static void setIntoCell(GameObject gameObject, Position position) {
		setIntoCell(gameObject, position.y, position.x);
	}

	public static void setIntoCell(Room CurrentRoom, Cell cell, Position position) {
		try {
			CurrentRoom.replaceCell(cell, position);
		} catch (ArrayIndexOutOfBoundsException e) {
			log.info("ERROR: generation was failed, index " + position.toString() + " is outer of bounds");
		}
	}

	public static void setIntoCell(ArrayList<Cell> bufferCells, GameObject gameObject, Position position) {
		Objects.requireNonNull(
						bufferCells.stream()
								.filter(cell -> cell.position.equals(position))
								.findFirst()
								.orElse(null))
				.putIntoCell(gameObject);
	}

	static int CheckSize(RoomGenerate.RoomSize roomSize) {
		switch (roomSize) {
			case MIDDLE: {
				return 6;
			}
			case BIG: {
				return 16;
			}
		}
		return 0;
	}


	public static void placeCorner(DrawDirection pD, DrawDirection cD, Position fP) {
		if (pD == DrawDirection.DOWN && cD == DrawDirection.RIGHT
				|| pD == DrawDirection.LEFT && cD == DrawDirection.UP)
			setIntoCell(BorderObjects.LBCorner, fP);

		if (pD == DrawDirection.RIGHT && cD == DrawDirection.DOWN
				|| pD == DrawDirection.UP && cD == DrawDirection.LEFT)
			setIntoCell(BorderObjects.RTCorner, fP);

		if (pD == DrawDirection.UP && cD == DrawDirection.RIGHT
				|| pD == DrawDirection.LEFT && cD == DrawDirection.DOWN)
			setIntoCell(BorderObjects.LTCorner, fP);

		if (pD == DrawDirection.DOWN && cD == DrawDirection.LEFT
				|| pD == DrawDirection.RIGHT && cD == DrawDirection.UP)
			setIntoCell(BorderObjects.RBCorner, fP);

		if (pD == DrawDirection.RIGHT && cD == DrawDirection.LEFT)
			setIntoCell(BorderObjects.HWall, fP);

		if (pD == DrawDirection.LEFT && cD == DrawDirection.RIGHT)
			setIntoCell(BorderObjects.HWall, fP);

		if (pD == DrawDirection.UP && cD == DrawDirection.DOWN)
			setIntoCell(BorderObjects.VWall, fP);

		if (pD == DrawDirection.DOWN && cD == DrawDirection.UP)
			setIntoCell(BorderObjects.VWall, fP);
	}

    /*static boolean OutOfBounds(int XY, int OFX, int OFY, int CRX, int CRY, int SX, int SY){
        return (XY + Math.max(OFX, SX) + SX >= Math.min(CRX-1, CRY)
             || XY + Math.max(OFY, SY) + SY >= Math.min(CRX, CRY-1));
    }

    static int DoRandomXY(int x, int y, Random random){
        return random.nextInt(
                Math.min(x, y)
        );
    }*/

    /*static void PlaceMobs(Room_old roomOld, char[][] CurrentRoom, ArrayList<Position> SpawnPositions){

        for(Mob mob : roomOld.RoomCreatures){

            boolean NotPlaced = true;

            while(NotPlaced) {
                Position SpawnPosition = SpawnPositions.get(rnd.nextInt(SpawnPositions.size()));

                int y = SpawnPosition.y, x = SpawnPosition.x;

                if (!Scans.CheckCreature(CurrentRoom[y][x])) {
                    if (Scans.CheckWall(CurrentRoom[y][x])) {
                        CurrentRoom[y][x] = mob.getCreatureSymbol();
                        mob.setMobPosition(y, x);
                        NotPlaced = false;
                    }
                }
            }
        }
    }*/


    /*public static GameObject getBorderObject(String name){
        try {
            return (GameObject) BorderObjects.class.getField(name).get(GameObject.class);
        } catch (NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
            return null;
        }

    }*/

	public static class BorderObjects {
		static GameObject VWall = new Border(GameResources.getModel("Vwall"));

		static GameObject HWall = new Border(GameResources.getModel("Hwall"));

		static GameObject RBCorner = new Border(GameResources.getModel("RBCorner"));

		static GameObject RTCorner = new Border(GameResources.getModel("RTCorner"));

		static GameObject LBCorner = new Border(GameResources.getModel("LBCorner"));

		static GameObject LTCorner = new Border(GameResources.getModel("LTCorner"));

		static GameObject BCenter = new Border(GameResources.getModel("BCenter"));

		static GameObject TCenter = new Border(GameResources.getModel("TCenter"));
	}
}