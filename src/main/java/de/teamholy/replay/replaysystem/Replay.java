package de.teamholy.replay.replaysystem;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.replaysystem.data.ReplayData;
import de.teamholy.replay.replaysystem.data.ReplayInfo;
import de.teamholy.replay.replaysystem.recording.Recorder;
import de.teamholy.replay.replaysystem.replaying.Replayer;
import de.teamholy.replay.utils.StringUtils;

@Getter @Setter
public class Replay {

	public static HashMap<String, Replay> ACTIVE_REPLAYS = new HashMap<>();

	private String id;
	
	private ReplayData data;
	
	private ReplayInfo replayInfo;
	
	private Recorder recorder;
	private Replayer replayer;
	
	private boolean isRecording, isPlaying;
	
	public Replay() {
		this.id = StringUtils.getRandomString(6);
		this.data = new ReplayData();
		this.isRecording = false;
		this.isPlaying = false;
	}
	
	public Replay(String id, ReplayData data) {
		this.id = id;
		this.data = data;
	}
	
	public void record(CommandSender sender, Player... players) {
		recordAll(Arrays.asList(players));
	}
	
	public void recordAll(List<Player> players) {
		this.recorder = new Recorder(this, players);
		this.recorder.start();
		this.isRecording = true;

		Replay.ACTIVE_REPLAYS.put(this.id, this);
	}

	/**
	 * Startet Aufnahme für bestimmte Welten
	 * Alle Spieler in diesen Welten werden aufgenommen
	 *
	 * @param worlds Welten, die aufgenommen werden sollen
	 */
	public void recordWorlds(List<World> worlds) {
		// Filtere Spieler die in den angegebenen Welten sind
		List<Player> playersInWorlds = new ArrayList<>();
		for (World world : worlds) {
			playersInWorlds.addAll(world.getPlayers());
		}

		// Speichere Weltnamen in den Replay-Daten
		List<String> worldNames = worlds.stream()
				.map(World::getName)
				.collect(Collectors.toList());
		this.data.setWorlds(worldNames);

		// Starte normale Aufnahme mit den gefundenen Spielern
		recordAll(playersInWorlds);
	}

	public void play(Player watcher) {
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(ReplaySystem.getInstance(), () -> startReplay(watcher));
		} else {
			startReplay(watcher);
		}
	}
		
	private void startReplay(Player watcher) {
		this.replayer = new Replayer(this, watcher);
		this.isPlaying = this.replayer.start();
	}
}
