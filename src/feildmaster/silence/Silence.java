package feildmaster.silence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Silence extends JavaPlugin {
    private List<String> silenceChat;

    public void onDisable() {
        getServer().getLogger().info(format("Disabled!"));
    }

    public void onEnable() {
        silenceChat = new ArrayList<String>();

        chatListener cl = new chatListener();
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, cl, Event.Priority.Low, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, cl, Event.Priority.Low, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_KICK, cl, Event.Priority.Low, this);

        getServer().getLogger().info(format("v"+getDescription().getVersion()+" Enabled!"));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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

    class chatListener extends PlayerListener {
        public void onPlayerChat(PlayerChatEvent event) {
            if(event.isCancelled()) return;
            if(silenceChat.contains(event.getPlayer().getName())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(format("Chat is silenced, use /silence to toggle"));
                return;
            }
            for(Player p : new HashSet<Player>(event.getRecipients()))
                if(silenceChat.contains(p.getName()))
                    event.getRecipients().remove(p);
        }

        public void onPlayerKick(PlayerKickEvent event) {
            if(silenceChat.contains(event.getPlayer().getName()))
                silenceChat.remove(event.getPlayer().getName());
        }

        public void onPlayerQuit(PlayerQuitEvent event) {
            if(silenceChat.contains(event.getPlayer().getName()))
                silenceChat.remove(event.getPlayer().getName());
        }
    }
}
