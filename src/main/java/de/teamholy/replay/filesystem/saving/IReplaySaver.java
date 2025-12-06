package de.teamholy.replay.filesystem.saving;

import java.util.concurrent.CompletableFuture;

import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.utils.fetcher.Consumer;

public interface IReplaySaver {

	void saveReplay(Replay replay);
	
	void loadReplay(String replayName, Consumer<Replay> consumer);
	
	CompletableFuture<Boolean> replayExists(String replayName);
	
	void deleteReplay(String replayName);
	
}
