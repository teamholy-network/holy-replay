package de.teamholy.replay.api;

import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.data.ActionData;
import de.teamholy.replay.replaysystem.data.ReplayInfo;
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
     * Stoppt eine laufende Aufnahme
     *
     * @param replayId ID des Replays
     * @param save Ob das Replay gespeichert werden soll
     * @return CompletableFuture das abgeschlossen wird wenn das Replay gestoppt wurde
     */
    void stopRecording(String replayId, boolean save);

    /**
     * Stoppt eine laufende Aufnahme mit Option für leere Replays
     *
     * @param replayId ID des Replays
     * @param save Ob das Replay gespeichert werden soll
     * @param ignoreEmpty Ob leere Replays ignoriert werden sollen
     * @return CompletableFuture das abgeschlossen wird wenn das Replay gestoppt wurde
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
    * @param message Die hinzuzufü
    * gende Nachricht
    *
     */
    void addMessageToRecording(String replayId, String message);

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
     * Startet den Static Mode (kontinuierliche Aufnahme)
     *
     * @return true wenn erfolgreich gestartet
     */
    boolean startStaticMode();

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
     * @param replayId ID für das gespeicherte Replay (null für auto-generiert)
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

