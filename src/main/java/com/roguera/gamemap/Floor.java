package com.roguera.gamemap;

import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Floor implements Serializable {

	private static final Logger log = LoggerFactory.getLogger(Floor.class);

	private static int floorNumberCounter = 0;

	private final int floorNumber;

	private final ArrayList<Room> roomsOnFloor;

	public ArrayList<Room> getRooms() {
		return this.roomsOnFloor;
	}

	public Floor() {
		this.floorNumber = ++floorNumberCounter;
		this.roomsOnFloor = new ArrayList<>();
		log.info("[FLOOR " + floorNumber + "] New floor is created");
	}

	public int getFloorNumber() {
		return this.floorNumber;
	}

	public void putRoomInFloor(Room room) {
		log.info("[FLOOR " + floorNumber + "] putting room " + room.roomNumber + " into the floor");
		room.floorNumber = this.floorNumber;
		this.roomsOnFloor.add(room);
	}

	public static void resetCounter() {
		floorNumberCounter = 0;
	}

	public static void setCounterFromLoad(int currentFloorNumber) {
		floorNumberCounter = currentFloorNumber;
	}
}
