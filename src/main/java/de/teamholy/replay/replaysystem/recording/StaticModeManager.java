package de.teamholy.replay.replaysystem.recording;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.data.ActionData;
import de.teamholy.replay.utils.StringUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages continuous recording in STATIC mode with a rolling buffer.
 * Records all players continuously and saves the last X minutes on demand.
 * Supports world-based filtering.
 */
@Getter
public class StaticModeManager {

    private static StaticModeManager instance;

    public static String CONTINUOUS_REPLAY_ID = "__static_continuous__";

    private Replay continuousReplay;
    private final Map<String, Long> playerJoinTimes = new ConcurrentHashMap<>();
    private final List<String> recordedWorlds = new ArrayList<>();
    private boolean isRunning = false;

    private StaticModeManager() {
    }

    public static StaticModeManager getInstance() {
        if (instance == null) {
            instance = new StaticModeManager();
        }
        return instance;
    }

    /**
     * Starts continuous recording of all players
     */
    public void start() {
        start(Collections.emptyList());
    }

    /**
     * Starts continuous recording of players in specific worlds
     *
     * @param worlds Welten, die aufgenommen werden sollen (leer = alle Welten)
     */
    public void start(List<World> worlds) {
        if (isRunning) {
            return;
        }

        isRunning = true;

        // Speichere Weltnamen für Filter
        recordedWorlds.clear();
        if (!worlds.isEmpty()) {
            recordedWorlds.addAll(worlds.stream()
                    .map(World::getName)
                    .toList());
        }

        // Create a continuous replay with a special ID
        continuousReplay = new Replay();
        continuousReplay.setId(CONTINUOUS_REPLAY_ID);

        // Start recording players in specified worlds (or all if empty)
        List<Player> playersToRecord = getPlayersInRecordedWorlds();
        if (!playersToRecord.isEmpty()) {
            continuousReplay.recordAll(playersToRecord);

            // Setze Welten-Filter in den Replay-Daten
            if (!recordedWorlds.isEmpty()) {
                continuousReplay.getData().setWorlds(new ArrayList<>(recordedWorlds));
            }

            // Track join times
            long now = System.currentTimeMillis();
            for (Player player : playersToRecord) {
                playerJoinTimes.put(player.getName(), now);
            }
        } else {
            // Starte leere Aufnahme, Spieler werden beim Join hinzugefügt
            continuousReplay.recordAll(new ArrayList<>());
            if (!recordedWorlds.isEmpty()) {
                continuousReplay.getData().setWorlds(new ArrayList<>(recordedWorlds));
            }
        }

        String worldInfo = recordedWorlds.isEmpty() ? "all worlds" : "worlds: " + String.join(", ", recordedWorlds);
        Bukkit.getLogger().info("[ReplaySystem] Static Mode: Continuous recording started for " + worldInfo);
    }

    /**
     * Stops continuous recording
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        isRunning = false;

        if (continuousReplay != null && continuousReplay.isRecording()) {
            continuousReplay.getRecorder().stop(false);
        }

        playerJoinTimes.clear();
        recordedWorlds.clear();
        continuousReplay = null;

        Bukkit.getLogger().info("[ReplaySystem] Static Mode: Continuous recording stopped");
    }

    /**
     * Adds a world to the recording
     *
     * @param world Welt zum Hinzufügen
     * @return true wenn erfolgreich
     */
    public boolean addWorld(World world) {
        if (!isRunning || world == null) {
            return false;
        }

        String worldName = world.getName();
        if (!recordedWorlds.contains(worldName)) {
            recordedWorlds.add(worldName);

            if (continuousReplay != null) {
                continuousReplay.getData().getWorlds().add(worldName);

                // Füge alle Spieler in dieser Welt hinzu
                for (Player player : world.getPlayers()) {
                    onPlayerJoin(player);
                }
            }

            Bukkit.getLogger().info("[ReplaySystem] Static Mode: Added world '" + worldName + "' to recording");
            return true;
        }
        return false;
    }

    /**
     * Removes a world from the recording
     *
     * @param world Welt zum Entfernen
     * @return true wenn erfolgreich
     */
    public boolean removeWorld(World world) {
        if (!isRunning || world == null) {
            return false;
        }

        String worldName = world.getName();
        if (recordedWorlds.remove(worldName)) {
            if (continuousReplay != null) {
                continuousReplay.getData().getWorlds().remove(worldName);
            }

            Bukkit.getLogger().info("[ReplaySystem] Static Mode: Removed world '" + worldName + "' from recording");
            return true;
        }
        return false;
    }

