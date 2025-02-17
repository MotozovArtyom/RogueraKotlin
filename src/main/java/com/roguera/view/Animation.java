package com.roguera.view;

import java.util.concurrent.TimeUnit;

import com.roguera.creatures.Creature;

import static com.roguera.view.ViewObjects.mapView;

public class Animation {

	private final int ANIMATION_SPEED_MILLISECONDS = 200;

	private final String[] deadAnimationSequence = {"|", ";", "‧", "."};

	public void deadAnimation(Creature creature) {
		for (String frame : deadAnimationSequence) {
			creature.model.changeModel(frame.charAt(0));
			Draw.call(mapView);
			try {
				TimeUnit.MILLISECONDS.sleep(ANIMATION_SPEED_MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
