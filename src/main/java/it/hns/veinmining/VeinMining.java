package it.hns.veinmining;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

public final class VeinMining extends JavaPlugin {
    private ArrayList<Player> players;
    private FileConfiguration config;
    @Override
    public void onEnable() {
        FileConfiguration config = this.getConfig();
        this.saveDefaultConfig();
        // Register the listener
        getServer().getPluginManager().registerEvents(new BreakListener(this), this);
        // Register the command
        new VeinCommand(this);
        players = new ArrayList<Player>();
    }
    public ArrayList<Player> getPlayers() {
        return players;
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
