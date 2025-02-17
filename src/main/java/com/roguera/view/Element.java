/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.view;

import com.googlecode.lanterna.TerminalPosition;
import com.roguera.gamemap.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Element {

    private static final Logger log = LoggerFactory.getLogger(Element.class);

	@Override
	public String toString() {
		return "Element{" +
				"ElementTitle='" + ElementTitle + '\'' +
				'}';
	}

	public String ElementName;

	public String ElementTitle;

	/**
	 * Позиция элемента начиная с данной точки слева направо.
	 */
	public Position ElementPosition;

	public Position ElementPointerPosition;

	public void setElementPointerPosition(Position elementPointerPosition) {
		log.info("CURSOR UI: Pointer position" + elementPointerPosition.toString());
		ElementPointerPosition = elementPointerPosition;
	}

	public Runnable ElementAction;

	public int ElementSize;

	public void setElementName(String elementName) {
		ElementName = elementName;
	}

	public void setElementTitle(String elementTitle) {
		ElementTitle = elementTitle;
	}

	public void setElementPosition(Position elementPosition) {
		ElementPosition = elementPosition;
	}

	public void setElementAction(Runnable elementAction) {
		ElementAction = elementAction;
	}

	public void setElementSize(int elementSize) {
		ElementSize = elementSize;
	}

	public TerminalPosition getTerminalPosition() {
		return new TerminalPosition(ElementPosition.x, ElementPosition.y);
	}

	public Element() {
	}

	public Element(String elementName, String elementTitle, Position elementPosition, Runnable elementAction) {
		ElementName = elementName;
		ElementTitle = elementTitle;
		ElementPosition = elementPosition;
		ElementAction = elementAction;
		ElementSize = elementTitle.length();
		ElementPointerPosition = elementPosition.getRelative(-1, 0);
	}
}
