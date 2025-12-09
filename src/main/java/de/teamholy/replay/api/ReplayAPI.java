package de.teamholy.replay.api;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.filesystem.saving.IReplaySaver;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.data.ActionData;
import de.teamholy.replay.replaysystem.data.ActionType;
import de.teamholy.replay.replaysystem.data.ReplayInfo;
import de.teamholy.replay.replaysystem.data.types.BlockChangeData;
import de.teamholy.replay.replaysystem.data.types.ChatData;
import de.teamholy.replay.replaysystem.data.types.ItemData;
import de.teamholy.replay.replaysystem.data.types.LocationData;
import de.teamholy.replay.replaysystem.recording.StaticModeManager;
import de.teamholy.replay.replayserver.ReplayHelper;
import de.teamholy.replay.replayserver.Replayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Hauptklasse für das ReplaySystem API.
 * Bietet alle Funktionen für Aufnahme, Wiedergabe und Verwaltung von Replays.
 *
 * @author TeamHoly
 */
public class ReplayAPI implements IReplayAPI {

    private static ReplayAPI instance;
    private final HookManager hookManager;
    private final IReplaySaver replaySaver;

    private ReplayAPI() {
        this.hookManager = new HookManager();
        this.replaySaver = ReplaySystem.getInstance().getReplaySaver();
    }

    // ========== Recording ==========

    @Override
    public Replay startRecording(String replayId, Player... players) {
        return startRecording(replayId, Arrays.asList(players));
    }

    @Override
    public Replay startRecording(String replayId, List<Player> players) {
        Replay replay = new Replay();
        if (replayId != null) {
            replay.setId(replayId);
        }
        replay.recordAll(players);
        return replay;
    }

    @Override
    public Replay startRecordingWorlds(String replayId, World... worlds) {
        return startRecordingWorlds(replayId, Arrays.asList(worlds));
    }

    @Override
    public Replay startRecordingWorlds(String replayId, List<World> worlds) {
        Replay replay = new Replay();
        if (replayId != null) {
            replay.setId(replayId);
        }
        replay.recordWorlds(worlds);
        return replay;
    }

    @Override
    public Replay startRecordingWorlds(String replayId, String... worldNames) {
        List<World> worlds = Arrays.stream(worldNames)
                .map(Bukkit::getWorld)
                .filter(w -> w != null)
                .collect(Collectors.toList());
        return startRecordingWorlds(replayId, worlds);
    }

    @Override
    public void stopRecording(String replayId, boolean save) {
        stopRecording(replayId, save, false);
    }

    @Override
    public void stopRecording(String replayId, boolean save, boolean ignoreEmpty) {
        if (Replay.ACTIVE_REPLAYS.containsKey(replayId)) {
            Replay replay = Replay.ACTIVE_REPLAYS.get(replayId);
            boolean shouldSave = save && (!replay.getRecorder().getData().getActions().isEmpty() || !ignoreEmpty);

            if (replay.isRecording()) {
                replay.getRecorder().stop(shouldSave);
            }
        }
    }

    @Override
    public void addDataToRecording(String replayId, de.teamholy.replay.replaysystem.data.ActionData actionData) {
        if (Replay.ACTIVE_REPLAYS.containsKey(replayId)) {
            Replay replay = Replay.ACTIVE_REPLAYS.get(replayId);
            if (replay.isRecording()) {
                replay.getRecorder().addData(replay.getRecorder().getCurrentTick(), actionData);
            }
        }
    }

    @Override
    public void addDataToAllRecordings(de.teamholy.replay.replaysystem.data.ActionData actionData) {
        for (Replay replay : Replay.ACTIVE_REPLAYS.values()) {
            if (replay.isRecording()) {
                replay.getRecorder().addData(replay.getRecorder().getCurrentTick(), actionData);
            }
        }
    }

    @Override
    public void addMessageToAllRecordings(String message) {
        for (Replay replay : Replay.ACTIVE_REPLAYS.values()) {
            if (replay.isRecording()) {
                ChatData chatData = new ChatData(message);
                ActionData actionData = new ActionData(
                        replay.getRecorder().getCurrentTick(),
                        ActionType.MESSAGE,
                        "SYSTEM",
                        chatData
                );
                replay.getRecorder().addData(replay.getRecorder().getCurrentTick(), actionData);
            }
        }
    }

