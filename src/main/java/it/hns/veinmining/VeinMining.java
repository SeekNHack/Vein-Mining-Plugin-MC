package it.hns.veinmining;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class VeinMining extends JavaPlugin {
    private ArrayList<Player> players;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Create the config
        try {
            this.configManager = new ConfigManager(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Register the listener
        getServer().getPluginManager().registerEvents(new BreakListener(this), this);
        // Register the command
        new VeinCommand(this);
        players = new ArrayList<Player>();
        // Avvio il tab completer
        getCommand("vein").setTabCompleter(new PluginTabCompleter());
        // Se è presente bStats lo avvio
        Metrics metrics = new Metrics(this, 20943);
    }
    public ArrayList<Player> getPlayers() {
        return players;
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
