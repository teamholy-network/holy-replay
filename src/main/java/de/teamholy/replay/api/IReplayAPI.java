package de.teamholy.replay.api;

import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.data.ActionData;
import de.teamholy.replay.replaysystem.data.ReplayInfo;
import de.teamholy.replay.replaysystem.data.types.BlockChangeData;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Hauptinterface für das ReplaySystem.
 * Bietet alle Funktionen für Aufnahme, Wiedergabe und Verwaltung von Replays.
 */
public interface IReplayAPI {

    // ========== Recording ==========

    /**
     * Startet eine Replay-Aufnahme mit den angegebenen Spielern
     *
     * @param replayId ID des Replays (null für auto-generiert)
     * @param players Spieler, die aufgenommen werden sollen
     * @return Das erstellte Replay
     */
    Replay startRecording(String replayId, Player... players);

    /**
     * Startet eine Replay-Aufnahme mit einer Liste von Spielern
     *
     * @param replayId ID des Replays (null für auto-generiert)
     * @param players Liste der Spieler
     * @return Das erstellte Replay
     */
    Replay startRecording(String replayId, List<Player> players);

    /**
     * Startet eine Replay-Aufnahme für bestimmte Welten
     * Alle Spieler in diesen Welten werden automatisch aufgenommen
     *
     * @param replayId ID des Replays (null für auto-generiert)
     * @param worlds Welten, die aufgenommen werden sollen
     * @return Das erstellte Replay
     */
    Replay startRecordingWorlds(String replayId, World... worlds);

    /**
     * Startet eine Replay-Aufnahme für eine Liste von Welten
     * Alle Spieler in diesen Welten werden automatisch aufgenommen
     *
     * @param replayId ID des Replays (null für auto-generiert)
     * @param worlds Liste der Welten
     * @return Das erstellte Replay
     */
    Replay startRecordingWorlds(String replayId, List<World> worlds);

    /**
     * Startet eine Replay-Aufnahme für bestimmte Welten (by name)
     * Alle Spieler in diesen Welten werden automatisch aufgenommen
     *
     * @param replayId ID des Replays (null für auto-generiert)
     * @param worldNames Namen der Welten
     * @return Das erstellte Replay
     */
    Replay startRecordingWorlds(String replayId, String... worldNames);

    /**
     * Stoppt eine laufende Aufnahme
     *
     * @param replayId ID des Replays
     * @param save Ob das Replay gespeichert werden soll
     */
    void stopRecording(String replayId, boolean save);

    /**
     * Stoppt eine laufende Aufnahme mit Option für leere Replays
     *
     * @param replayId ID des Replays
     * @param save Ob das Replay gespeichert werden soll
     * @param ignoreEmpty Ob leere Replays ignoriert werden sollen
     */
    void stopRecording(String replayId, boolean save, boolean ignoreEmpty);

    /*
    * Fügt einer laufenden Aufnahme Aktionsdaten hinzu
    *
    * @param replayId ID des Replays
    * @param actionData Die hinzuzufügenden Aktionsdaten
    *
    * @return void
     */
    void addDataToRecording(String replayId, ActionData actionData);

    /**
     * Fügt allen laufenden Aufnahmen Aktionsdaten hinzu
     *
     * @param actionData Die hinzuzufügenden Aktionsdaten
     * @return void
     */
    void addDataToAllRecordings(ActionData actionData);
    /*
    * Fügt allen laufenden Aufnahme eine Nachricht hinzu
    *
    * @param replayId ID des Replays
    * @param message Die hinzuzufügende Nachricht
    *
     */
    void addMessageToAllRecordings(String message);

    /*
    * Fügt einer laufenden Aufnahme eine Nachricht hinzu
    * @param replayId ID des Replays
    * @param message Die hinzuzü
    * gende Nachricht
    *
     */
    void addMessageToRecording(String replayId, String message);

    /**
     * Fügt einer laufenden Aufnahme einen BlockChange hinzu.
     * Wird verwendet, wenn ein anderes Plugin einen Block verändert/abbaut und
     * dies im Replay festgehalten werden soll.
     *
     * @param replayId ID des Replays
     * @param oldBlock Der vorherige Block
     * @param newBlock Der neue Block (Material)
     *
     */
    void addBlockChangeToRecording(String replayId, Block oldBlock, Material newBlock);

    /**
     * Fügt allen laufenden Aufnahmen einen BlockChange hinzu.
     *
     * @param oldBlock Der vorherige Block
     * @param newBlock Der neue Block (Material)
     *
     */
    void addBlockChangeToAllRecordings(Block oldBlock, Material newBlock);

    // ========== Playback ==========

    /**
     * Spielt ein Replay für einen Zuschauer ab
     *
     * @param replayId ID des Replays
     * @param watcher Zuschauer
     * @return CompletableFuture mit dem Replay (Optional.empty() wenn nicht gefunden)
     */
    CompletableFuture<Optional<Replay>> playReplay(String replayId, Player watcher);

