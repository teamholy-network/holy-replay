package de.teamholy.replay.filesystem.saving;

import java.util.List;

import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.utils.fetcher.Consumer;

public interface IReplaySaver {

	void saveReplay(Replay replay);
	
	void loadReplay(String replayName, Consumer<Replay> consumer);
	
	boolean replayExists(String replayName);
	
	void deleteReplay(String replayName);
	
	List<String> getReplays();
}
