package com.roguera.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.roguera.gamemap.Dungeon;
import com.roguera.gamemap.Position;
import com.roguera.input.CursorUI;
import com.roguera.input.Input;
import com.roguera.items.Equipment;
import com.roguera.items.Item;
import com.roguera.items.Usable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.roguera.view.ViewObjects.infoGrid;

public class InventoryWindow extends Window {

	private static final Logger log = LoggerFactory.getLogger(InventoryWindow.class);

	private final int _width = 25;
	private final int _height = 5;

	private final TerminalPosition _inventoryWindowPosition = new TerminalPosition(20, 5);

	private TerminalSize _inventoryWindowSize = new TerminalSize(_width, _height);

	private final ArrayList<Item> playerInventory;

	private ArrayList<Element> menuElements;

	private CursorUI cursorUI;

	public InventoryWindow() {
		super();

		this.playerInventory = Dungeon.player.Inventory;

		_inventoryWindowSize = _inventoryWindowSize.withRelative(getMaxLenghtNameOfItems(), playerInventory.size());

		makeWindow(_inventoryWindowPosition, _inventoryWindowSize);

		fillElements();
		cursorUI = new CursorUI(menuElements);
	}

	private int getMaxLenghtNameOfItems() {
		String s = "";
		if (!playerInventory.isEmpty())
			s = playerInventory.stream().sorted(Comparator.comparing(Item::getName)).collect(Collectors.toList()).get(0).getName();
		return s.length();
	}

	private void fillElements() {
		menuElements = new ArrayList<>();
		for (Item item : this.playerInventory) {
			menuElements.add(new Element(
					item.getName(),
					item.model.toString() + " " + item.getName() + " [" + ((Equipment)item).getStats() + "]",
					new Position(1, this.playerInventory.indexOf(item)),
					this::contextFocus
			));
		}
		if (Dungeon.player.quickEquipment.size() > 0) {
			menuElements.add(new Element(
					"Get from quick slot",
					"Get from quick slot",
					new Position(1, menuElements.size()),
					() -> {
						putStringIntoWindow("Input number of slot", new Position(1, menuElements.size() + 1));
						Draw.flush();
						Optional<KeyStroke> key = Input.waitForInput();
						if (key.isPresent()) {
							if (key.get().getKeyType() == KeyType.Character) {
								try {
									int num = Integer.parseInt(String.valueOf(key.get().getCharacter()));
									if (Dungeon.player.quickEquipment.size() >= num) {
										if (Dungeon.player.tryToPutIntoInventory(Dungeon.player.quickEquipment.get(num - 1))) {
											Dungeon.player.quickEquipment.remove(num - 1);
											fillElements();
											Draw.reset(infoGrid.getThirdBlock());
											Draw.call(infoGrid.getThirdBlock());
											Draw.call(this);
											cursorUI = new CursorUI(menuElements);
										}
									}
								} catch (NumberFormatException e) {
									log.info("[ERROR][INVENTORY] wrong character!");
								}
							}
						}
					}
			));
		}
	}

	@Override
	protected void content() {
		for (Element element : menuElements) {
			putElementIntoWindow(element);
		}
		putStringIntoWindow("Items " + playerInventory.size() + "/10", new Position(5, -1));
		if (!menuElements.isEmpty())
			try {
				setPointerToElement(menuElements.get(cursorUI.indexOfElement), cursorUI.cursorPointer);
			} catch (IndexOutOfBoundsException e) {
				resetPointer();
			}
		else
			putStringIntoWindow("Your inventory is empty!", new Position(0, 0));
	}

	private void resetPointer() {
		cursorUI.indexOfElement = Math.abs(cursorUI.indexOfElement - menuElements.size());
		setPointerToElement(menuElements.get(cursorUI.indexOfElement), cursorUI.cursorPointer);
	}

	@Override
	protected void input() {
		if (!menuElements.isEmpty()) {
			cursorUI.setFirstElementCursorPosition();

			focusToItems();
		} else
			super.input();
	}

	private void contextFocus() {
		new ContextMenuWindow(this.playerInventory.get(cursorUI.indexOfElement),
				new Position(_inventoryWindowPosition.getColumn(),
						_inventoryWindowPosition.getRow() - 5), new TerminalSize(_inventoryWindowSize.getColumns(), 5)).show();
		isOpen = true;
	}

	private void focusToItems() {
		while (!menuElements.isEmpty()) {
			Optional<KeyStroke> keyStroke = Input.waitForInput();
			if (keyStroke.isPresent()) {
				if (Input.keyIsEscape(keyStroke.get()))
					break;
				cursorUI.SelectElementV(keyStroke.get().getKeyType());
				Draw.call(this);
			}
		}
	}

	private class ContextMenuWindow extends Window {

