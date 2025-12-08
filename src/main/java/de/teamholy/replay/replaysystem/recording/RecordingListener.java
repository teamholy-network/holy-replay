package de.teamholy.replay.replaysystem.recording;

import com.comphenix.protocol.wrappers.EnumWrappers;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.filesystem.Messages;
import de.teamholy.replay.listener.AbstractListener;
import de.teamholy.replay.replaysystem.data.ActionData;
import de.teamholy.replay.replaysystem.data.ActionType;
import de.teamholy.replay.replaysystem.data.types.*;
import de.teamholy.replay.replaysystem.utils.ItemUtils;
import de.teamholy.replay.replaysystem.utils.NPCManager;
import de.teamholy.replay.utils.version.MaterialBridge;
import de.teamholy.replay.utils.VersionUtil;
import de.teamholy.replay.utils.VersionUtil.VersionEnum;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecordingListener extends AbstractListener {
	
	
	private PacketRecorder packetRecorder;
	private Recorder recorder;
	
	private List<String> replayLeft;
	
	public RecordingListener(PacketRecorder packetRecorder) {
		this.packetRecorder = packetRecorder;
		
		this.recorder = this.packetRecorder.getRecorder();
		this.replayLeft = new ArrayList<String>();
	}
	
	/**
	 * Prüft ob ein Spieler in einer Welt ist, die aufgenommen wird
	 * @param player Spieler
	 * @return true wenn die Welt des Spielers aufgenommen wird
	 */
	private boolean isPlayerInRecordedWorld(Player player) {
		List<String> recordedWorlds = recorder.getData().getWorlds();
		// Wenn keine Welten-Filter gesetzt sind, werden alle Welten aufgenommen
		if (recordedWorlds.isEmpty()) {
			return true;
		}
		return recordedWorlds.contains(player.getWorld().getName());
	}

	/**
	 * Prüft ob ein Block in einer Welt ist, die aufgenommen wird
	 * @param block Block
	 * @return true wenn die Welt des Blocks aufgenommen wird
	 */
	private boolean isBlockInRecordedWorld(Block block) {
		List<String> recordedWorlds = recorder.getData().getWorlds();
		if (recordedWorlds.isEmpty()) {
			return true;
		}
		return recordedWorlds.contains(block.getWorld().getName());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if (recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
				this.packetRecorder.addData(p.getName(), NPCManager.copyFromPlayer(p, true, true));
			}
			
		}
		
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onHeld(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		if (recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
			ItemStack stack = p.getInventory().getItem(e.getNewSlot());
			itemInHand(p, stack);
		}
		
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				
				boolean isInteractable = e.getClickedBlock() != null && ItemUtils.isInteractable(e.getClickedBlock().getType());
				if (e.getItem() != null && ItemUtils.isUsable(e.getItem().getType()) && (!isInteractable || p.isSneaking())) {
					if (!this.recorder.getData().getWatcher(p.getName()).isBlocking()) {
						PlayerWatcher watcher = this.recorder.getData().getWatcher(p.getName());
						watcher.setBlocking(true);
						this.packetRecorder.addData(p.getName(), MetadataUpdate.fromWatcher(watcher));
						
					}
				}
				
				
				if (NPCManager.isArmor(e.getItem()) && !NPCManager.wearsArmor(p, NPCManager.getArmorType(e.getItem()))) {
					InvData data = NPCManager.copyFromPlayer(p, true, true);
					String armorType = NPCManager.getArmorType(e.getItem());
					if (armorType != null) {
						if (armorType.equals("head")) data.setHead(NPCManager.fromItemStack(e.getItem()));
						if (armorType.equals("chest")) data.setChest(NPCManager.fromItemStack(e.getItem()));
						if (armorType.equals("leg")) data.setLeg(NPCManager.fromItemStack(e.getItem()));
						if (armorType.equals("boots")) data.setBoots(NPCManager.fromItemStack(e.getItem()));
					}
					if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
						data.setMainHand(null);
					}
					
					this.packetRecorder.addData(p.getName(), data);
					
				}
			}
			
			if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.getTargetBlock((Set<Material>) null, 5).getType() == Material.FIRE) {
				LocationData location = LocationData.fromLocation(p.getTargetBlock((Set<Material>) null, 5).getLocation());
				
				ItemData before = ItemData.fromMaterial(Material.FIRE);
				ItemData after = ItemData.air();
				
				this.packetRecorder.addData(p.getName(), new BlockChangeData(location, before, after));
			}
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onConsume(PlayerItemConsumeEvent e) {
		Player p = e.getPlayer();
		if (recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
			if (recorder.getData().getWatcher(p.getName()).isBlocking()) {
				PlayerWatcher watcher = this.recorder.getData().getWatcher(p.getName());
				watcher.setBlocking(false);
				this.packetRecorder.addData(p.getName(), MetadataUpdate.fromWatcher(watcher));
				
				InvData data = NPCManager.copyFromPlayer(p, true, true);
				if (p.getItemInHand() != null && p.getItemInHand().getAmount() <= 1) {
					data.setMainHand(null);
				}
				
				this.packetRecorder.addData(p.getName(), data);
				
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {

				this.packetRecorder.addData(p.getName(), new AnimationData(1));
				
				if (p.getFireTicks() > 20 && !this.recorder.getData().getWatcher(p.getName()).isBurning()) {
					this.recorder.getData().getWatcher(p.getName()).setBurning(true);
					this.packetRecorder.addData(p.getName(), new MetadataUpdate(true, this.recorder.getData().getWatcher(p.getName()).isBlocking(), this.recorder.getData().getWatcher(p.getName()).isElytra()));
				} else if (p.getFireTicks() <= 20 && this.recorder.getData().getWatcher(p.getName()).isBurning()) {
					this.recorder.getData().getWatcher(p.getName()).setBurning(false);
					this.packetRecorder.addData(p.getName(), new MetadataUpdate(false, this.recorder.getData().getWatcher(p.getName()).isBlocking(), this.recorder.getData().getWatcher(p.getName()).isElytra()));
				}
			}
		} else if (e.getEntity() instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) e.getEntity();
			if (this.packetRecorder.getEntityLookup().containsKey(living.getEntityId())) {
				
				this.packetRecorder.addData(this.packetRecorder.getEntityLookup().get(living.getEntityId()), new EntityAnimationData(living.getEntityId(), (living.getHealth() - e.getFinalDamage()) > 0 ? 2 : 3));
			}
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCrit(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player damager = (Player) e.getDamager();
			Player victim = (Player) e.getEntity();
			if (recorder.getPlayers().contains(damager.getName()) && recorder.getPlayers().contains(victim.getName())
				&& isPlayerInRecordedWorld(damager) && isPlayerInRecordedWorld(victim)) {
				if (damager.getFallDistance() > 0.0F && !damager.isOnGround() && damager.getVehicle() == null) {
					
					this.packetRecorder.addData(victim.getName(), new AnimationData(4));
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBed(PlayerBedEnterEvent e) {
		Player p = e.getPlayer();
		if (recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {

			this.packetRecorder.addData(p.getName(), new BedEnterData(LocationData.fromLocation(e.getBed().getLocation())));
		}
		
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBedLeave(PlayerBedLeaveEvent e) {
		Player p = e.getPlayer();
		if (recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {

			this.packetRecorder.addData(p.getName(), new AnimationData(2));
		}
		
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (this.recorder.getPlayers().contains(p.getName())) {
			this.recorder.addData(this.recorder.getCurrentTick(), new ActionData(0, ActionType.DESPAWN, p.getName(), null));
			this.recorder.getPlayers().remove(p.getName());
			
			if (!this.replayLeft.contains(p.getName())) this.replayLeft.add(p.getName());
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		// Prüfe ob Spieler in einer aufgenommenen Welt joined
		if (!isPlayerInRecordedWorld(p)) {
			return;
		}

		if (!this.recorder.getPlayers().contains(p.getName()) && (this.replayLeft.contains(p.getName())) || ConfigManager.ADD_PLAYERS) {
			this.recorder.getPlayers().add(p.getName());
			this.recorder.getData().getWatchers().put(p.getName(), new PlayerWatcher(p.getName()));
			this.recorder.createSpawnAction(p, p.getLocation(), false);
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
			this.recorder.addData(this.recorder.getCurrentTick(), new ActionData(0, ActionType.DEATH, p.getName(), null));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		// Prüfe sowohl aktuelle Welt als auch Respawn-Welt
		List<String> recordedWorlds = this.recorder.getData().getWorlds();
		boolean respawnWorldRecorded = recordedWorlds.isEmpty() || recordedWorlds.contains(e.getRespawnLocation().getWorld().getName());

		if (this.recorder.getPlayers().contains(p.getName())) {
			if (respawnWorldRecorded) {
				// Respawn in aufgenommener Welt - erstelle Spawn-Action
				this.recorder.createSpawnAction(p, e.getRespawnLocation(), false);
			} else {
				// Respawn in nicht-aufgenommener Welt - entferne Spieler aus Aufnahme
				this.recorder.addData(this.recorder.getCurrentTick(), new ActionData(0, ActionType.DESPAWN, p.getName(), null));
				this.recorder.getPlayers().remove(p.getName());
				if (!this.replayLeft.contains(p.getName())) {
					this.replayLeft.add(p.getName());
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onThrow(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
			InvData data = NPCManager.copyFromPlayer(p, true, true);
			// Check if item in hand is thrown
			if (data.getMainHand() != null && p.getItemInHand() != null && p.getItemInHand().getAmount() <= 1 && p.getItemInHand().getType() == data.getMainHand().toMaterial()) {
				data.setMainHand(null);
			}
			
			this.packetRecorder.addData(p.getName(), data);
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onLaunch(ProjectileLaunchEvent e) {
		Projectile proj = e.getEntity();
		if (proj.getShooter() instanceof Player) {
			Player p = (Player) proj.getShooter();
			if (this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
				LocationData spawn = LocationData.fromLocation(p.getEyeLocation());
				LocationData velocity = LocationData.fromLocation(proj.getVelocity().toLocation(p.getWorld()));
				
				this.packetRecorder.addData(p.getName(), new ProjectileData(spawn, velocity, proj.getType()));
				this.packetRecorder.addData(p.getName(), NPCManager.copyFromPlayer(p, true, true));
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPickup(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		if (this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
			// Change PlayerItemInHand
			
			if (p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
				itemInHand(p, e.getItem().getItemStack());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p) && isBlockInRecordedWorld(e.getBlockPlaced())) {
			LocationData location = LocationData.fromLocation(e.getBlockPlaced().getLocation());


            ItemData before = VersionUtil.isAbove(VersionEnum.V1_13) ? ItemData.fromMaterial(e.getBlockReplacedState().getType()) : new ItemData(e.getBlockReplacedState().getType().getId(), e.getBlockReplacedState().getData().getData());
			ItemData after = VersionUtil.isAbove(VersionEnum.V1_13) ? ItemData.fromMaterial(e.getBlockPlaced().getBlockData().getMaterial()) : new ItemData(e.getBlockPlaced().getType().getId(), e.getBlockPlaced().getData());

			this.packetRecorder.addData(p.getName(), new BlockChangeData(location, before, after));
			
			// Change PlayerItemInHand when last block in hand
			if (e.getItemInHand().getAmount() == 1) {
				ItemStack stack = new ItemStack(Material.AIR, 1);
				itemInHand(p, stack);
			}
		}
		
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p) && isBlockInRecordedWorld(e.getBlock())) {
			recordBlockBreak(p, e.getBlock());
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onTNTExplode(EntityExplodeEvent e) {
		if (!(e.getEntity() instanceof TNTPrimed)) return;
		
		TNTPrimed tnt = (TNTPrimed) e.getEntity();
		if (!(tnt.getSource() instanceof Player)) return;
		
		Player p = (Player) tnt.getSource();
		if (!this.recorder.getPlayers().contains(p.getName()) || !isPlayerInRecordedWorld(p)) return;

		for (Block block : e.blockList()) {
			// Nur Blöcke in aufgenommenen Welten aufnehmen
			if (!isBlockInRecordedWorld(block)) continue;

			//Block change is done by the explosion packet.
			// We record the block data for reversed playing, real_changes=true and world reset
			recordBlockBreak(p, block, false, false);
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onTNTPrime(PlayerInteractEvent e) {
		//TNTPrimeEvent is bugged without paper, so we need to use PlayerInteractEvent instead
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getItem() == null || !(e.getItem().getType() == Material.FLINT_AND_STEEL || e.getItem().getType() == MaterialBridge.FIRE_CHARGE.toMaterial()))
			return;
		Block block = e.getClickedBlock();
		if (block == null) return;
		if (block.getType() != Material.TNT) return;
		Player p = e.getPlayer();
		if(p.isSneaking()) return;
		if (!this.recorder.getPlayers().contains(p.getName()) || !isPlayerInRecordedWorld(p) || !isBlockInRecordedWorld(block)) return;

		recordBlockBreak(e.getPlayer(), block, false, true);
	}
	
	@SuppressWarnings("deprecation")
	private void recordBlockBreak(Player p, Block block, boolean playEffect, boolean doBlockChange) {
		LocationData location = LocationData.fromLocation(block.getLocation());
		ItemData before = ItemData.fromBlock(block);
		ItemData after = ItemData.air();
		
		this.packetRecorder.addData(p.getName(), new BlockChangeData(location, before, after, playEffect, doBlockChange));
	}
	
	private void recordBlockBreak(Player p, Block block) {
		recordBlockBreak(p, block, true, true);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFill(PlayerBucketFillEvent e) {
		Player p = e.getPlayer();
		if (this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p) && isBlockInRecordedWorld(e.getBlockClicked())) {
			LocationData location = LocationData.fromLocation(e.getBlockClicked().getLocation());
			
			ItemData before = ItemData.fromBlock(e.getBlockClicked());
			ItemData after = ItemData.air();
			
			this.packetRecorder.addData(p.getName(), new BlockChangeData(location, before, after));
			
			// Change PlayerItemInHand when fill bucket
			ItemStack stack;

			if (e.getBlockClicked().getState().getType().equals(MaterialBridge.LAVA.toMaterial())) {
				stack = new ItemStack(Material.LAVA_BUCKET, 1);
			} else {
				stack = new ItemStack(Material.WATER_BUCKET, 1);
			}
			itemInHand(p, stack);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEmpty(PlayerBucketEmptyEvent e) {
		Player p = e.getPlayer();
		if (this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {
			Block block = e.getBlockClicked().getRelative(e.getBlockFace());
			if (!isBlockInRecordedWorld(block)) return;

			LocationData location = LocationData.fromLocation(block.getLocation());
			
			ItemData before = ItemData.fromBlock(block);
			ItemData after;

			if (VersionUtil.isAbove(VersionEnum.V1_13)) {
				Material material = e.getBucket() == Material.LAVA_BUCKET ? Material.LAVA : Material.WATER;
				after = ItemData.fromMaterial(material);
			} else {
                after = new ItemData(e.getBucket() == Material.LAVA_BUCKET ? 11 : 9, 0);
            }

			this.packetRecorder.addData(p.getName(), new BlockChangeData(location, before, after));
			
			// Change PlayerItemInHand
			ItemStack stack = new ItemStack(Material.BUCKET, 1);
			itemInHand(p, stack);
		}
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();
		if (!this.recorder.getPlayers().contains(p.getName())) {
			return;
		}

		World fromWorld = e.getFrom();
		World toWorld = p.getWorld();

		List<String> recordedWorlds = this.recorder.getData().getWorlds();
		boolean fromRecorded = recordedWorlds.isEmpty() || recordedWorlds.contains(fromWorld.getName());
		boolean toRecorded = recordedWorlds.isEmpty() || recordedWorlds.contains(toWorld.getName());

		// Spieler wechselt zwischen aufgenommenen Welten
		if (fromRecorded && toRecorded) {
			LocationData location = LocationData.fromLocation(p.getLocation());
			this.packetRecorder.addData(p.getName(), new WorldChangeData(location));

			// Füge Ziel-Welt zu Liste hinzu (falls nicht vorhanden)
			if (!recordedWorlds.isEmpty() && !recordedWorlds.contains(toWorld.getName())) {
				this.recorder.getData().getWorlds().add(toWorld.getName());
			}
		}
		// Spieler verlässt aufgenommene Welt → Despawn
		else if (fromRecorded && !toRecorded) {
			this.recorder.addData(this.recorder.getCurrentTick(), new ActionData(0, ActionType.DESPAWN, p.getName(), null));
			this.recorder.getPlayers().remove(p.getName());
			if (!this.replayLeft.contains(p.getName())) {
				this.replayLeft.add(p.getName());
			}
		}
		// Spieler betritt aufgenommene Welt → Spawn
		else if (!fromRecorded && toRecorded) {
			if (!this.recorder.getPlayers().contains(p.getName())) {
				this.recorder.getPlayers().add(p.getName());
				this.recorder.getData().getWatchers().put(p.getName(), new PlayerWatcher(p.getName()));
				this.recorder.createSpawnAction(p, p.getLocation(), false);
			}
		}
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent e) {
		Player p = e.getPlayer();
		if (VersionUtil.isAbove(VersionEnum.V1_21) && this.recorder.getPlayers().contains(p.getName()) && isPlayerInRecordedWorld(p)) {

			EnumWrappers.PlayerAction action = e.isSneaking() ? EnumWrappers.PlayerAction.START_SNEAKING : EnumWrappers.PlayerAction.STOP_SNEAKING;
			this.packetRecorder.addData(p.getName(), new EntityActionData(action));
		}
	}
	
	public void itemInHand(Player p, ItemStack stack) {
		InvData data = NPCManager.copyFromPlayer(p, true, true);
		data.setMainHand(NPCManager.fromItemStack(stack));
		
		this.packetRecorder.addData(p.getName(), data);
		
		if (recorder.getData().getWatcher(p.getName()).isBlocking()) {
			recorder.getData().getWatcher(p.getName()).setBlocking(false);
			this.packetRecorder.addData(p.getName(), new MetadataUpdate(recorder.getData().getWatcher(p.getName()).isBurning(), false, this.recorder.getData().getWatcher(p.getName()).isElytra()));
		}
	}
}
