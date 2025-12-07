package de.teamholy.replay.database;

import de.teamholy.replay.database.entity.ReplayEntity;
import de.teamholy.replay.database.repository.ReplayRepository;
import eu.koboo.en2do.MongoManager;

import java.util.concurrent.CompletableFuture;

public class DatabaseService extends IDatabaseService {

    private final ReplayRepository replayRepository;

    public DatabaseService(MongoManager mongoManager) {
        replayRepository = mongoManager.create(ReplayRepository.class);
    }

    @Override
    public void addReplay(String id, int duration, Long time, byte[] data) {
        ReplayEntity entity = new ReplayEntity(id, duration, time, data);
        replayRepository.asyncSave(entity);
    }

    @Override
    public CompletableFuture<byte[]> getReplayData(String id) {
        return replayRepository.asyncFindFirstById(id)
                .thenApply(entity -> {
                    if (entity != null) {
                        return entity.getData();
                    }
                    return null;
                })
                .exceptionally(e -> {
                    throw new RuntimeException("Failed to get replay data: " + id, e);
                });
    }

    @Override
    public void deleteReplay(String id) {
        replayRepository.asyncDeleteById(id);
    }

    @Override
    public CompletableFuture<Boolean> exists(String id) {
        return replayRepository.asyncExistsById(id);
    }

}

