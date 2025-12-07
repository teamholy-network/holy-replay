package de.teamholy.replay.command;

import de.teamholy.replay.api.ReplayAPI;
import de.teamholy.replay.replaysystem.Replay;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple test commands (refactored):
 * /replay start [replayId] - start recording for all online players (all worlds)
 * /replay end <replayId>   - stop recording and optionally save
 * /replay play <replayId>  - play a replay for the player who ran the command
 */
public class SimpleReplayCommand implements CommandExecutor {

    private final ReplayAPI api = ReplayAPI.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("replay")) return false;

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start": {
                // /replay start [id]
                String id = null;
                if (args.length > 1) id = args[1];

                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                Replay replay = api.startRecording(id, players);
                sender.sendMessage("[Replay] Started recording with id: " + replay.getId());
                return true;
            }

            case "end": {
                // /replay end <id>
                if (args.length < 2) {
                    sender.sendMessage("Usage: /replay end <replayId>");
                    return true;
                }
                String id = args[1];
                api.stopRecording(id, true);
                sender.sendMessage("[Replay] Stopped recording '" + id + "' (saved)");
                return true;
            }

            case "play": {
                // /replay play <id>
                if (args.length < 2) {
                    sender.sendMessage("Usage: /replay play <replayId>");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                Player player = (Player) sender;
                String replayId = args[1];

                api.playReplay(replayId, player).whenComplete((opt, t) -> {
                    if (t != null) {
                        player.sendMessage("Error while loading replay: " + t.getMessage());
                        return;
                    }
                    if (opt.isPresent()) {
                        player.sendMessage("[Replay] Playing replay " + replayId);
                    } else {
                        player.sendMessage("[Replay] Replay not found: " + replayId);
                    }
                });
                return true;
            }

            default:
                sendUsage(sender);
                return true;
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("/replay start [id] - start recording for all online players");
        sender.sendMessage("/replay end <id>   - stop recording and save");
        sender.sendMessage("/replay play <id>  - play a replay (player only)");
    }
}
