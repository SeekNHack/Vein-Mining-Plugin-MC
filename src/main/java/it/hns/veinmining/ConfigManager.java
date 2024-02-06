package it.hns.veinmining;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigManager {

    private VeinMining plugin;
    private FileConfiguration config;
    private HashMap<String,FileConfiguration> weaponConfigs;


    public ConfigManager(VeinMining instance) throws IOException {
        plugin = instance;
        config = plugin.getConfig();
        plugin.saveDefaultConfig();
        weaponConfigs = new HashMap<String,FileConfiguration>();
        // Se i file delle armi non esistono li creo caricandoli dai file di default, altrimenti li carico
        for (String weapon : config.getStringList("weapons")) {
            File weaponFile = new File(plugin.getDataFolder(), weapon + ".yml");
            if (!weaponFile.exists()) {
                plugin.saveResource(weapon + ".yml", false);
            }
            FileConfiguration weaponConfig = YamlConfiguration.loadConfiguration(weaponFile);
            weaponConfigs.put(weapon, weaponConfig);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
    public HashMap<String,FileConfiguration> getWeaponConfigs() {
        return weaponConfigs;
    }
    public ArrayList<String> getWeapons() {
        return new ArrayList<String>(weaponConfigs.keySet());
    }
    public FileConfiguration getWeaponConfig(String weapon) {
        return weaponConfigs.get(weapon);
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        for (String weapon : weaponConfigs.keySet()) {
            File weaponFile = new File(plugin.getDataFolder(), weapon + ".yml");
            FileConfiguration weaponConfig = YamlConfiguration.loadConfiguration(weaponFile);
            weaponConfigs.put(weapon, weaponConfig);
        }
    }
}
