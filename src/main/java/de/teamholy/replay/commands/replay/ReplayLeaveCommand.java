package de.teamholy.replay.commands.replay;




import de.teamholy.replay.filesystem.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.commands.AbstractCommand;
import de.teamholy.replay.commands.SubCommand;
import de.teamholy.replay.replaysystem.replaying.ReplayHelper;
import de.teamholy.replay.replaysystem.replaying.Replayer;

public class ReplayLeaveCommand extends SubCommand {

	public ReplayLeaveCommand(AbstractCommand parent) {
		super(parent, "leave", "Leave your Replay", "leave", true);
	}

	@Override
	public boolean execute(CommandSender cs, Command cmd, String label, String[] args) {		
		Player p = (Player) cs;
		
		if (ReplayHelper.replaySessions.containsKey(p.getName())) {
			Replayer replayer = ReplayHelper.replaySessions.get(p.getName());
			
			replayer.stop();
			
		} else {
			Messages.REPLAY_LEAVE.send(cs);
		}
		
		return true;
	}

	
}
