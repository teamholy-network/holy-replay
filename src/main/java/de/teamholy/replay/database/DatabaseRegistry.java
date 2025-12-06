package de.teamholy.replay.database;

import de.teamholy.replay.database.utils.Database;

public class DatabaseRegistry {

	public static Database database;
	
	public static void registerDatabase(Database d) {
		database = d;
	}
	
	public static boolean isRegistered() {
		return database != null;
	}
	
	public static Database getDatabase() {
		return database;
	}
}