    /**
     * Stoppt die Wiedergabe für einen Zuschauer
     *
     * @param watcher Zuschauer
     * @return true wenn eine Wiedergabe gestoppt wurde
     */
    boolean stopPlayback(Player watcher);

    /**
     * Pausiert die Wiedergabe für einen Zuschauer
     *
     * @param watcher Zuschauer
     * @return true wenn erfolgreich
     */
    boolean pausePlayback(Player watcher);

    /**
     * Setzt die Wiedergabe für einen Zuschauer fort
     *
     * @param watcher Zuschauer
     * @return true wenn erfolgreich
     */
    boolean resumePlayback(Player watcher);

    /**
     * Springt zu einer bestimmten Zeit im Replay
     *
     * @param watcher Zuschauer
     * @param seconds Sekunde zu der gesprungen werden soll
     * @return true wenn erfolgreich
     */
    boolean jumpToTime(Player watcher, int seconds);

    /**
     * Setzt die Wiedergabe-Geschwindigkeit
     *
     * @param watcher Zuschauer
     * @param speed Geschwindigkeit (1.0 = normal, 2.0 = doppelt, etc.)
     * @return true wenn erfolgreich
     */
    boolean setPlaybackSpeed(Player watcher, double speed);

    /**
     * Teleportiert den Zuschauer zu einem Spieler im Replay
     * Funktioniert nur, wenn der Ziel-Spieler in einer aufgenommenen Welt ist
     *
     * @param watcher Zuschauer
     * @param targetPlayerName Name des Ziel-Spielers
     * @return true wenn erfolgreich
     */
    boolean teleportToPlayer(Player watcher, String targetPlayerName);

    /**
     * Aktiviert den Verfolger-Modus für einen Zuschauer
     * Der Zuschauer folgt dem Spieler automatisch, auch zwischen Welten
     *
     * @param watcher Zuschauer
     * @param targetPlayerName Name des zu verfolgenden Spielers
     * @return true wenn erfolgreich
     */
    boolean enableFollowMode(Player watcher, String targetPlayerName);

    /**
     * Deaktiviert den Verfolger-Modus für einen Zuschauer
     *
     * @param watcher Zuschauer
     * @return true wenn erfolgreich
     */
    boolean disableFollowMode(Player watcher);

    /**
     * Prüft ob der Verfolger-Modus für einen Zuschauer aktiv ist
     *
     * @param watcher Zuschauer
     * @return true wenn Verfolger-Modus aktiv
     */
    boolean isFollowModeEnabled(Player watcher);

    /**
     * Gibt den Namen des Spielers zurück, dem der Zuschauer folgt
     *
     * @param watcher Zuschauer
     * @return Optional mit dem Namen des verfolgten Spielers
     */
    Optional<String> getFollowTarget(Player watcher);

    /**
     * Gibt alle Spieler zurück, die in aufgenommenen Welten im Replay sind
     * Wird beim Viewer verwendet um zu filtern welche Spieler angezeigt werden
     *
     * @param replayId ID des Replays
     * @param timestamp Zeitstempel im Replay
     * @return Liste der Spielernamen in aufgenommenen Welten
     */
    List<String> getPlayersInRecordedWorlds(String replayId, long timestamp);

    /**
     * Gibt alle aufgenommenen Welten eines Replays zurück
     *
     * @param replayId ID des Replays
     * @return Liste der Weltnamen
     */
    List<String> getRecordedWorlds(String replayId);

    // ========== Loading & Management ==========

    /**
     * Lädt ein Replay ohne es abzuspielen
     *
     * @param replayId ID des Replays
     * @return CompletableFuture mit dem Replay (Optional.empty() wenn nicht gefunden)
     */
    CompletableFuture<Optional<Replay>> loadReplay(String replayId);

    /**
     * Lädt nur die Metadaten eines Replays (schneller als komplettes Laden)
     *
     * @param replayId ID des Replays
     * @return CompletableFuture mit den Replay-Infos
     */
    CompletableFuture<Optional<ReplayInfo>> loadReplayInfo(String replayId);

    /**
     * Prüft ob ein Replay existiert
     *
     * @param replayId ID des Replays
     * @return CompletableFuture mit true wenn das Replay existiert
     */
    CompletableFuture<Boolean> replayExists(String replayId);

    /**
     * Löscht ein Replay
     *
     * @param replayId ID des Replays
     * @return CompletableFuture das abgeschlossen wird wenn das Replay gelöscht wurde
     */
    CompletableFuture<Void> deleteReplay(String replayId);

    /**
     * Speichert ein Replay
     *
     * @param replay Das zu speichernde Replay
     * @return CompletableFuture das abgeschlossen wird wenn das Replay gespeichert wurde
     */
    CompletableFuture<Void> saveReplay(Replay replay);

    /**
     * Listet alle verfügbaren Replay-IDs auf
     *
     * @return CompletableFuture mit Liste aller Replay-IDs
     */
    CompletableFuture<List<String>> listAllReplays();

