/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.view;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draw {

	private static final Logger log = LoggerFactory.getLogger(Draw.class);

	public static int DrawCallCount = 0;
	public static int DrawResetCount = 0;
	public static int DrawInitCount = 0;

	public static void call(IViewBlock viewBlock) {
		DrawCallCount++;
		//log.info("[DRAW_CALL]: "+viewBlock.getClass().getName());
		viewBlock.Draw();
		flush();
	}

	public static void reset(IViewBlock viewBlock) {
		DrawResetCount++;
		viewBlock.Reset();
		flush();
	}

	public static void init(IViewBlock viewBlock) {
		DrawInitCount++;
		viewBlock.Init();
		flush();
	}

	public static void flush() {
		try {
			TerminalView.terminal.flush();
		} catch (IOException e) {
			log.info(Arrays.toString(e.getStackTrace()));
		}
	}

	public static void clear() {
		try {
			TerminalView.terminal.clearScreen();
		} catch (IOException e) {
			log.info(Arrays.toString(e.getStackTrace()));
		}
	}

}
