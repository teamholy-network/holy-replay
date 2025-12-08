package de.teamholy.replay.listener;


import java.util.Arrays;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.filesystem.ItemConfig;
import de.teamholy.replay.filesystem.ItemConfigOption;
import de.teamholy.replay.filesystem.ItemConfigType;
import de.teamholy.replay.legacy.LegacyUtils;
import de.teamholy.replay.replayserver.ReplayHelper;
import de.teamholy.replay.replayserver.ReplayPacketListener;
import de.teamholy.replay.replayserver.Replayer;
import de.teamholy.replay.replaysystem.utils.entities.INPC;
import de.teamholy.replay.utils.VersionUtil;
import de.teamholy.replay.utils.version.MaterialBridge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;


@SuppressWarnings("all")
public class ReplayListener extends AbstractListener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            var player = event.getPlayer();
            if (ReplayHelper.replaySessions.containsKey(player.getName())) {
                event.setCancelled(true);

                Replayer replayer = ReplayHelper.replaySessions.get(player.getName());
                if (player.getItemInHand() == null) {
                    return;
                }
                if (player.getItemInHand().getItemMeta() == null) {
                    return;
                }

                ItemMeta meta = player.getItemInHand().getItemMeta();
                ItemConfigType itemType = ItemConfig.getByIdAndName(player.getItemInHand().getType(),
                        meta.getDisplayName().replaceAll("§", "&"));

                if (itemType == ItemConfigType.PAUSE) {
                    replayer.setPaused(!replayer.isPaused());
                    ReplayHelper.sendTitle(player, " ", "§c❙❙", 20);
                }

                if (itemType == ItemConfigType.FORWARD) {
                    replayer.getUtils().forward();
                    ReplayHelper.sendTitle(player, " ", "§a»»", 20);

                }
                if (itemType == ItemConfigType.BACKWARD) {
                    replayer.getUtils().backward();
                    ReplayHelper.sendTitle(player, " ", "§c««", 20);

                }

                if (itemType == ItemConfigType.RESUME) {
                    replayer.setPaused(!replayer.isPaused());
                    ReplayHelper.sendTitle(player, " ", "§a➤", 20);

                }

                if (itemType == ItemConfigType.SPEED) {
                    if (player.isSneaking()) {
                        if (replayer.getSpeed() < 1) {
                            replayer.setSpeed(1);
                        } else if (replayer.getSpeed() == 1) {
                            replayer.setSpeed(2);
                        }

                    } else {
                        if (replayer.getSpeed() == 2) {
                            replayer.setSpeed(1);
                        } else if (replayer.getSpeed() == 1) {
                            replayer.setSpeed(0.5D);
                        } else if (replayer.getSpeed() == 0.5D) {
                            replayer.setSpeed(0.25D);
                        }
                    }


                }

                if (itemType == ItemConfigType.LEAVE) {
                    replayer.stop();
                }

                if (itemType == ItemConfigType.TELEPORT) {
                    ReplayHelper.createTeleporter(player, replayer, 1);
                }

                ItemConfigOption pauseResume = ItemConfig.getItem(ItemConfigType.RESUME);

                if (itemType == ItemConfigType.PAUSE || itemType == ItemConfigType.RESUME) {
                    if (replayer.isPaused()) {
                        player.getInventory().setItem(pauseResume.getSlot(), ReplayHelper.getResumeItem());
                    } else {
                        player.getInventory().setItem(pauseResume.getSlot(), ReplayHelper.getPauseItem());
                    }
                }


            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (ReplayHelper.replaySessions.containsKey(player.getName())) {
                event.setCancelled(true);

                // Avoid IncompatibleClassChangeError < 1.21
                String title = VersionUtil.isAbove(VersionUtil.VersionEnum.V1_21) ? event.getView().getTitle()
                        : LegacyUtils.getInventoryTitle(event);

                if (title.equalsIgnoreCase("§7Teleporter")) {
                    Replayer replayer = ReplayHelper.replaySessions.get(player.getName());

                    if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
                        event.getCurrentItem().getItemMeta().getDisplayName();
                        if (event.getCurrentItem().getType() == MaterialBridge.PLAYER_HEAD.toMaterial()) {
                            String owner = event.getCurrentItem().getItemMeta().getDisplayName()
                                    .replaceAll("§6", "");
                            if (replayer.getNPCList().containsKey(owner)) {
                                INPC npc = replayer.getNPCList().get(owner);
                                player.teleport(npc.getLocation());
                            }
                        } else if (event.getCurrentItem().getType() == Material.ARROW) {
                            if (event.getSlot() == event.getInventory().getSize() - 1) {
                                int nextPage = event.getCurrentItem().getAmount();
                                ReplayHelper.createTeleporter(player, replayer, nextPage);
                            } else if (event.getSlot() == event.getInventory().getSize() - 9) {
                                int previousPage = event.getCurrentItem().getAmount();
                                ReplayHelper.createTeleporter(player, replayer, previousPage);
                            }
                        }


                    }
                }
            }
        }
    }


    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        var player = (Player) event.getEntity();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            event.setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (ReplayHelper.replaySessions.containsKey(player.getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        var player = event.getPlayer();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            Replayer replayer = ReplayHelper.replaySessions.get(player.getName());

            for (INPC npc : replayer.getNPCList().values()) {
                npc.despawn();
                npc.respawn(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            Replayer replayer = ReplayHelper.replaySessions.get(player.getName());
            replayer.stop();
            replayer.getSession().resetPlayer();
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            event.setCancelled(true);
        }

        boolean isReplayItem = ReplayHelper.replaySessions.values()
                .stream()
                .anyMatch(replayer -> replayer.getUtils().getEntities().containsValue(event.getItem()));

        if (isReplayItem) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            var oldChunk = event.getFrom().getChunk();
            var newChunk = event.getTo().getChunk();

            if (oldChunk.getWorld() != newChunk.getWorld() || oldChunk.getX() != newChunk.getX()
                    || oldChunk.getZ() != newChunk.getZ()) {
                Replayer replayer = ReplayHelper.replaySessions.get(player.getName());

                for (INPC npc : replayer.getNPCList().values()) {

                    if (ReplayHelper.isInRange(npc.getLocation(), player.getLocation())) {
                        if (!Arrays.asList(npc.getVisible()).contains(player)) {
                            npc.respawn(player);
                        }
                    } else {
                        if (Arrays.asList(npc.getVisible()).contains(player)) {
                            npc.despawn();
                        }
                    }
                }
            }

        }

    }


    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            final Replayer replayer = ReplayHelper.replaySessions.get(player.getName());

            new BukkitRunnable() {

                @Override
                public void run() {
                    for (INPC npc : replayer.getNPCList().values()) {

                        npc.despawn();

                        if (ReplayHelper.isInRange(player.getLocation(), npc.getLocation())) {
                            npc.respawn(player);
                        }
                    }

                }
            }.runTaskLater(ReplaySystem.getInstance(), 20);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        var player = e.getPlayer();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            ReplayPacketListener packetListener = ReplayHelper.replaySessions.get(player.getName())
                    .getSession().getPacketListener();

            if (packetListener.getPrevious() != -1) {
                packetListener.setCamera(player, player.getEntityId(), packetListener.getPrevious());

                player.setAllowFlight(true);
            }
        }


    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        var player = e.getEntity();
        if (ReplayHelper.replaySessions.containsKey(player.getName())) {
            e.setKeepLevel(true);
            e.setKeepInventory(true);
        }
    }
}