		private final Item selectedItem;

		private ArrayList<Element> contextMenuElements = new ArrayList<>();

		private final CursorUI contextMenuCursor;

		private boolean contextMenuIsOpen;

		private final Runnable equip = () -> {
			int index = cursorUI.indexOfElement;
			Item item = Dungeon.player.Inventory.get(index);
			Dungeon.player.Inventory.remove(item);
			if (item instanceof Equipment) {
				if (item instanceof Usable) {
					((Usable)item).use();
				} else {
					Dungeon.player.equipItemIntoFirstSlot((Equipment)item);
				}
			}
			menuElements.remove(index);

			InventoryWindow.this.fillElements();
			Draw.call(infoGrid.getThirdBlock());
			Draw.call(InventoryWindow.this);
			Draw.call(this);

		};

		private final Runnable drop = () -> {
			int index = cursorUI.indexOfElement;
			Dungeon.getCurrentRoom().getCell(Dungeon.player.playerPosition.getRelative(1, 0)).putIntoCell(Dungeon.player.Inventory.remove(index));
			menuElements.remove(index);
			InventoryWindow.this.fillElements();
			Draw.call(InventoryWindow.this);
			ViewObjects.mapView.drawAround();
		};

		private final Runnable putIntoQuickMenu = () -> {
			int index = cursorUI.indexOfElement;
			putStringIntoWindow("Input number of quick slot", new Position(0, 1));
			putStringIntoWindow("You can put " +
							(Dungeon.player.quickEquipment.size() > 0 ? "up for " + (Dungeon.player.quickEquipment.size() + 1) + " slot" : "into first slot only"),
					new Position(0, 2));
			Item item = Dungeon.player.Inventory.get(index);

			Draw.flush();
			Optional<KeyStroke> keyStroke = Input.waitForInput();
			if (keyStroke.isPresent()) {
				if (keyStroke.get().getKeyType() == KeyType.Character) {

					Character number = keyStroke.get().getCharacter();

					int numberOfSlot = Integer.parseInt(String.valueOf(number));

					if (Dungeon.player.quickEquipment.size() >= numberOfSlot - 1) {
						if (Dungeon.player.putIntoQuickMenu(item, numberOfSlot)) {

							Dungeon.player.Inventory.remove(item);
							InventoryWindow.this.menuElements.remove(index);

							if (menuElements.size() < 2) {
								contextMenuIsOpen = false;
							}

							InventoryWindow.this.fillElements();
						}
						Draw.call(infoGrid.getThirdBlock());
						Draw.call(InventoryWindow.this);
					}
				}
			}
		};

		private final Runnable back = () -> {
			contextMenuIsOpen = false;
			Draw.reset(this);
		};

		private final Runnable[] menuActions = {equip, drop, putIntoQuickMenu, back};

		public ContextMenuWindow(Item item, Position position, TerminalSize size) {
			super(position.toTerminalPosition(), size);
			this.selectedItem = item;
			if (selectedItem instanceof Usable) {
				menuOptions[0] = "Use";
			}
			drawContextMenu();
			contextMenuCursor = new CursorUI(contextMenuElements);
		}

		String[] menuOptions = {"Equip", "Drop", "QuickMenu", "Back"};

		@Override
		protected void content() {
			for (Element element : contextMenuElements) {
				putElementIntoWindow(element);
			}
			try {
				setPointerToElement(contextMenuElements.get(contextMenuCursor.indexOfElement), contextMenuCursor.cursorPointer);
			} catch (IndexOutOfBoundsException e) {
				resetPointer();
			}
			super.content();
		}

		private void drawContextMenu() {
			int offsetX = 0;

			contextMenuElements = new ArrayList<>();

			for (int i = 0; i < menuOptions.length; i++) {
				contextMenuElements.add(new Element(
						menuOptions[i],
						menuOptions[i],
						new Position((i > 0 ? offsetX += menuOptions[i - 1].length() + 2 : 1), 0),
						menuActions[i]));
			}
		}

		private void resetPointer() {
			contextMenuCursor.indexOfElement = Math.abs(contextMenuCursor.indexOfElement - contextMenuElements.size());
			setPointerToElement(contextMenuElements.get(contextMenuCursor.indexOfElement), contextMenuCursor.cursorPointer);
		}

		@Override
		protected void input() {
			contextMenuIsOpen = true;
			contextMenuCursor.setFirstElementCursorPosition();
			while (contextMenuIsOpen) {
				Optional<KeyStroke> keyStroke = Input.waitForInput();
				if (keyStroke.isPresent()) {
					if (Input.keyIsEscape(keyStroke.get()))
						break;
					contextMenuCursor.SelectElementH(keyStroke.get().getKeyType());
					Draw.call(this);
				}
			}
		}
	}
}

