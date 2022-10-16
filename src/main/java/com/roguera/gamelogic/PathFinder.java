/*
 * Copyright (c) Ksenofontov N. 2020-2021.
 */

package com.roguera.gamelogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import com.roguera.gamemap.Cell;
import com.roguera.gamemap.Position;
import com.roguera.gamemap.Room;
import com.roguera.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathFinder {

	private final static Logger log = LoggerFactory.getLogger(PathFinder.class);

	private LinkedList<Node> openList;

	private LinkedList<Node> closeList;

	private final int HORIZONTAL = 10;

	private final int DIAGONAL = 14;

	ArrayList<Position> finalPath;

	public ArrayList<Position> getPathToTarget(Room map, Position target, Position start) {
		openList = new LinkedList<>();
		closeList = new LinkedList<>();
		finalPath = new ArrayList<>();
		//log.info("[A*]ALGORITHM IS STARTED");
/*        log.info("[A*]Room: "+map.RoomNumber);
        log.info("[A*]Target position: "+target.toString());
        log.info("[A*]Start position: "+start.toString());*/

		openList.add(new Node(map.getCell(start), null, 0, 0, 0));
/*        log.info("[A*]Add start node to open list: "+openList.getFirst().nodeCell.position.toString());

        log.info("[A*]Add start node to close list");*/
		closeList.add(openList.getFirst());

		while (!openList.isEmpty() && !targetNodeIsFind(target)) {

			findNodeSequence(target, closeList.getLast());

		}

		getFinalPath(closeList.getLast());

		try {
			finalPath.add(openList.getLast().nodeCell.position);
		} catch (NoSuchElementException e) {
			log.info("[A*]Can't find element");
		}

		//log.info("[A*]Final path("+finalPath.size()+"): ");

		//Показать путь движения в консоли
/*        if(Roguera.isDebug) {
            System.out.print("start -> ");
            for (Position pos : finalPath) {
                System.out.print(pos.toString() + " -> ");
            }
            System.out.print("end");
            System.out.println();
        }*/

		//log.info("[A*]ALGORITHM IS ENDED");
		return finalPath;
	}

	private void findNodeSequence(Position target, Node start) {

		Cell[] cellsAround = start.nodeCell.getCellsAround();

		openList.removeFirst();

		ArrayList<Cell> buffer = (ArrayList<Cell>)Arrays.stream(cellsAround).collect(Collectors.toList());

		buffer.removeIf(Objects::isNull);

		cellsAround = buffer.toArray(new Cell[0]);

		for (Cell cell : cellsAround) {
			if ((cell.isEmpty()
					|| cell.getFromCell().tag.equals("fog")
					|| cell.getFromCell() instanceof Player)
					&& closeList.stream().noneMatch(node -> node.nodeCell.position.equals(cell.position))) {

				int G = (start.nodeCell.position.x == cell.position.x || start.nodeCell.position.y == cell.position.y ? HORIZONTAL : DIAGONAL);

				int H = manhattanLong(target, cell.position) * 10;

				int F = G + H;

				if (openList.stream().anyMatch(node -> node.nodeCell.position.equals(cell.position))) {
					Node n = openList.stream().filter(node -> node.nodeCell.position.equals(cell.position)).findFirst().get();
					if (n.G > G) {
						n.G = G;
						n.rootNode = start;
					}
				} else {
					openList.add(new Node(cell, start, F, G, H));
					if (targetNodeIsFind(target)) {
						// log.info("[A*]Target node is find!");
						return;
					}
				}
			}
		}
		try {
			Node nextNode = openList.stream().min(comparingF).get();
            /*log.info("[A*]Node F = "+nextNode.F);
            log.info("[A*]Next node position: "+nextNode.nodeCell.position);
            log.info("[A*]Next node root position: "+nextNode.rootNode.nodeCell.position);
            log.info("[A*]Add next node into close list");*/
			closeList.add(nextNode);
		} catch (NoSuchElementException e) {
			log.info("[A*]Can't find element");
		}

	}

	private int manhattanLong(Position target, Position currentCell) {
		int xT = target.x;
		int yT = target.y;

		int xC = currentCell.x;
		int yC = currentCell.y;

		int xD = 0;
		int yD = 0;

		if (xT != xC) {
			xD = Math.abs(xT - xC);
		}

		if (yC != yT) {
			yD = Math.abs(yT - yC);
		}

		return xD + yD;
	}

	Comparator<Node> comparingF = (Node n1, Node n2) -> {
		if (n1.F > n2.F) {
			return 1;
		} else if (n2.F > n1.F) {
			return -1;
		} else {
			return 0;
		}
	};

	private boolean targetNodeIsFind(Position target) {
		return openList.stream().anyMatch(node -> node.nodeCell.position.equals(target));
	}

	private void getFinalPath(Node prevNode) {
		if (prevNode.rootNode != null) {
			getFinalPath(prevNode.rootNode);
		}
		finalPath.add(prevNode.nodeCell.position);
	}

	private static class Node {
		Cell nodeCell;
		Node rootNode;

		int F, G, H;

		public Node(Cell thisCell, Node rootNode, int F, int G, int H) {
			this.nodeCell = thisCell;
			this.rootNode = rootNode;
			this.F = F;
			this.G = G;
			this.H = H;
		}
	}

}
