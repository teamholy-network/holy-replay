package de.teamholy.replay.commands.replay;


import java.util.List;
import java.util.stream.Collectors;

import de.teamholy.replay.filesystem.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.commands.AbstractCommand;
import de.teamholy.replay.commands.SubCommand;
import de.teamholy.replay.filesystem.saving.ReplaySaver;

public class ReplayDeleteCommand extends SubCommand {

	public ReplayDeleteCommand(AbstractCommand parent) {
		super(parent, "delete", "Deletes a replay", "delete <Name>", false);
	}

	@Override
	public boolean execute(CommandSender cs, Command cmd, String label, String[] args) {
		if (args.length != 2) return false;
		
		String name = args[1];
		
		if (ReplaySaver.exists(name)) {
			ReplaySaver.delete(name);
			Messages.REPLAY_DELETE.send(cs);
		} else {
			Messages.REPLAY_NOT_FOUND.send(cs);
		}
		
		return true;
	}
	
	@Override
	public List<String> onTab(CommandSender cs, Command cmd, String label, String[] args) {
		return ReplaySaver.getReplays().stream()
				.filter(name -> StringUtil.startsWithIgnoreCase(name, args.length > 1 ? args[1] : null))
				.collect(Collectors.toList());
	}

	
}
