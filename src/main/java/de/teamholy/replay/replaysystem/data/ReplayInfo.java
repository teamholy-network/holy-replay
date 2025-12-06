package de.teamholy.replay.replaysystem.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReplayInfo {
	
	private String id;

	private Long time;
	
	private int duration;

}
