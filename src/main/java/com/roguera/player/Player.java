/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.player;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.roguera.gamemap.Cell;
import com.roguera.gamemap.Position;
import com.roguera.items.Armor;
import com.roguera.items.Equipment;
import com.roguera.items.Gold;
import com.roguera.items.Item;
import com.roguera.items.Potion;
import com.roguera.items.Weapon;
import com.roguera.view.Draw;
import com.roguera.view.ViewObjects;
import com.roguera.creatures.Creature;
import com.roguera.gamelogic.RogueraGameSystem;
import com.roguera.gamemap.Dungeon;
import com.roguera.resources.Colors;
import com.roguera.resources.GameResources;
import com.roguera.resources.Model;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player extends Creature {

    private static final Logger log = LoggerFactory.getLogger(Player.class);

	final transient private DiscordRichPresence playerRichPresence;

	private final String defaultName = "Player" + ThreadLocalRandom.current().nextInt(1, 10000);

	public Position playerPosition = new Position();

	private PlayerData playerData;

	private byte currentRoom = 1;

	private String pname;

	@Override
	public Model setModel(Model model) {
		return super.setModel(model);
	}

	public HashMap<String, com.roguera.items.Equipment> Equipment = new HashMap<>();

	public ArrayList<Item> Inventory = new ArrayList<>();

	public ArrayList<Equipment> quickEquipment = new ArrayList<>(5);

	public boolean putUpItem(Item item) {
		//log.info("[PLAYER]Picked up item: "+item.toString());
		if (item instanceof Equipment) {
			return autoEquip((Equipment)item);
		} else if (item instanceof Gold) {
			return getPlayerData().setMoney(((Gold)item).getAmount());
		} else {
			//log.info("\ninto inventory");
			if (putIntoInventory(item)) {

				ViewObjects.logView.playerActionPickUp(item.getName());

				Draw.call(ViewObjects.infoGrid.getFirstBlock());

				Draw.call(ViewObjects.infoGrid.getThirdBlock());

				return true;
			}
			return false;
		}
	}

	private boolean isGreaterStats(Item item, String key) {
		return ((Equipment)item).getStats() > getEquipmentFromSlot(key).orElse(com.roguera.items.Equipment.BLANK).getStats();
	}

	private boolean autoEquip(Equipment equipment) {
		//removeBlank();
		if (equipment instanceof Weapon) {
			if (getEquipmentFromSlot("FirstWeapon").isPresent() && isGreaterStats(equipment, "FirstWeapon")) {
				return switchEquipment(equipment, "FirstWeapon");
			} else if (getEquipmentFromSlot("FirstWeapon").isEmpty()) {
				return putIntoEquipment(equipment, "FirstWeapon");
			} else {
				//log.info("\ninto inventory");
				return putIntoInventory(equipment);
			}
		} else if (equipment instanceof Armor) {
			if (getEquipmentFromSlot("Armor").isPresent() && isGreaterStats(equipment, "Armor")) {
				return switchEquipment(equipment, "Armor");
			} else if (getEquipmentFromSlot("Armor").isEmpty()) {
				return putIntoEquipment(equipment, "Armor");
			} else {
				//log.info("\ninto inventory");
				return putIntoInventory(equipment);
			}
		} else {
			//log.info("\ninto inventory");
			return putIntoInventory(equipment);
		}
	}

	private boolean putIntoInventory(Item item) {
		if (quickEquipment.toArray().length < 5) {
			if (item instanceof Potion)
				return quickEquipment.add((Equipment)item);
			else {
				return tryToPutIntoInventory(item);
			}
		} else {
			return tryToPutIntoInventory(item);
		}
	}

	public boolean tryToPutIntoInventory(Item item) {
		if (Inventory.size() < 10) {
			return Inventory.add(item);
		} else {
			ViewObjects.logView.putLog("Your inventory is full!");
			return false;
		}
	}

	public boolean putIntoQuickMenu(Item item, int index) {
		if (index <= 5) {
			//log.info("[PLAYER_INVENTORY] Put item "+item+ " into quick menu on index "+ index);
			try {
				if (quickEquipment.toArray().length < 5) {
					quickEquipment.add(index - 1, (com.roguera.items.Equipment)item);
					return true;
				} else {
					return false;
				}
			} catch (IndexOutOfBoundsException e) {
				log.info(Colors.RED_BRIGHT + "[ERROR][PLAYER_INVENTORY] Index " + index + " out of bounds");
				return false;
			}
		} else {
			return false;
		}
	}

	public Optional<Equipment> getEquipmentFromSlot(String key) {
		return Optional.ofNullable(Equipment.get(key));
	}

	public boolean equipItemIntoFirstSlot(Equipment eq) {
		if (eq instanceof Weapon)
			return switchEquipment(eq, "FirstWeapon");
		else if (eq instanceof Armor)
			return switchEquipment(eq, "Armor");
		return false;
	}

	public void equipItemFromQuickSlot(Equipment eq) {
		int index = quickEquipment.indexOf(eq);
		quickEquipment.remove(eq);
		String place = "";
		if (eq instanceof Weapon) {
			place = "FirstWeapon";
		} else if (eq instanceof Armor) {
			place = "Armor";
		}
		quickEquipment.add(index, Equipment.remove(place));
		putIntoEquipment(eq, place);
	}

	private boolean switchEquipment(Equipment toEquip, String place) {
		putIntoInventory(Equipment.remove(place));
		return putIntoEquipment(toEquip, place);
	}

	private boolean putIntoEquipment(Equipment equipment, String place) {
		Equipment.put(place, equipment);
		playerData.recountStats(equipment.equipmentStat, equipment.getStats());
		Draw.call(ViewObjects.infoGrid.getFirstBlock());
		return true;
	}

	private void removeBlank() {
		if (!Equipment.get("FirstWeapon").getName().equals("blank"))
			Equipment.remove("FirstWeapon", com.roguera.items.Equipment.BLANK);
	}

	@Override
	public void placeObject(Cell cell) {
		this.playerPosition = cell.position;
		this.cellPosition = this.playerPosition;
		cell.gameObjects.add(this);
	}

	{
		Equipment.put("FirstWeapon", null);
		Equipment.put("Armor", null);
	}

	public Cell getFrontCell() {
		return Dungeon.getCurrentRoom().getCell(playerPosition.getRelative(0, 1));
	}

	public Cell[] lookAround() {
		Cell[] cells = new Cell[8];
		int i = 0;
		for (Position direction : Position.AroundPositions) {
			cells[i] = Dungeon.getCurrentRoom().getCell(playerPosition.getRelative(direction));
			i++;
		}
		return cells;
	}

	@Override
	protected Equipment findEquipmentInInventoryByTag(String tag) {
		try {
			return this.Equipment.values().stream().filter(eq -> eq.tag.startsWith(tag)).findFirst().get();
		} catch (NoSuchElementException | NullPointerException e) {
			return com.roguera.items.Equipment.BLANK;
		}
	}

	public Player() {
		super();

		this.tag += ".player";

		try {
			playerData = new PlayerData();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		playerData.setPlayerName(defaultName);

		setModel(new Model("Player", Colors.GREEN_BRIGHT, GameResources.PLAYER_MODEL));

		playerData.setHP(100);

		this.name = playerData.getPlayerName();

		this.cellPosition = this.playerPosition;

		playerRichPresence = new DiscordRichPresence.Builder("Ready to crawling dungeon")
				.setStartTimestamps(Instant.now().getEpochSecond()).setBigImage("icon", "Roguera").build();

		pname = ViewObjects.getTrimString(playerData.getPlayerName());
	}

	public Player(PlayerData playerData) {
		super();

		this.tag += ".player";

		setModel(new Model("Player", Colors.GREEN_BRIGHT, GameResources.PLAYER_MODEL));

		this.setPlayerData(playerData);

		this.playerPosition = playerData.getPlayerPositionData();

		this.HP = playerData.getHP();

		this.currentRoom = (byte)playerData.getCurrentRoom().roomNumber;

		this.name = playerData.getPlayerName();

		this.cellPosition = playerData.getPlayerPositionData();

		this.playerPosition = playerData.getPlayerPositionData();

		this.Inventory = getPlayerData().getPlayerInventory();

		this.Equipment = getPlayerData().getPlayerEquipment();

		this.quickEquipment = getPlayerData().getPlayerQuickEquipment();

		playerRichPresence = new DiscordRichPresence.Builder("Ready to crawling dungeon")
				.setStartTimestamps(Instant.now().getEpochSecond()).setBigImage("icon", "Roguera").build();

		this.pname = this.name;
	}

	@Override
	public String getName() {
		return this.playerData.getPlayerName();
	}

	@Override
	public int getHP() {
		return playerData.getHP();
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	public byte getCurrentRoom() {
		return currentRoom;
	}

	public void setCurrentRoom(byte currentRoom) {
		this.currentRoom = currentRoom;
	}

	public void updateRichPresence() {
		pname = ViewObjects.getTrimString(playerData.getPlayerName());
		playerRichPresence.details = pname + " (LVL " + playerData.getLevel() + ")";
		playerRichPresence.state = "Floor " + Dungeon.getCurrentFloor().get().getFloorNumber() + "|" + " Room " + currentRoom;
		DiscordRPC.discordUpdatePresence(playerRichPresence);
	}

	@Override
	public void getHit(int incomingDamage) {
		int fullDef = getDefenceByEquipment();
		int deltaDmg = incomingDamage - fullDef;
		this.playerData.setHP(this.playerData.getHP() - Math.max(0, deltaDmg));
	}

	@Override
	public int getDamageByEquipment() {
		return this.playerData.get_baseAtk() + this.getPlayerData().get_atk() + this.getPlayerData().get_atkPotionBonus(); //+ findEquipmentInInventoryByTag("item.equipment.weapon.").getStats();
	}

	@Override
	public int getDefenceByEquipment() {
		return this.playerData.get_baseDef() + this.getPlayerData().get_def() + this.getPlayerData().get_defPotionBonus(); //+ findEquipmentInInventoryByTag("item.equipment.armor.").getStats();
	}

	public void checkNewLevel() {
		if (this.playerData.getExp() >= this.playerData.getRequiredEXP()) {
			this.playerData.setLevel(this.playerData.getLevel() + 1);
			this.playerData.updBaseATK();
			this.playerData.updRequiredEXP();
			this.playerData.updBaseDEF();
			this.playerData.setMaxHP(RogueraGameSystem.getNextHP());

			ViewObjects.logView.playerAction("get the new level!");
			Draw.call(ViewObjects.infoGrid.getFirstBlock());
		}
	}

	public void setPlayerData(PlayerData playerData) {
		this.playerData = playerData;
	}

}
