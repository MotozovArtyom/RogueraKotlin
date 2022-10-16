package com.roguera.gamelogic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.roguera.gamemap.Dungeon;
import com.roguera.gamemap.Floor;
import com.roguera.player.Player;
import com.roguera.player.PlayerData;
import com.roguera.resources.GameResources;
import com.roguera.view.ViewObjects;
import com.roguera.resources.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveLoadSystem {

    private static final Logger log = LoggerFactory.getLogger(SaveLoadSystem.class);

	private static File currentSaveFile;

	private static PlayerData playerDataFile;

	private static ObjectOutputStream saveFileStream;

	private static File saveFile;

	private static File saveFileTemp;

	private static final String DIRECTORY = "./saves/";

	private static final String EXTENSION = ".sav";

	/**
	 * Сохранение данных игрока в файл
	 *
	 * @throws IOException если сохранение не удалось
	 */
	public static void saveGame() throws IOException {

		createSaveDirectory();

		playerDataFile = Dungeon.player.getPlayerData();

		putDataInSaveFile();

		String playerName = ViewObjects.getTrimString(playerDataFile.getPlayerName());

		String saveFileName = DIRECTORY + playerName + EXTENSION;

		saveFile = new File(saveFileName);

		saveFileStream = new ObjectOutputStream(new FileOutputStream(saveFile));

		try {

			trySaveToTemp();

		} catch (IOException e) {
			log.info(Colors.RED_BRIGHT + "[ERROR][SAVE] Error with save file");
			e.printStackTrace();
			return;
		}

		saveFileStream.writeObject(playerDataFile);

		saveFileStream.flush();

		saveFileStream.close();

		log.info("[SAVE] room " + playerDataFile.getCurrentRoom().roomNumber + " has saved");

		log.info("[SAVE] success! " + saveFileName + " has created");

		log.info("[SAVE] remove temp file");

		saveFileTemp.delete();

		log.info("[SAVE]Player data info: " + playerDataFile.toString());
	}

	public static void loadGame(String saveFileName) throws IOException, ClassNotFoundException {

		FileInputStream saveFileInputStream = new FileInputStream(saveFileName);

		ObjectInputStream saveObjectInputStream = new ObjectInputStream(saveFileInputStream);

		log.info("[LOAD]Loading game from file: " + saveFileName);

		PlayerData savedFile = (PlayerData)saveObjectInputStream.readObject();


		try {
			getDataFromSavedFile(savedFile);
		} catch (IndexOutOfBoundsException e) {
			log.info(Colors.RED_BRIGHT + "[ERROR][LOAD] load game was failed. See stacktrace:");
			e.printStackTrace();
		}

		saveObjectInputStream.close();

		saveFileInputStream.close();

		log.info("[LOAD]Save file is loaded");
	}

	private static void createSaveDirectory() {
		//"./saves/"
		File saveDirectory = new File(DIRECTORY);

		if (!saveDirectory.exists()) {
			saveDirectory.mkdir();
		}
	}

	private static void putDataInSaveFile() {
		playerDataFile.setPlayerPositionData(Dungeon.player.playerPosition);
		playerDataFile.setPlayerInventory(Dungeon.player.Inventory);
		playerDataFile.setPlayerQuickEquipment(Dungeon.player.quickEquipment);
		playerDataFile.setPlayerEquipment(Dungeon.player.Equipment);
		playerDataFile.setSaveFileVersion(GameResources.VERSION);
		playerDataFile.setCurrentFloor(Dungeon.getCurrentFloor().get());
		playerDataFile.setCurrentRoom(Dungeon.getCurrentRoom());
		playerDataFile.set_atkPotionBonus(0);
		playerDataFile.set_defPotionBonus(0);
		playerDataFile.resetScore();
	}

	private static void getDataFromSavedFile(PlayerData savedFile) throws IndexOutOfBoundsException {
		Dungeon.player = new Player(savedFile);

		int floorNumber = savedFile.getCurrentFloor().getFloorNumber();

		Floor.setCounterFromLoad(floorNumber);

		Dungeon.currentFloorNumber = floorNumber;

		Dungeon.floors.add(savedFile.getCurrentFloor());

		Dungeon.rooms = savedFile.getCurrentFloor().getRooms();

		Dungeon.savedRoom = savedFile.getCurrentRoom();

		Dungeon.player.setCurrentRoom((byte)savedFile.getCurrentRoom().roomNumber);

		//int scoreHash = JDBСQueries.getUserScoreHash();

	}

	private static void trySaveToTemp() throws IOException {

		saveFileTemp = new File(DIRECTORY + "_temp_" + EXTENSION);

		ObjectOutputStream saveFileTempStream = new ObjectOutputStream(new FileOutputStream(saveFileTemp));

		saveFileTempStream.writeObject(playerDataFile);

		saveFileTempStream.flush();

		saveFileTempStream.close();
	}

	public static String getSaveFileName() {
		File directory = new File("./saves/");

		ArrayList<File> files = new ArrayList<>(List.of(Objects.requireNonNull(directory.listFiles(((dir, name) -> name.endsWith(".sav"))))));

		files.sort(Comparator.comparingLong(File::lastModified));

		try {
			log.info("[LOAD] Load file with name: " + files.get(files.size() - 1).getName());
			return "./saves/" + files.get(files.size() - 1).getName();
		} catch (NullPointerException e) {
			log.info("[ERROR] Save file exist but not found");
			log.info(e.getMessage());
			return "";
		}
	}

	public static boolean saveFileExists() {
		File directory = new File("./saves/");

		File[] files = directory.listFiles((dir, name) -> name.endsWith(".sav"));
		if (files != null) {
			return files.length > 0;
		} else {
			return false;
		}
	}

	public static boolean deleteSaveFile() {
		currentSaveFile = new File(ViewObjects.getTrimString(Dungeon.player.getName()) + ".sav");
		return currentSaveFile.delete();
	}
}
