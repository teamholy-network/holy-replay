package de.teamholy.replay.commands.replay;



import de.teamholy.replay.filesystem.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.commands.AbstractCommand;
import de.teamholy.replay.commands.SubCommand;
import de.teamholy.replay.filesystem.ConfigManager;

public class ReplayReloadCommand extends SubCommand {

	public ReplayReloadCommand(AbstractCommand parent) {
		super(parent, "reload", "Reloads the config", "reload", false);
	}

	@Override
	public boolean execute(CommandSender cs, Command cmd, String label, String[] args) {
		ConfigManager.reloadConfig();
		Messages.REPLAY_RELOAD.send(cs);
		return true;
	}

	
}
