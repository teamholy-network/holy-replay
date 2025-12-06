package de.teamholy.replay.commands.replay;




import java.util.List;
import java.util.stream.Collectors;

import de.teamholy.replay.filesystem.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.commands.AbstractCommand;
import de.teamholy.replay.commands.SubCommand;
import de.teamholy.replay.filesystem.saving.ReplaySaver;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.replaying.ReplayHelper;
import de.teamholy.replay.utils.fetcher.Consumer;

public class ReplayPlayCommand extends SubCommand {

	public ReplayPlayCommand(AbstractCommand parent) {
		super(parent, "play", "Starts a recorded replay", "play <Name>", true);
	}

	@Override
	public boolean execute(CommandSender cs, Command cmd, String label, String[] args) {
		if (args.length != 2) return false;
		
		String name = args[1];
		
		final Player p = (Player)cs;	
		
		if (ReplaySaver.exists(name) && !ReplayHelper.replaySessions.containsKey(p.getName())) {
			Messages.REPLAY_PLAY_LOAD.arg("replay", name).send(p);

			try {
				ReplaySaver.load(args[1], replay -> {
					Messages.REPLAY_PLAY.arg("duration", replay.getData().getDuration() / 20).send(p);
                    replay.play(p);
                });

			} catch (Exception e) {
				e.printStackTrace();
				
				Messages.REPLAY_PLAY_ERROR.arg("replay", name).send(p);
			}
		} else {
			Messages.REPLAY_NOT_FOUND.send(p);
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
