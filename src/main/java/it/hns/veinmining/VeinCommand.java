package it.hns.veinmining;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class VeinCommand implements CommandExecutor {
    VeinMining plugin;
    public VeinCommand(VeinMining instance) {
        plugin = instance;
        instance.getCommand("vein").setExecutor(this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // Se commandSender ha il permesso vein.reload e il secondo argomento è reload ricarico la config
        if (strings.length == 1 && strings[0].equals("reload") && commandSender.hasPermission("vein.reload")) {
            plugin.getConfigManager().reloadConfig();
            commandSender.sendMessage("§aConfig reloaded");
            return true;
        }
        ArrayList<Player> players = plugin.getPlayers();
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if(!player.hasPermission("vein.mining")) {
                players.remove(player);
                player.sendMessage("§cYou don't have permission to use this command");
                return true;
            }
            if (players.contains(player)) {
                players.remove(player);
                player.sendMessage("§cVeinMining disabled");
            } else {
                players.add(player);
                player.sendMessage("§aVeinMining enabled");
            }
        }
        else{
            commandSender.sendMessage("§cYou must be a player to use this command");
        }
        return true;
    }

}