    @Override
    public void addMessageToRecording(String replayId, String message) {
        if (Replay.ACTIVE_REPLAYS.containsKey(replayId)) {
            Replay replay = Replay.ACTIVE_REPLAYS.get(replayId);
            if (replay.isRecording()) {
                ChatData chatData = new ChatData(message);
                ActionData actionData = new ActionData(
                        replay.getRecorder().getCurrentTick(),
                        ActionType.MESSAGE,
                        "SYSTEM",
                        chatData
                );
                replay.getRecorder().addData(replay.getRecorder().getCurrentTick(), actionData);
            }
        }
    }

    @Override
    public void addBlockChangeToRecording(String replayId, Block oldBlock, Material newBlock) {
        if (replayId == null) return;
        if (Replay.ACTIVE_REPLAYS.containsKey(replayId)) {
            Replay replay = Replay.ACTIVE_REPLAYS.get(replayId);
            if (replay.isRecording()) {
                LocationData location = LocationData.fromLocation(oldBlock.getLocation());
                ItemData before = ItemData.fromBlock(oldBlock);
                ItemData after = ItemData.fromMaterial(newBlock);

                BlockChangeData blockChangeData = new BlockChangeData(
                        location, before, after, true, true
                );

                ActionData actionData = new ActionData(
                        replay.getRecorder().getCurrentTick(),
                        ActionType.CUSTOM,
                        "SYSTEM",
                        blockChangeData
                );

                replay.getRecorder().addData(replay.getRecorder().getCurrentTick(), actionData);
            }
        }
    }

    @Override
    public void addBlockChangeToAllRecordings(Block oldBlock, Material newBlock) {
        for (Replay replay : Replay.ACTIVE_REPLAYS.values()) {
            if (replay.isRecording()) {
                LocationData location = LocationData.fromLocation(oldBlock.getLocation());
                ItemData before = ItemData.fromBlock(oldBlock);
                ItemData after = ItemData.fromMaterial(newBlock);


                BlockChangeData blockChangeData = new BlockChangeData(
                        location, before, after, true, true
                );

                ActionData actionData = new ActionData(
                        replay.getRecorder().getCurrentTick(),
                        ActionType.CUSTOM,
                        "SYSTEM",
                        blockChangeData
                );

                replay.getRecorder().addData(replay.getRecorder().getCurrentTick(), actionData);
            }
        }
    }

    // ========== Playback ==========

    @Override
    public CompletableFuture<Optional<Replay>> playReplay(String replayId, Player watcher) {
        CompletableFuture<Optional<Replay>> future = new CompletableFuture<>();

        if (!ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            ReplaySystem.getInstance().getReplaySaver().loadReplay(replayId, replay -> {
                if (replay != null) {
                    replay.play(watcher);
                    future.complete(Optional.of(replay));
                } else {
                    future.complete(Optional.empty());
                }
            });
        } else {
            future.complete(Optional.empty());
        }

        return future;
    }

