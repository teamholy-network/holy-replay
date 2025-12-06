package de.teamholy.replay.database;


import java.util.concurrent.CompletableFuture;

public abstract class IDatabaseService {

	public abstract void addReplay(String id, int duration, Long time, byte[] data);

	public abstract CompletableFuture<byte[]> getReplayData(String id);

	public abstract void deleteReplay(String id);

	public abstract CompletableFuture<Boolean> exists(String id);


}
