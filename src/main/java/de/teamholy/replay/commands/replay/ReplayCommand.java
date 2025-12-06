package de.teamholy.replay.commands.replay;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.commands.AbstractCommand;
import de.teamholy.replay.commands.MessageFormat;
import de.teamholy.replay.commands.SubCommand;
import de.teamholy.replay.filesystem.Messages;

public class ReplayCommand extends AbstractCommand {

	public ReplayCommand() {
		super("Replay", ReplaySystem.PREFIX + "AdvancedReplay §ev" + ReplaySystem.getInstance().getDescription().getVersion(), "replay.command");
	}

	@Override
	protected MessageFormat setupFormat() {
		return new MessageFormat()
				.overview(Messages.COMMAND_OVERVIEW.getFullMessage())
				.syntax(Messages.COMMAND_SYNTAX.getFullMessage())
				.permission(Messages.COMMAND_NO_PERMISSION.getFullMessage())
				.notFound(Messages.COMMAND_NOTFOUND.getFullMessage());
	}

	@Override
	protected SubCommand[] setupCommands() {
		
		return new SubCommand[] { new ReplayStartCommand(this), 
				new ReplayStopCommand(this).addAlias("save"), 
				new ReplayPlayCommand(this), 
				new ReplayDeleteCommand(this).addAlias("remove"),
				new ReplayJumpCommand(this),
				new ReplayLeaveCommand(this),
				new ReplayInfoCommand(this),
				new ReplayListCommand(this), 
				new ReplayReloadCommand(this),
				new ReplayReformatCommand(this),
				new ReplayMigrateCommand(this) };
	}

}
