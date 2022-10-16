package com.roguera.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.roguera.net.PlayerDTO;

public class SavePlayerDTO {

	public static void save(PlayerDTO playerDTO) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(playerDTO.getNickName() + ".auth"));
			outputStream.writeObject(playerDTO);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
