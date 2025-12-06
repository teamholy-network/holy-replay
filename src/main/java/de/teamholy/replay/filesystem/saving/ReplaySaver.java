package de.teamholy.replay.filesystem.saving;


import de.teamholy.replay.database.DatabaseService;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.data.ReplayData;
import de.teamholy.replay.replaysystem.data.ReplayInfo;
import de.teamholy.replay.utils.fetcher.Consumer;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// TODO: Use NIO for better performance and implement en2do to upload the repalys into the mongo database
public class ReplaySaver implements IReplaySaver {

    private DatabaseService databaseService;
    public static Map<String, ReplayInfo> replayCache = new HashMap<>();

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
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ZstdOutputStream zstdOut = new ZstdOutputStream(byteOut);
                 ObjectOutputStream objectOut = new ObjectOutputStream(zstdOut)) {

                objectOut.writeObject(replay.getData());
                objectOut.flush();
                zstdOut.flush();

                data = byteOut.toByteArray();
            }

            if (replay.getReplayInfo() == null) {
                replay.setReplayInfo(new ReplayInfo(replay.getId(), System.currentTimeMillis(), replay.getData().getDuration()));
            }

            databaseService.addReplay(replay.getId(), replay.getReplayInfo().getDuration(), replay.getReplayInfo().getTime(), data);

            updateCache(replay.getId(), replay.getReplayInfo());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void loadReplay(String replayName, Consumer<Replay> consumer) {
        databaseService.getReplayData(replayName).thenAccept(replay -> {
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(replay);
                 ZstdInputStream zstdIn = new ZstdInputStream(byteIn);
                 ObjectInputStream objectIn = new ObjectInputStream(zstdIn)) {

                ReplayData replayData = (ReplayData) objectIn.readObject();

                consumer.accept(new Replay(replayName, replayData));
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    }

    private void updateCache(String id, ReplayInfo info) {
        if (info != null && id != null) {
            replayCache.put(id, info);
        } else replayCache.remove(id);
    }
}
