/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.mapgenerate;

import java.util.ArrayList;
import java.util.Objects;

import com.roguera.gamemap.Cell;
import com.roguera.gamemap.Position;
import com.roguera.gamemap.Scan;

public class GenerateRules {

	public static boolean IsNotOutOfBoundsRule(Position point, ArrayList<Cell> CurrentRoomCell) {
		return CurrentRoomCell.contains(
				CurrentRoomCell.stream()
						.filter(
								cell -> cell.position.equals(point))
						.findFirst()
						.orElse(new Cell(new Position(-1, -1)))
		);
	}

	public static boolean IsNotObstaclesRule(Position point, ArrayList<Cell> CurrentRoomCells) {
		return !Scan.checkWall(Objects.requireNonNull(CurrentRoomCells.stream()
				.filter(
						cell -> cell.position.equals(point))
				.findFirst()
				.orElse(null)));
	}

	public static boolean IsNotSameCellRule(Position point, ArrayList<Cell> CurrentRoomCells) {
		return Objects.requireNonNull(CurrentRoomCells.stream()
						.filter(
								cell -> cell.position.equals(point))
						.findFirst()
						.orElse(null))
				.isEmpty();
	}

}
