package x.entt.XDragons.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import x.entt.XDragons.Main;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final Main plugin;
    private FileConfiguration configFileConfig = null;
    private final File configFile;
    private FileConfiguration dragonsData = null;
    private final File dragonsFile;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.dragonsFile = new File(plugin.getDataFolder(), "dragons.yml");
        registerDefaults();
    }

    private void registerDefaults() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        configFileConfig = YamlConfiguration.loadConfiguration(configFile);

        if (!dragonsFile.exists()) {
            plugin.saveResource("dragons.yml", false);
        }
        dragonsData = YamlConfiguration.loadConfiguration(dragonsFile);
    }

    public void loadConfig() {
        configFileConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfig() {
        loadConfig();
    }

    public FileConfiguration getConfig() {
        return configFileConfig == null ? YamlConfiguration.loadConfiguration(configFile) : configFileConfig;
    }

    public void loadDragons() {
        dragonsData = YamlConfiguration.loadConfiguration(dragonsFile);
    }

    public void reloadDragons() {
        loadDragons();
    }

    public FileConfiguration getDragons() {
        return dragonsData == null ? YamlConfiguration.loadConfiguration(dragonsFile) : dragonsData;
    }

    public void saveDragons() {
        if (dragonsData == null) {
            loadDragons();
        }
        try {
            dragonsData.save(dragonsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save dragons.yml: " + e.getMessage());
        }
    }
}