package com.roguera.gamelogic;

import java.io.Serializable;

public interface NPCConsumeAction<T> extends Serializable {
	void take(T item);
}
