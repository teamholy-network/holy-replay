package de.teamholy.replay.utils;

import de.teamholy.replay.ReplaySystem;

public class LogUtils {

	public static void log(String message){
		ReplaySystem.getInstance().getLogger().info(message);
	}

}
