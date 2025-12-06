package de.teamholy.replay.commands.replay;



import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.commands.AbstractCommand;
import de.teamholy.replay.commands.SubCommand;
import de.teamholy.replay.filesystem.saving.DefaultReplaySaver;
import de.teamholy.replay.filesystem.saving.ReplaySaver;

public class ReplayReformatCommand extends SubCommand {

	public ReplayReformatCommand(AbstractCommand parent) {
		super(parent, "reformat", "Reformat the replays", "reformat", false);
		
		this.setEnabled(false);
	}

	@Override
	public boolean execute(CommandSender cs, Command cmd, String label, String[] args) {
		cs.sendMessage(ReplaySystem.PREFIX + "Reformatting Replay files...");
		((DefaultReplaySaver)ReplaySaver.replaySaver).reformatAll();
		cs.sendMessage("§aFinished. Check console for details.");
		
		return true;
	}

	
}
