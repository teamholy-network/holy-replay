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
        CompletableFuture.runAsync(() -> {
            try {
                ReplayEntity entity = new ReplayEntity(id, duration, time, data);
                replayRepository.save(entity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add replay: " + id, e);
            }
        });
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
        CompletableFuture.runAsync(() -> {
            try {
                replayRepository.deleteById(id);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete replay: " + id, e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> exists(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return replayRepository.existsById(id);
            } catch (Exception e) {
                throw new RuntimeException("Failed to check replay existence: " + id, e);
            }
        });
    }

}

