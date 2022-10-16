package com.roguera.creatures;

import com.roguera.gamemap.Position;


/**
 * <p>
 * Интерфейса "движимого" объекта. Описывает что должен реализовывать класс, объекты которого будут перемещаться на игровой карте.
 * </p>
 */
public interface Movable {
	void moveTo(Position position);
}
