package com.roguera.cinematic;

import java.util.ArrayList;

import com.roguera.base.GameObject;
import com.roguera.creatures.Movable;
import com.roguera.gamemap.Position;
import com.roguera.gamemap.Room;
import com.roguera.view.Draw;
import com.roguera.view.ViewObjects;
import com.roguera.resources.Model;

public class Actor extends GameObject implements Movable {

	private static int actorCounter = 0;

	private final ArrayList<Runnable> actionSequences;

	private final Room sceneField;

	public Actor(String name, Model actorModel, Room sceneField) {
		this.id = ++actorCounter;
		this.model = actorModel;
		this.sceneField = sceneField;
		this.actionSequences = new ArrayList<>();
	}

	public void addAction(Runnable action) {
		actionSequences.add(action);
	}

	public void doSequence(int index) {
		actionSequences.get(index).run();
	}

	public ArrayList<Runnable> getSequences() {
		return actionSequences;
	}

	@Override
	public void moveTo(Position position) {
		sceneField.getCell(this.cellPosition).removeFromCell(this);
		sceneField.getCell(position).putIntoCell(this);
		this.cellPosition = position;
		Draw.call(ViewObjects.mapView);
	}
}
