package de.teamholy.replay.filesystem.saving;


import de.teamholy.replay.database.DatabaseService;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.data.ReplayData;
import de.teamholy.replay.replaysystem.data.ReplayInfo;
import de.teamholy.replay.utils.fetcher.Consumer;
import org.bukkit.Bukkit;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ReplaySaver implements IReplaySaver {

    private DatabaseService databaseService;
    public static Map<String, ReplayInfo> replayCache = new HashMap<>();

    private static final Map<String, byte[]> dataCache = new ConcurrentHashMap<>();

    public ReplaySaver(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public static ReplayInfo getInfo(String replay) {
        if (replayCache != null && replayCache.containsKey(replay)) return replayCache.get(replay);

        return null;
    }

    @Override
    public void saveReplay(Replay replay) {
        try {
            byte[] data;
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

            try (ZstdOutputStream zstdOut = new ZstdOutputStream(byteOut, 7);
                 ObjectOutputStream objectOut = new ObjectOutputStream(zstdOut)) {

                objectOut.writeObject(replay.getData());
                objectOut.flush();
                zstdOut.flush();
            }

            data = byteOut.toByteArray();

            if (replay.getReplayInfo() == null) {
                replay.setReplayInfo(new ReplayInfo(replay.getId(), System.currentTimeMillis(), replay.getData().getDuration()));
            }

            dataCache.put(replay.getId(), data);
            Bukkit.getLogger().info("[ReplaySaver] Saved replay '" + replay.getId() + "' to cache (" + data.length + " bytes)");

            databaseService.addReplay(replay.getId(), replay.getReplayInfo().getDuration(), replay.getReplayInfo().getTime(), data);

            updateCache(replay.getId(), replay.getReplayInfo());

        } catch (Exception e) {
            Bukkit.getLogger().severe("[ReplaySaver] Failed to save replay '" + replay.getId() + "': " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void loadReplay(String replayName, Consumer<Replay> consumer) {
        if (dataCache.containsKey(replayName)) {
            try {
                byte[] cachedData = dataCache.get(replayName);
                Bukkit.getLogger().info("[ReplaySaver] Loading replay '" + replayName + "' from cache (" + cachedData.length + " bytes)");

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(cachedData);
                     ZstdInputStream zstdIn = new ZstdInputStream(byteIn);
                     ObjectInputStream objectIn = new ObjectInputStream(zstdIn)) {

                    ReplayData replayData = (ReplayData) objectIn.readObject();
                    consumer.accept(new Replay(replayName, replayData));
                    return;
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("[ReplaySaver] Cache load failed for '" + replayName + "', trying DB: " + e.getMessage());
            }
        }

        Bukkit.getLogger().info("[ReplaySaver] Loading replay '" + replayName + "' from database");
        databaseService.getReplayData(replayName).thenAccept(replayBytes -> {
            if (replayBytes == null || replayBytes.length == 0) {
                Bukkit.getLogger().warning("[ReplaySaver] No data found for replay: " + replayName);
                consumer.accept(null);
                return;
            }

            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(replayBytes);
                 ZstdInputStream zstdIn = new ZstdInputStream(byteIn);
                 ObjectInputStream objectIn = new ObjectInputStream(zstdIn)) {

                ReplayData replayData = (ReplayData) objectIn.readObject();
                Bukkit.getLogger().info("[ReplaySaver] Successfully loaded replay '" + replayName + "' from DB (" + replayBytes.length + " bytes)");
                consumer.accept(new Replay(replayName, replayData));

            } catch (Exception e) {
                Bukkit.getLogger().severe("[ReplaySaver] Failed to load replay '" + replayName + "': " + e.getMessage());
                e.printStackTrace();
                consumer.accept(null);
            }
        }).exceptionally(ex -> {
            Bukkit.getLogger().severe("[ReplaySaver] Database error loading replay '" + replayName + "': " + ex.getMessage());
            ex.printStackTrace();
            consumer.accept(null);
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> replayExists(String replayName) {
        return databaseService.exists(replayName);
    }

    @Override
    public void deleteReplay(String replayName) {
        databaseService.deleteReplay(replayName);
        updateCache(replayName, null);
        dataCache.remove(replayName);
        Bukkit.getLogger().info("[ReplaySaver] Deleted replay '" + replayName + "'");
    }

    private void updateCache(String id, ReplayInfo info) {
        if (info != null && id != null) {
            replayCache.put(id, info);
        } else replayCache.remove(id);
    }
}
