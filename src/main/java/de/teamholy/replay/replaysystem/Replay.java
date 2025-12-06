package de.teamholy.replay.replaysystem;

import java.util.Arrays;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.replaysystem.data.ReplayData;
import de.teamholy.replay.replaysystem.data.ReplayInfo;
import de.teamholy.replay.replaysystem.recording.Recorder;
import de.teamholy.replay.replaysystem.replaying.Replayer;
import de.teamholy.replay.utils.ReplayManager;
import de.teamholy.replay.utils.StringUtils;

@Getter @Setter
public class Replay {

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
		
		ReplayManager.activeReplays.put(this.id, this);
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
