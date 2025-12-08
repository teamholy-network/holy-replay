package de.teamholy.replay.replayserver.session;

import java.util.Arrays;
import java.util.List;

import de.teamholy.replay.replayserver.ReplayHelper;
import de.teamholy.replay.replayserver.ReplayPacketListener;
import de.teamholy.replay.replayserver.Replayer;
import org.bukkit.Bukkit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.api.events.ReplaySessionFinishEvent;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.filesystem.ItemConfig;
import de.teamholy.replay.filesystem.ItemConfigOption;
import de.teamholy.replay.filesystem.ItemConfigType;

public class ReplaySession {

	private final Replayer replayer;
	
	private final Player player;
	
	private ItemStack content[];
	
	private int level;
	
	private float xp;
	
	private Location start;
	
	private final ReplayPacketListener packetListener;

	public ReplaySession(Replayer replayer) {
		this.replayer = replayer;
		
		this.player = this.replayer.getWatchingPlayer();
		
		this.packetListener = new ReplayPacketListener(replayer);
	}
	
	public void startSession() {
		this.packetListener.register();

		this.content = this.player.getInventory().getContents();
		if (this.start == null) {
			this.start = this.player.getLocation();
		}
		this.level = this.player.getLevel();
		this.xp = this.player.getExp();

		this.player.setHealth(this.player.getMaxHealth());
		this.player.setFoodLevel(20);
		this.player.getInventory().clear();
		
		ItemConfigOption teleport = ItemConfig.getItem(ItemConfigType.TELEPORT);
		ItemConfigOption time = ItemConfig.getItem(ItemConfigType.SPEED);
		ItemConfigOption leave = ItemConfig.getItem(ItemConfigType.LEAVE);
		ItemConfigOption backward = ItemConfig.getItem(ItemConfigType.BACKWARD);
		ItemConfigOption forward = ItemConfig.getItem(ItemConfigType.FORWARD);
		ItemConfigOption pauseResume = ItemConfig.getItem(ItemConfigType.PAUSE);

		List<ItemConfigOption> configItems = Arrays.asList(teleport, time, leave, backward, forward, pauseResume);

		configItems.stream()
			.filter(ItemConfigOption::isEnabled)
			.forEach(item -> {
				this.player.getInventory().setItem(item.getSlot(), ReplayHelper.createItem(item));
			});
		

		this.player.setAllowFlight(true);
		this.player.setFlying(true);
	}
	
	public void stopSession() {
        ReplayHelper.replaySessions.remove(this.player.getName());
		
		this.packetListener.unregister();


		new BukkitRunnable() {
			
			@Override
			public void run() {
				resetPlayer();
				
				player.teleport(start);

				ReplaySessionFinishEvent finishEvent = new ReplaySessionFinishEvent(replayer.getReplay(), player);
				Bukkit.getPluginManager().callEvent(finishEvent);
			}
		}.runTask(ReplaySystem.getInstance());
		

	}
	
	public void resetPlayer() {
		player.getInventory().clear();
		player.getInventory().setContents(content);
		
		if (player.getGameMode() != GameMode.CREATIVE) {
			player.setFlying(false);
			player.setAllowFlight(false);
		}

		if (ConfigManager.PROGRESS_TYPE == ReplayProgressType.XP_BAR) {
			player.setLevel(level);
			player.setExp(xp);
		}
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public ReplayPacketListener getPacketListener() {
		return packetListener;
	}
}
