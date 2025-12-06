package de.teamholy.replay.api.examples;

import de.teamholy.replay.api.IReplayAPI;
import de.teamholy.replay.api.ReplayAPI;
import de.teamholy.replay.replaysystem.Replay;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * Einfache Beispiele für die Verwendung der ReplayAPI
 */
public class SimpleAPIExamples {

  private final IReplayAPI api = ReplayAPI.getInstance();

  /**
   * Beispiel 1: Einfache Aufnahme und Wiedergabe
   */
  public void basicRecordingAndPlayback(Player p1, Player p2, Player viewer) {
    // Aufnahme starten
    Replay replay = api.startRecording("mein_replay", p1, p2);
    System.out.println("Recording gestartet: " + replay.getId());

    // Nach einiger Zeit: Aufnahme stoppen
    api.stopRecording("mein_replay", true)
        .thenRun(() -> System.out.println("Replay gespeichert!"));

    // Replay abspielen
    api.playReplay("mein_replay", viewer)
        .thenAccept(r -> {
          if (r.isPresent()) {
            System.out.println("Replay wird abgespielt!");
          }
        });
  }

  /**
   * Beispiel 2: Wiedergabe steuern
   */
  public void controlPlayback(Player viewer) {
    // Pausieren
    api.pausePlayback(viewer);

    // Fortsetzen
    api.resumePlayback(viewer);

    // Zu Sekunde 30 springen
    api.jumpToTime(viewer, 30);

    // Geschwindigkeit auf 2x setzen
    api.setPlaybackSpeed(viewer, 2.0);

    // Stoppen
    api.stopPlayback(viewer);
  }

  /**
   * Beispiel 3: Replay-Verwaltung
   */
  public void manageReplays() {
    // Existiert das Replay?
    api.replayExists("test_replay")
        .thenAccept(exists -> {
          if (exists) {
            System.out.println("Replay existiert!");
          }
        });

    // Replay laden
    api.loadReplay("test_replay")
        .thenAccept(replay -> {
          replay.ifPresent(r -> {
            System.out.println("Geladen: " + r.getId());
          });
        });

    // Replay löschen
    api.deleteReplay("old_replay")
        .thenRun(() -> System.out.println("Gelöscht!"));
  }

  /**
   * Beispiel 4: Replays filtern
   */
  public void filterReplays() {
    // Alle Replays
    api.listAllReplays()
        .thenAccept(all -> System.out.println("Gesamt: " + all.size()));

    // Nur lange Replays (> 5 Minuten)
    api.listReplaysByDuration(300)
        .thenAccept(long_replays -> {
          System.out.println("Lange Replays: " + long_replays.size());
        });

    // Replays der letzten 24 Stunden
    long yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
    api.listReplaysByTimeRange(yesterday, System.currentTimeMillis())
        .thenAccept(recent -> {
          System.out.println("Aktuelle: " + recent.size());
        });
  }

  /**
   * Beispiel 5: Batch-Loading
   */
  public void batchLoad() {
    List<String> ids = Arrays.asList("replay1", "replay2", "replay3");

    api.loadReplays(ids)
        .thenAccept(replays -> {
          System.out.println("Geladen: " + replays.size() + " Replays");
          for (Replay r : replays) {
            System.out.println("- " + r.getId());
          }
        });
  }

  /**
   * Beispiel 6: Static Mode
   */
  public void useStaticMode() {
    // Starten
    if (api.startStaticMode()) {
      System.out.println("Static Mode aktiviert!");
    }

    // Später: Wichtiges Ereignis speichern
    api.saveStaticReplay("wichtig")
        .thenAccept(id -> {
          id.ifPresent(replayId ->
              System.out.println("Gespeichert: " + replayId)
          );
        });

    // Stoppen
    api.stopStaticMode();
  }

  /**
   * Beispiel 7: Zuschauer-Status prüfen
   */
  public void checkViewerStatus(Player player) {
    // Sieht der Spieler ein Replay?
    if (api.isWatching(player)) {
      // Welches Replay?
      api.getWatchingReplay(player)
          .ifPresent(replay -> {
            System.out.println(player.getName() + " sieht: " + replay.getId());
          });
    }
  }

  /**
   * Beispiel 8: PvP-Aufnahme-System
   */
  public void pvpRecordingSystem(Player attacker, Player victim) {
    String replayId = "pvp_" + attacker.getName() + "_vs_" + victim.getName();

    // Aufnahme starten
    api.startRecording(replayId, attacker, victim);

    // Bei Tod: Stoppen und speichern
    api.stopRecording(replayId, true, true)
        .thenCompose(v -> api.replayExists(replayId))
        .thenAccept(exists -> {
          if (exists) {
            attacker.sendMessage("§aDein Kampf wurde aufgenommen: " + replayId);
            victim.sendMessage("§aDer Kampf wurde aufgenommen: " + replayId);
          }
        });
  }

  /**
   * Beispiel 9: Kompletter Workflow
   */
  public void completeWorkflow(Player p1, Player p2, Player viewer) {
    String id = "complete_example";

    // 1. Aufnahme starten
    Replay replay = api.startRecording(id, p1, p2);
    System.out.println("1. Aufnahme gestartet");

    // 2. Aufnahme stoppen
    api.stopRecording(id, true)
        .thenCompose(v -> {
          System.out.println("2. Aufnahme gestoppt");
          // 3. Existenz prüfen
          return api.replayExists(id);
        })
        .thenCompose(exists -> {
          System.out.println("3. Existiert: " + exists);
          // 4. Abspielen
          return api.playReplay(id, viewer);
        })
        .thenAccept(r -> {
          System.out.println("4. Wiedergabe gestartet");
          // 5. Steuerung
          api.setPlaybackSpeed(viewer, 1.5);
          System.out.println("5. Geschwindigkeit auf 1.5x gesetzt");
        });
  }

  /**
   * Beispiel 10: Auto-Cleanup
   */
  public void autoCleanup() {
    long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

    api.listAllReplays().thenAccept(allIds -> {
      System.out.println("Prüfe " + allIds.size() + " Replays...");

      for (String id : allIds) {
        api.loadReplayInfo(id).thenAccept(info -> {
          info.ifPresent(i -> {
            if (i.getTime() < weekAgo) {
              api.deleteReplay(id).thenRun(() -> {
                System.out.println("Gelöscht: " + id);
              });
            }
          });
        });
      }
    });
  }
}
