package de.teamholy.replay.utils;

import java.util.List;

import java.util.Random;


public class MathUtils {

	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public static double round(double number, double amount){				
		return Math.round(number * amount)/amount;
		
	}
	  
}
