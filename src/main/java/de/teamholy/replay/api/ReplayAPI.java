package de.teamholy.replay.api;

import java.util.List;

import de.teamholy.replay.replaysystem.recording.StaticModeManager;
import de.teamholy.replay.replaysystem.replaying.Replayer;
import org.bukkit.entity.Player;

import de.teamholy.replay.filesystem.saving.IReplaySaver;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.replaying.ReplayHelper;
import de.teamholy.replay.utils.ReplayManager;
import de.teamholy.replay.utils.fetcher.Consumer;

public class ReplayAPI {

	private static ReplayAPI instance;
	
	private HookManager hookManager;
	
	private ReplayAPI() {
		this.hookManager = new HookManager();
	}
	
	public void registerHook(IReplayHook hook) {
		this.hookManager.registerHook(hook);
	}
	
	public void unregisterHook(IReplayHook hook) {
		this.hookManager.unregisterHook(hook);
	}
	
	public Replay recordReplay(String name, List<Player> players) {
		Replay replay = new Replay();
		if (name != null) replay.setId(name);
		replay.recordAll(players);
		
		return replay;
	}

	public Replay recordReplay(String name, Player... players) {
		return recordReplay(name, players);
	}
	
	public void stopReplay(String name, boolean save) {
		stopReplay(name, save, false);
	}
	
	public void stopReplay(String name, boolean save, boolean ignoreEmpty) {
		if (Replay.ACTIVE_REPLAYS.containsKey(name)) {
			Replay replay = Replay.ACTIVE_REPLAYS.get(name);
			
			boolean shouldSave = save && (replay.getRecorder().getData().getActions().size() > 0 || !ignoreEmpty);
			if (replay.isRecording()) replay.getRecorder().stop(shouldSave);
		}
	}
	
	public void playReplay(String name, Player watcher) {
		if (ReplaySaver.exists(name) && !ReplayHelper.replaySessions.containsKey(watcher.getName())) {
			ReplaySaver.load(name, new Consumer<Replay>() {
				
				@Override
				public void accept(Replay replay) {
					replay.play(watcher);
					
				}
			});
		}
	}

	public void jumpToReplayTime(Player watcher, Integer second) {
		if (ReplayHelper.replaySessions.containsKey(watcher.getName())) {
			Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
			if (replayer != null) {
				int duration = replayer.getReplay().getData().getDuration() / 20;
				if (second > 0 && second <= duration) {
					replayer.getUtils().jumpTo(second);
				}
			}
		}
	}
	
	public void registerReplaySaver(IReplaySaver replaySaver) {
		ReplaySaver.register(replaySaver);
	}

	public IReplaySaver getReplaySaver() {
		return ReplaySaver.getReplaySaver();
	}
	
	public HookManager getHookManager() {
		return hookManager;
	}
	
	// ========== Static Mode Methods ==========

	/**
	 * Saves the last configured minutes from static mode continuous recording
	 * @param customId Custom ID for the replay, or null for auto-generated ID
	 * @return The ID of the saved replay, or null if static mode is not running
	 */
	public String saveStaticReplay(String customId) {
		return StaticModeManager.getInstance().saveLastMinutes(customId);
	}

	/**
	 * Checks if static mode is currently recording
	 * @return true if static mode is active and recording
	 */
	public boolean isStaticModeRecording() {
		return StaticModeManager.getInstance().isRecording();
	}

	/**
	 * Gets the current duration of static mode recording in seconds
	 * @return Duration in seconds, or 0 if not recording
	 */
	public int getStaticModeDuration() {
		return StaticModeManager.getInstance().getCurrentDuration();
	}


	public static ReplayAPI getInstance() {
		if (instance == null) instance = new ReplayAPI();
		
		return instance;
	}
}
