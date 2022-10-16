/*
 * Copyright (c) Ksenofontov N. 2020-2021.
 */

package com.roguera.view.ui;

import java.io.IOException;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.roguera.view.Draw;
import com.roguera.view.IViewBlock;
import com.roguera.view.TerminalView;
import com.roguera.gamemap.Cell;
import com.roguera.gamemap.Dungeon;
import com.roguera.gamemap.Position;
import com.roguera.gamemap.Room;
import com.roguera.view.ViewObjects;

public class MapView implements IViewBlock {

	private TextGraphics MapViewGraphics = null;

	private Room currentRoom;

	public MapView() {
		ViewObjects.ViewBlocks.add(this);
	}

	public void setRoom(Room room) {
		this.currentRoom = room;
	}

	public TerminalSize size = new TerminalSize(25, 30);

	@Override
	public void Init() {
		try {
			MapViewGraphics = TerminalView.terminal.newTextGraphics();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void Draw() {
		currentRoom.getCells().forEach(
				cell -> TerminalView.putCharInTerminal(MapViewGraphics, cell.getFromCell().getModel(), cell.position)
		);
	}

	public void drawAround() {
		drawAround(Dungeon.player.playerPosition);
		TerminalView.putCharInTerminal(MapViewGraphics, Dungeon.player.getModel(), Dungeon.player.playerPosition);
		Draw.flush();
	}

	public void drawAround(Position position) {
		for (Cell cell : Dungeon.getCurrentRoom().getCell(position).getCellsAround()) {
			try {
				if (!(cell.getFromCell().equals(Dungeon.player)))
					TerminalView.putCharInTerminal(MapViewGraphics, cell.getFromCell().getModel(), cell.position);
			} catch (NullPointerException ignored) {
			}
		}
		Draw.flush();
	}

	@Override
	public void Reset() {
		MapViewGraphics.fillRectangle(new TerminalPosition(0, 0), size, ' ');
	}
    /*
    private TextGraphics MapDrawGraphics = null;

    private char cell;
    private final Position CellPosition = new Position();
    private HashMap<Integer, String> ColoredItemsID;
    private final Predicate<Item> GetItemByPosition = item -> item.ItemPosition.equals(CellPosition);

    public GameMapBlock(){
        ViewObjects.ViewBlocks.add(this);
    }

    public void Init(){
        try {
            MapDrawGraphics = com.roguera.main.view.TerminalView.terminal.newTextGraphics();

            ColoredItemsID = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
     */
}