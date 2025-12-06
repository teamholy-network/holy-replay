package de.teamholy.replay.api;

import java.util.List;

import de.teamholy.replay.replaysystem.data.ActionData;
import de.teamholy.replay.replaysystem.data.types.PacketData;
import de.teamholy.replay.replaysystem.replaying.Replayer;

public interface IReplayHook {

	List<PacketData> onRecord(String playerName);
	
	void onPlay(ActionData data, Replayer replayer);
}
