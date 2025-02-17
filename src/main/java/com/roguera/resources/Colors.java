/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.resources;

import com.googlecode.lanterna.TextColor;

public class Colors {

	//Syntax for foreground - \u001b[38;5;#m
	//Syntax for background - \u001b[48;5;#m
	// \u001b = ESC;

	public static final String R = "\u001b[0m";

	/* Foreground colors */

	public static final String BLACK = "\u001b[38;5;0m";
	public static final String RED_BRIGHT = "\u001b[38;5;9m";
	public static final String RED_PASTEL = "\u001b[38;5;203m";
	public static final String GREEN_BRIGHT = "\u001b[38;5;10m";
	public static final String GREEN_PASTEL = "\u001b[38;5;150m";
	public static final String BLUE_BRIGHT = "\u001b[38;5;12m";
	public static final String BLUE_PASTEL = "\u001b[38;5;68m";
	public static final String MAGENTA = "\u001b[38;5;90m";
	public static final String CYAN = "\u001b[38;5;36m";
	public static final String CYAN_BRIGHT = "\u001b[38;5;51m";
	public static final String ORANGE = "\u001b[38;5;214m";
	public static final String PINK = "\u001b[38;5;207m";
	public static final String PINK_PASTEL = "\u001b[38;5;217m";
	public static final String LAVENDER_PASTEL = "\u001b[38;5;182m";
	public static final String VIOLET_PASTEL = "\u001b[38;5;183m";
	public static final String VIOLET = "\u001b[38;5;200m";
	public static final String BROWN = "\u001b[38;5;130m";
	public static final String GREY = "\u001b[38;5;253m";
	public static final String IRON = "\u001b[38;5;250m";
	public static final String COPPER = "\u001b[38;5;202m";
	public static final String GOLDEN = "\u001b[38;5;221m";
	public static final String DIAMOND = "\u001b[38;5;39m";
	public static final String DEEPBLUE = "\u001b[38;5;17m";
	public static final String WHITE_BRIGHT = "\u001b[38;5;15m";
	public static final String GREY_243 = "\u001b[38;5;243m";
	public static final String GREY_242 = "\u001b[38;5;242m";
	public static final String GREY_241 = "\u001b[38;5;241m";

	/* Background colors */

	public static final String B_RED_BRIGHT = "\u001b[48;5;9m";
	public static final String B_GREEN_BRIGHT = "\u001b[48;5;10m";
	public static final String B_BLUE_BRIGHT = "\u001b[48;5;12m";
	public static final String B_MAGENTA = "\u001b[48;5;90m";
	public static final String B_CYAN = "\u001b[48;5;50m";
	public static final String B_ORANGE = "\u001b[48;5;214m";
	public static final String B_PINK = "\u001b[48;5;207m";
	public static final String B_VIOLET = "\u001b[48;5;200m";
	public static final String B_DEEPBLUE = "\u001b[48;5;17m";
	public static final String B_DARKRED = "\u001b[48;5;52m";
	public static final String B_GREYSCALE_237 = "\u001b[48;5;237m";
	public static final String B_GREYSCALE_233 = "\u001b[48;5;233m";


	public static TextColor GetTextColor(String color) {
		//System.out.println("Color was: " + color);
		color = color.replaceFirst("\\u001b\\[\\d{2};\\d{1};", "#");
		color = color.replace('m', ' ');
		//System.out.println("Color is: " + color);
		return TextColor.Factory.fromString(color);
	}
}
