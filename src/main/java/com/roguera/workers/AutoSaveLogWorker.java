package com.roguera.workers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.roguera.gamemap.Dungeon;
import com.roguera.view.ViewObjects;
import com.roguera.resources.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoSaveLogWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(AutoSaveLogWorker.class);

	private ObjectOutputStream outputLogStream;

	private FileOutputStream fileOutputStream;

	private File logFile;

	private final String FILE_EXTENSION = ".txt";

	private String fileName;

	private final File logDirectory = new File("logs/");

	@Override
	public void run() {

		this.fileName = ViewObjects.getTrimString(Dungeon.player.getPlayerData().getPlayerName() + "_" + getLocalTime() + FILE_EXTENSION);

		logDirectory.mkdir();

		logFile = new File(logDirectory + "/" + fileName);

		log.info("[AUTO_SAVE_LOG_WORKER] Starting...");

		log.info("[AUTO_SAVE_LOG_WORKER] File name to save: " + fileName);

		try {
			while (!Thread.currentThread().isInterrupted()) {
				TimeUnit.SECONDS.sleep(3);
				saveLogToFile();
			}
		} catch (InterruptedException | FileNotFoundException e) {
			if (Optional.ofNullable(e.getCause()).isPresent()) {
				log.info(Colors.RED_BRIGHT + "[ERROR] Error in autosave log worker:");
				e.printStackTrace();
			} else {
				try {
					saveLogToFile();
				} catch (IOException | ConcurrentModificationException ex) {
					log.info("[AUTO_SAVE_LOG_WORKER] Try to restart...");
					run();
				}
			}
		} catch (IOException e) {
			log.info(Colors.RED_BRIGHT + "[ERROR] Error in autosave log worker:");
			e.printStackTrace();
		}
		log.info("[AUTO_SAVE_LOG_WORKER] Ended");
	}

	private void saveLogToFile() throws IOException {
		fileOutputStream = new FileOutputStream(logFile);
		log.info(Colors.RED_BRIGHT + "[ERROR] Error in autosave log worker:");
	}

	private String getLocalTime() {
		int year = java.time.Year.now().getValue();
		int month = java.time.LocalDate.now().getMonth().getValue();
		int day = LocalDate.now().getDayOfMonth();
		int hour = LocalTime.now().getHour();
		int minute = LocalTime.now().getMinute();
		int second = LocalTime.now().getSecond();
		return "" + year + month + day + "_" + hour + minute + second;
	}
}