    /**
     * Listet Replays nach Mindestdauer
     *
     * @param minDurationSeconds Mindestdauer in Sekunden
     * @return CompletableFuture mit gefilterten Replay-IDs
     */
    CompletableFuture<List<String>> listReplaysByDuration(int minDurationSeconds);

    /**
     * Listet Replays nach Zeitraum
     *
     * @param fromTimestamp Von Zeitstempel (Millisekunden)
     * @param toTimestamp Bis Zeitstempel (Millisekunden)
     * @return CompletableFuture mit gefilterten Replay-IDs
     */
    CompletableFuture<List<String>> listReplaysByTimeRange(long fromTimestamp, long toTimestamp);

    /**
     * Lädt mehrere Replays gleichzeitig (Batch-Loading)
     *
     * @param replayIds Liste der Replay-IDs
     * @return CompletableFuture mit Liste der geladenen Replays
     */
    CompletableFuture<List<Replay>> loadReplays(List<String> replayIds);

    // ========== Active Replays ==========

    /**
     * Holt ein aktives (aufnehmendes) Replay
     *
     * @param replayId ID des Replays
     * @return Optional mit dem Replay
     */
    Optional<Replay> getActiveReplay(String replayId);

    /**
     * Holt das Replay das ein Spieler gerade ansieht
     *
     * @param watcher Zuschauer
     * @return Optional mit dem Replay
     */
    Optional<Replay> getWatchingReplay(Player watcher);

    /**
     * Prüft ob ein Spieler gerade ein Replay ansieht
     *
     * @param watcher Zuschauer
     * @return true wenn der Spieler ein Replay ansieht
     */
    boolean isWatching(Player watcher);

    // ========== Static Mode ==========

    /**
     * Startet den Static Mode (kontinuierliche Aufnahme) für alle Spieler
     *
     * @return true wenn erfolgreich gestartet
     */
    boolean startStaticMode();

    /**
     * Startet den Static Mode für bestimmte Welten
     * Alle Spieler in diesen Welten werden kontinuierlich aufgenommen
     *
     * @param worlds Welten, die aufgenommen werden sollen
     * @return true wenn erfolgreich gestartet
     */
    boolean startStaticMode(World... worlds);

    /**
     * Startet den Static Mode für eine Liste von Welten
     * Alle Spieler in diesen Welten werden kontinuierlich aufgenommen
     *
     * @param worlds Liste der Welten
     * @return true wenn erfolgreich gestartet
     */
    boolean startStaticMode(List<World> worlds);

    /**
     * Startet den Static Mode für bestimmte Welten (by name)
     * Alle Spieler in diesen Welten werden kontinuierlich aufgenommen
     *
     * @param worldNames Namen der Welten
     * @return true wenn erfolgreich gestartet
     */
    boolean startStaticModeForWorlds(String... worldNames);

    /**
     * Fügt eine Welt zum laufenden Static Mode hinzu
     *
     * @param world Welt, die hinzugefügt werden soll
     * @return true wenn erfolgreich
     */
    boolean addWorldToStaticMode(World world);

    /**
     * Entfernt eine Welt aus dem laufenden Static Mode
     *
     * @param world Welt, die entfernt werden soll
     * @return true wenn erfolgreich
     */
    boolean removeWorldFromStaticMode(World world);

    /**
     * Gibt alle Welten zurück, die im Static Mode aufgenommen werden
     *
     * @return Liste der aufgenommenen Welten (leer = alle Welten)
     */
    List<World> getStaticModeWorlds();

    /**
     * Stoppt den Static Mode
     *
     * @return true wenn erfolgreich gestoppt
     */
    boolean stopStaticMode();

    /**
     * Prüft ob Static Mode aktiv ist
     *
     * @return true wenn Static Mode läuft
     */
    boolean isStaticModeActive();

    /**
     * Speichert die letzten Minuten aus dem Static Mode
     *
     * @param replayId ID für das gespeicherte Replay (null für auto-generiert)
     * @return CompletableFuture mit der ID des gespeicherten Replays
     */
    CompletableFuture<Optional<String>> saveStaticReplay(String replayId);

    /**
     * Speichert eine bestimmte Anzahl Minuten aus dem Static Mode
     *
     * @param replayId ID für das gespeicherten Replay (null für auto-generiert)
     * @param minutes Anzahl der zu speichernden Minuten
     * @return CompletableFuture mit der ID des gespeicherten Replays
     */
    CompletableFuture<Optional<String>> saveStaticReplay(String replayId, int minutes);

    // ========== Hooks ==========

    /**
     * Registriert einen Hook für Replay-Events
     *
     * @param hook Der zu registrierende Hook
     */
    void registerHook(IReplayHook hook);

    /**
     * Entfernt einen Hook
     *
     * @param hook Der zu entfernende Hook
     */
    void unregisterHook(IReplayHook hook);
}