    /**
     * Gets all worlds that are being recorded
     *
     * @return Liste der aufgenommenen Welten
     */
    public List<World> getRecordedWorldObjects() {
        return recordedWorlds.stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a player should be recorded based on world filter
     *
     * @param player Spieler
     * @return true wenn der Spieler aufgenommen werden soll
     */
    private boolean shouldRecordPlayer(Player player) {
        if (recordedWorlds.isEmpty()) {
            return true; // Keine Filter = alle Spieler
        }
        return recordedWorlds.contains(player.getWorld().getName());
    }

    /**
     * Gets all players that are in recorded worlds
     */
    private List<Player> getPlayersInRecordedWorlds() {
        if (recordedWorlds.isEmpty()) {
            return new ArrayList<>(Bukkit.getOnlinePlayers());
        }

        return Bukkit.getOnlinePlayers().stream()
                .filter(this::shouldRecordPlayer)
                .collect(Collectors.toList());
    }

    /**
     * Called when a player joins - adds them to continuous recording if in recorded world
     */
    public void onPlayerJoin(Player player) {
        if (!isRunning || continuousReplay == null || !continuousReplay.isRecording()) {
            return;
        }

        // Prüfe ob Spieler in aufgenommener Welt ist
        if (!shouldRecordPlayer(player)) {
            return;
        }

        Recorder recorder = continuousReplay.getRecorder();
        if (!recorder.getPlayers().contains(player.getName())) {
            recorder.getPlayers().add(player.getName());
            recorder.getData().getWatchers().put(player.getName(), new PlayerWatcher(player.getName()));
            recorder.createSpawnAction(player, player.getLocation(), false);

            playerJoinTimes.put(player.getName(), System.currentTimeMillis());

            Bukkit.getLogger().info("[ReplaySystem] Static Mode: Added " + player.getName() + " to continuous recording");
        }
    }

    /**
     * Called when a player changes world - check if still in recorded world
     */
    public void onPlayerChangeWorld(Player player, World from, World to) {
        if (!isRunning || continuousReplay == null || !continuousReplay.isRecording()) {
            return;
        }

        boolean wasRecorded = recordedWorlds.isEmpty() || recordedWorlds.contains(from.getName());
        boolean shouldRecord = shouldRecordPlayer(player);

        if (wasRecorded && !shouldRecord) {
            // Spieler verlässt aufgenommene Welt - entferne aus Aufnahme
            Bukkit.getLogger().info("[ReplaySystem] Static Mode: " + player.getName() + " left recorded world");
        } else if (!wasRecorded && shouldRecord) {
            // Spieler betritt aufgenommene Welt - füge zur Aufnahme hinzu
            onPlayerJoin(player);
        }
    }

    /**
     * Called when a player leaves - keeps their data in the buffer
     */
    public void onPlayerLeave(Player player) {
        if (!isRunning) {
            return;
        }

        playerJoinTimes.remove(player.getName());

        Bukkit.getLogger().info("[ReplaySystem] Static Mode: " + player.getName() + " left, data kept in buffer");
    }

    /**
     * Saves the last X minutes (configured duration) of recording with a custom ID.
     *
     * IMPORTANT: This method does NOT stop or modify the main continuous recording!
     * - Multiple saves can be called in quick succession (e.g., 2 hackers within 2 minutes)
     * - Each save creates an independent copy of the buffer data
     * - The main recorder continues running and collecting data
     * - Example: Hacker1 detected → save() → 10 seconds later → Hacker2 detected → save()
     *            Both replays will contain their respective timeframes
     *
     * @param customId Custom ID for the replay, or null for auto-generated ID
     * @return The ID of the saved replay, or null if static mode is not running
     */
    public synchronized String saveLastMinutes(String customId) {
        if (!isRunning || continuousReplay == null || !continuousReplay.isRecording()) {
            return null;
        }

        Recorder recorder = continuousReplay.getRecorder();

        // Snapshot current state - main recorder continues unaffected
        int currentTick = recorder.getCurrentTick();
        int bufferDuration = ConfigManager.STATIC_MODE_DURATION * 20; // Convert seconds to ticks

        // Calculate the start tick for the buffer window
        int startTick = Math.max(0, currentTick - bufferDuration);

        // Create a new independent replay with filtered data
        Replay savedReplay = new Replay();
        savedReplay.setId(customId != null ? customId : StringUtils.getRandomString(8));

        // Copy only the actions from the last X minutes (deep copy - no reference to original)
        HashMap<Integer, List<ActionData>> filteredActions = new HashMap<>();

        for (Map.Entry<Integer, List<ActionData>> entry : recorder.getData().getActions().entrySet()) {
            int tick = entry.getKey();

            // Only include actions within the buffer window
            if (tick >= startTick) {
                // Adjust tick numbers to start from 0
                int adjustedTick = tick - startTick;
                // Create new ArrayList to avoid reference issues
                filteredActions.put(adjustedTick, new ArrayList<>(entry.getValue()));
            }
        }

        // Set replay data - all copies, no references to original recorder data
        savedReplay.getData().setActions(filteredActions);
        savedReplay.getData().setDuration(Math.min(bufferDuration, currentTick));
        savedReplay.getData().setWatchers(new HashMap<>(recorder.getData().getWatchers()));

        // Kopiere Welten-Filter
        if (!recordedWorlds.isEmpty()) {
            savedReplay.getData().setWorlds(new ArrayList<>(recordedWorlds));
        }

        // Set replay info
        savedReplay.setReplayInfo(new de.teamholy.replay.replaysystem.data.ReplayInfo(
                savedReplay.getId(),
                System.currentTimeMillis(),
                Math.min(bufferDuration, currentTick)
        ));

        // Save the replay asynchronously to not block the main recorder
        ReplaySystem.getInstance().getReplaySaver().saveReplay(savedReplay);

        Bukkit.getLogger().info("[ReplaySystem] Static Mode: Saved last " + ConfigManager.STATIC_MODE_DURATION +
                " seconds as replay '" + savedReplay.getId() + "' (main recorder continues)");

        return savedReplay.getId();
    }

    /**
     * Gets the current recording duration in seconds
     */
    public int getCurrentDuration() {
        if (!isRunning || continuousReplay == null || !continuousReplay.isRecording()) {
            return 0;
        }

        return continuousReplay.getRecorder().getCurrentTick() / 20;
    }

    /**
     * Checks if static mode is currently running
     */
    public boolean isRecording() {
        return isRunning && continuousReplay != null && continuousReplay.isRecording();
    }
}

