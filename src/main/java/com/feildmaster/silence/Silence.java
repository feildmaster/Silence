package com.feildmaster.silence;

import java.util.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Silence extends JavaPlugin {
    public static boolean BLOCK_SLASH_ME = true;
    private final List<String> SILENCE_CHAT = new ArrayList<String>();
    private boolean server_wide = false;
    private String server_message = "All chat is currently being silenced";
    private String player_message = "Chat is silenced, use /silence to toggle";

    @Override
    public void onEnable() {
        Listener listener = new Listener() {
            @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
            public void onPlayerChat(PlayerChatEvent event) {
                if (server_wide) {
                    if (event.getPlayer().hasPermission("silence.bypass")) {
                        return;
                    }

                    event.setCancelled(true);
                    event.getPlayer().sendMessage(server_message);
                    return;
                }

                if (SILENCE_CHAT.contains(event.getPlayer().getName())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(format(player_message));
                    return;
                }

                for (Player p : new HashSet<Player>(event.getRecipients())) {
                    if (SILENCE_CHAT.contains(p.getName())) {
                        event.getRecipients().remove(p);
                    }
                }
            }

            @EventHandler(priority = EventPriority.LOW)
            public void onPlayerJoin(PlayerJoinEvent event) {
                if (server_wide) {
                    event.getPlayer().sendMessage(format(server_message));
                    //if(event.getPlayer().hasPermission("silence.bypass")) event.getPlayer().sendMessage("You are able to talk");
                } else if (SILENCE_CHAT.contains(event.getPlayer().getName())) {
                    event.getPlayer().sendMessage(format(player_message));
                    //if(event.getPlayer().hasPermission("silence.bypass")) event.getPlayer().sendMessage("You are able to talk");
                }
            }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
                if (server_wide && event.getMessage().startsWith("/me") && !event.getPlayer().hasPermission("silence.bypass")) {
                    event.getPlayer().sendMessage(format(server_message));
                    event.setCancelled(true);
                }
            }
        };


        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("silence-all")) {
            if (!sender.hasPermission("silence.admin")) {
                return true;
            }

            StringBuilder message = new StringBuilder();

            if (args.length > 0) {
                for (String s : args) {
                    message.append(s).append(" ");
                }

                message.deleteCharAt(message.length() - 1);
            }

            if (server_wide) {
                if (message.length() == 0) {
                    message.append("Chat no longer silenced.");
                }
                server_wide = false;
                getServer().broadcastMessage(format(message.toString()));
            } else {
                if (message.length() == 0) {
                    message.append("All chat is currently being silenced");
                }
                server_wide = true;
                getServer().broadcastMessage(format(server_message = message.toString()));
            }

            return true;
        }

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (SILENCE_CHAT.contains(player.getName())) {
            SILENCE_CHAT.remove(player.getName());
            player.sendMessage(format("Now listening to chat"));
        } else {
            SILENCE_CHAT.add(player.getName());
            player.sendMessage(format("Chat will be silenced"));
        }

        return true;
    }

    public String format(String message) {
        return String.format("[%1$s] %2$s", getDescription().getName(), message);
    }
}