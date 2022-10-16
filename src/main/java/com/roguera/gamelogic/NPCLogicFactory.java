/*
 * Copyright (c) Ksenofontov N. 2020-2021.
 */

package com.roguera.gamelogic;

import java.io.Serializable;
import java.util.ArrayList;

import com.roguera.items.Item;
import com.roguera.view.TraderWindow;

public class NPCLogicFactory implements Serializable {

	private static final NPCConsumeAction<ArrayList<Item>> tradeItems = (ArrayList<Item> traderInventory) -> {
		new TraderWindow(traderInventory).show();
	};

	public static NPCConsumeAction<ArrayList<Item>> getTraderLogic() {
		return tradeItems;
	}
}
