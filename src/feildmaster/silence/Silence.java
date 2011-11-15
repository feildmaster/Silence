package feildmaster.silence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Silence extends JavaPlugin {
    private static final List<String> silenceChat = new ArrayList<String>();
    private boolean server_wide = false;
    private String server_message = "All chat is currently being silenced";
    private String player_message = "Chat is silenced, use /silence to toggle";

    public void onDisable() {
        getServer().getLogger().info(format("Disabled!"));
    }

    public void onEnable() {
        PlayerListener listener = new PlayerListener() {
            public void onPlayerChat(PlayerChatEvent event) {
                if(event.isCancelled()) return;

                if(server_wide) {
                    if(event.getPlayer().hasPermission("silence.bypass")) return;

                    event.setCancelled(true);
                    event.getPlayer().sendMessage(server_message);

                    return;
                }

                if(silenceChat.contains(event.getPlayer().getName())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(format(player_message));
                    return;
                }

                for(Player p : new HashSet<Player>(event.getRecipients()))
                    if(silenceChat.contains(p.getName()))
                        event.getRecipients().remove(p);
            }

            public void onPlayerJoin(PlayerJoinEvent event) {
                if(server_wide) {
                    event.getPlayer().sendMessage(format(server_message));
                    //if(event.getPlayer().hasPermission("silence.bypass")) event.getPlayer().sendMessage("You are able to talk");
                } else if (silenceChat.contains(event.getPlayer().getName())) {
                    event.getPlayer().sendMessage(format(player_message));
                    //if(event.getPlayer().hasPermission("silence.bypass")) event.getPlayer().sendMessage("You are able to talk");
                }
            }

            public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
                if(server_wide && event.getMessage().startsWith("/me") && !event.getPlayer().hasPermission("silence.bypass")) {
                    event.getPlayer().sendMessage(format(server_message));
                    event.setCancelled(true);
                }
            }
        };

        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, listener, Event.Priority.Low, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, listener, Event.Priority.Low, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, listener, Event.Priority.Low, this);

        getServer().getLogger().info(format("v"+getDescription().getVersion()+" Enabled!"));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equals("silence-all")) {
            if(!sender.hasPermission("silence.admin")) return true;

            StringBuilder message = new StringBuilder();

            if(args.length > 0) {
                for(String s : args) message.append(s).append(" ");

                message.deleteCharAt(message.length()-1);
            }

            if(server_wide) {
                if(message.length() == 0) message.append("Chat no longer silenced.");
                server_wide = false;
                getServer().broadcastMessage(format(message.toString()));
            } else {
                if(message.length() == 0) message.append("All chat is currently being silenced");
                server_wide = true;
                getServer().broadcastMessage(format(server_message = message.toString()));
            }

            return true;
        }

        if(!(sender instanceof Player)) return true;

        Player player = (Player)sender;

        if(silenceChat.contains(player.getName())) {
            silenceChat.remove(player.getName());
            player.sendMessage(format("Now listening to chat"));
        } else {
            silenceChat.add(player.getName());
            player.sendMessage(format("Chat will be silenced"));
        }

        return true;
    }

    public String format(String message) {
        return String.format("[%1$s] %2$s", getDescription().getName(), message);
    }
}