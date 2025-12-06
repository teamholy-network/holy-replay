package de.teamholy.replay.commands.replay;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.teamholy.replay.filesystem.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.commands.AbstractCommand;
import de.teamholy.replay.commands.SubCommand;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.filesystem.saving.DatabaseReplaySaver;
import de.teamholy.replay.filesystem.saving.DefaultReplaySaver;
import de.teamholy.replay.filesystem.saving.IReplaySaver;
import de.teamholy.replay.filesystem.saving.ReplaySaver;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.utils.LogUtils;
import de.teamholy.replay.utils.fetcher.Consumer;

public class ReplayMigrateCommand extends SubCommand {

	private List<String> options = Arrays.asList("file", "database");
	
	public ReplayMigrateCommand(AbstractCommand parent) {
		super(parent, "migrate", "Migrate all replays", "migrate <File|Database>", false);
		
		this.setEnabled(false);
	}

	@Override
	public boolean execute(CommandSender cs, Command cmd, String label, String[] args) {
		if (args.length != 2) return false;
		
		String option = args[1].toLowerCase();
		if (options.contains(option)) {
			IReplaySaver migrationSaver = null;
			
			if (option.equalsIgnoreCase("file") && ReplaySaver.replaySaver instanceof DatabaseReplaySaver) {
				migrationSaver = new DefaultReplaySaver();
				
			} else if (option.equalsIgnoreCase("database") && ReplaySaver.replaySaver instanceof DefaultReplaySaver) {
				ConfigManager.USE_DATABASE = true;
				ConfigManager.loadData(false);

				migrationSaver = new DatabaseReplaySaver();
			} else {
				Messages.REPLAY_MIGRATE_ERROR.send(cs);
				return true;
			}

			Messages.REPLAY_MIGRATE.arg("option", option).send(cs);
			for (String replayName : ReplaySaver.getReplays()) {
				this.migrate(replayName, migrationSaver);
			}
			
			
		} else {
			Messages.REPLAY_MIGRATE_INVALID.arg("options", options.stream().collect(Collectors.joining("|", "<", ">"))).send(cs);
		}
		
		return true;
	}
	
	private void migrate(String replayName, IReplaySaver saver) {
		
		ReplaySaver.load(replayName, new Consumer<Replay>() {
			
			@Override
			public void accept(Replay replay) {
				try {
					if (!saver.replayExists(replayName)) {
						LogUtils.log("Migrating " + replayName + "...");

						saver.saveReplay(replay);
					}
				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
	}
	
	@Override
	public List<String> onTab(CommandSender cs, Command cmd, String label, String[] args) {
		return options.stream()
				.filter(option -> StringUtil.startsWithIgnoreCase(option, args[1]))
				.collect(Collectors.toList());
	}

}