    @Override
    public boolean stopPlayback(Player watcher) {
        if (ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
            if (replayer != null) {
                replayer.stop();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pausePlayback(Player watcher) {
        if (ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
            if (replayer != null && !replayer.isPaused()) {
                replayer.setPaused(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean resumePlayback(Player watcher) {
        if (ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
            if (replayer != null && replayer.isPaused()) {
                replayer.setPaused(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean jumpToTime(Player watcher, int seconds) {
        if (ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
            if (replayer != null) {
                int duration = replayer.getReplay().getData().getDuration() / 20;
                if (seconds > 0 && seconds <= duration) {
                    replayer.getUtils().jumpTo(seconds);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean setPlaybackSpeed(Player watcher, double speed) {
        if (ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
            if (replayer != null && speed > 0) {
                replayer.setSpeed(speed);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean teleportToPlayer(Player watcher, String targetPlayerName) {
        if (!ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            return false;
        }

        Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
        if (replayer == null || !replayer.getNPCList().containsKey(targetPlayerName)) {
            return false;
        }

        // Prüfe ob Spieler in aufgenommener Welt ist
        Replay replay = replayer.getReplay();
        if (!replay.getData().getWorlds().isEmpty()) {
            // Es gibt Welt-Filter - prüfe ob Ziel-Spieler in aufgenommener Welt ist
            // Diese Logik wird in ReplayingUtils.teleportToPlayer implementiert
        }

        return replayer.getUtils().teleportToPlayer(targetPlayerName);
    }

    @Override
    public boolean enableFollowMode(Player watcher, String targetPlayerName) {
        if (!ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            return false;
        }

        Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
        if (replayer == null || !replayer.getNPCList().containsKey(targetPlayerName)) {
            return false;
        }

        replayer.setFollowMode(true);
        replayer.setFollowTarget(targetPlayerName);
        return true;
    }

    @Override
    public boolean disableFollowMode(Player watcher) {
        if (!ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            return false;
        }

        Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
        if (replayer == null) {
            return false;
        }

        replayer.setFollowMode(false);
        replayer.setFollowTarget(null);
        return true;
    }

    @Override
    public boolean isFollowModeEnabled(Player watcher) {
        if (!ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            return false;
        }

        Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
        return replayer != null && replayer.isFollowMode();
    }

    @Override
    public Optional<String> getFollowTarget(Player watcher) {
        if (!ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            return Optional.empty();
        }

        Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
        if (replayer == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(replayer.getFollowTarget());
    }

    @Override
    public List<String> getPlayersInRecordedWorlds(String replayId, long timestamp) {
        Optional<Replay> replayOpt = getActiveReplay(replayId);
        if (!replayOpt.isPresent()) {
            return new ArrayList<>();
        }

        Replay replay = replayOpt.get();
        List<String> recordedWorlds = replay.getData().getWorlds();

        // Wenn keine Welt-Filter, gebe alle Spieler zurück
        if (recordedWorlds.isEmpty()) {
            return new ArrayList<>(replay.getData().getWatchers().keySet());
        }

        // TODO: Filtere Spieler basierend auf ihrer Welt zum gegebenen Zeitstempel
        // Dies erfordert Tracking der Spieler-Welten in den ActionData
        return new ArrayList<>(replay.getData().getWatchers().keySet());
    }

    @Override
    public List<String> getRecordedWorlds(String replayId) {
        Optional<Replay> replayOpt = getActiveReplay(replayId);
        if (replayOpt.isPresent()) {
            return new ArrayList<>(replayOpt.get().getData().getWorlds());
        }

        // Versuche aus gespeichertem Replay zu laden
        try {
            Optional<Replay> loaded = loadReplay(replayId).get();
            if (loaded.isPresent()) {
                return new ArrayList<>(loaded.get().getData().getWorlds());
            }
        } catch (Exception e) {
            // Ignore
        }

        return new ArrayList<>();
    }

    // ========== Loading & Management ==========

    @Override
    public CompletableFuture<Optional<Replay>> loadReplay(String replayId) {
        CompletableFuture<Optional<Replay>> future = new CompletableFuture<>();

        replaySaver.loadReplay(replayId, replay -> future.complete(Optional.ofNullable(replay)));

        return future;
    }

    @Override
    public CompletableFuture<Optional<ReplayInfo>> loadReplayInfo(String replayId) {
        // TODO: Implementierung für schnelles Metadaten-Laden
        // Aktuell wird das komplette Replay geladen
        return loadReplay(replayId).thenApply(replay ->
                replay.map(Replay::getReplayInfo)
        );
    }

    @Override
    public CompletableFuture<Boolean> replayExists(String replayId) {
        return replaySaver.replayExists(replayId);
    }

    @Override
    public CompletableFuture<Void> deleteReplay(String replayId) {
        return CompletableFuture.runAsync(() -> replaySaver.deleteReplay(replayId));
    }

    @Override
    public CompletableFuture<Void> saveReplay(Replay replay) {
        return CompletableFuture.runAsync(() -> replaySaver.saveReplay(replay));
    }

    @Override
    public CompletableFuture<List<String>> listAllReplays() {
        // TODO: Muss in IReplaySaver implementiert werden
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<String>> listReplaysByDuration(int minDurationSeconds) {
        return listAllReplays().thenCompose(allIds -> {
            List<CompletableFuture<Optional<ReplayInfo>>> futures = allIds.stream()
                    .map(this::loadReplayInfo)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        List<String> filtered = new ArrayList<>();
                        for (int i = 0; i < allIds.size(); i++) {
                            Optional<ReplayInfo> info = futures.get(i).join();
                            if (info.isPresent() && info.get().getDuration() >= minDurationSeconds * 20) {
                                filtered.add(allIds.get(i));
                            }
                        }
                        return filtered;
                    });
        });
    }

    @Override
    public CompletableFuture<List<String>> listReplaysByTimeRange(long fromTimestamp, long toTimestamp) {
        return listAllReplays().thenCompose(allIds -> {
            List<CompletableFuture<Optional<ReplayInfo>>> futures = allIds.stream()
                    .map(this::loadReplayInfo)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        List<String> filtered = new ArrayList<>();
                        for (int i = 0; i < allIds.size(); i++) {
                            Optional<ReplayInfo> info = futures.get(i).join();
                            if (info.isPresent()) {
                                long time = info.get().getTime();
                                if (time >= fromTimestamp && time <= toTimestamp) {
                                    filtered.add(allIds.get(i));
                                }
                            }
                        }
                        return filtered;
                    });
        });
    }

    @Override
    public CompletableFuture<List<Replay>> loadReplays(List<String> replayIds) {
        List<CompletableFuture<Optional<Replay>>> futures = replayIds.stream()
                .map(this::loadReplay)
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));
    }

    // ========== Active Replays ==========

    @Override
    public Optional<Replay> getActiveReplay(String replayId) {
        return Optional.ofNullable(Replay.ACTIVE_REPLAYS.get(replayId));
    }

    @Override
    public Optional<Replay> getWatchingReplay(Player watcher) {
        if (ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            Replayer replayer = ReplayHelper.replaySessions.get(watcher.getName());
            if (replayer != null) {
                return Optional.ofNullable(replayer.getReplay());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isWatching(Player watcher) {
        return ReplayHelper.replaySessions.containsKey(watcher.getName());
    }

    // ========== Static Mode ==========

    @Override
    public boolean startStaticMode() {
        StaticModeManager manager = StaticModeManager.getInstance();
        if (!manager.isRecording()) {
            manager.start();
            return true;
        }
        return false;
    }

    @Override
    public boolean startStaticMode(World... worlds) {
        return startStaticMode(Arrays.asList(worlds));
    }

    @Override
    public boolean startStaticMode(List<World> worlds) {
        StaticModeManager manager = StaticModeManager.getInstance();
        if (!manager.isRecording()) {
            manager.start(worlds);
            return true;
        }
        return false;
    }

    @Override
    public boolean startStaticModeForWorlds(String... worldNames) {
        List<World> worlds = Arrays.stream(worldNames)
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return startStaticMode(worlds);
    }

    @Override
    public boolean addWorldToStaticMode(World world) {
        return StaticModeManager.getInstance().addWorld(world);
    }

    @Override
    public boolean removeWorldFromStaticMode(World world) {
        return StaticModeManager.getInstance().removeWorld(world);
    }

    @Override
    public List<World> getStaticModeWorlds() {
        return StaticModeManager.getInstance().getRecordedWorldObjects();
    }

    @Override
    public boolean stopStaticMode() {
        StaticModeManager manager = StaticModeManager.getInstance();
        if (manager.isRecording()) {
            manager.stop();
            return true;
        }
        return false;
    }

    @Override
    public boolean isStaticModeActive() {
        return StaticModeManager.getInstance().isRecording();
    }

    @Override
    public CompletableFuture<Optional<String>> saveStaticReplay(String replayId) {
        return CompletableFuture.supplyAsync(() -> {
            String id = StaticModeManager.getInstance().saveLastMinutes(replayId);
            return Optional.ofNullable(id);
        });
    }

    @Override
    public CompletableFuture<Optional<String>> saveStaticReplay(String replayId, int minutes) {
        // TODO: Muss im StaticModeManager implementiert werden
        // Fallback zur Standard-Methode
        return saveStaticReplay(replayId);
    }

    // ========== Hooks ==========

    @Override
    public void registerHook(IReplayHook hook) {
        hookManager.registerHook(hook);
    }

    @Override
    public void unregisterHook(IReplayHook hook) {
        hookManager.unregisterHook(hook);
    }

    // ========== Singleton ==========

    /**
     * Gibt die Singleton-Instanz der ReplayAPI zurück
     *
     * @return ReplayAPI Instanz
     */
    public static ReplayAPI getInstance() {
        if (instance == null) {
            instance = new ReplayAPI();
        }
        return instance;
    }
}
