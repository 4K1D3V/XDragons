package x.entt.XDragons;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import x.entt.XDragons.command.XDragonCommand;
import x.entt.XDragons.dragon.DragonListener;
import x.entt.XDragons.dragon.DragonManager;
import x.entt.XDragons.util.ConfigManager;
import x.entt.XDragons.util.Messenger;


import java.util.Objects;

public class Main extends JavaPlugin {
    private ConfigManager fh;
    private DragonManager dragonManager;
    public String prefix;

    @Override
    public void onEnable() {
        fh = new ConfigManager(this);
        dragonManager = new DragonManager(this);
        prefix = fh.getConfig().getString("prefix", "&4&l[XDragons] &f");

        registerCommands();
        registerEvents();

        Bukkit.getLogger().info(Messenger.color(prefix + "&av" + getDescription().getVersion() + " &2Enabled!"));
    }

    @Override
    public void onDisable() {
        dragonManager.clearAllDragons();
        Bukkit.getLogger().info(Messenger.color(prefix + "&cDisabled"));
    }

    private void registerCommands() {
        XDragonCommand cmd = new XDragonCommand(this);
        Objects.requireNonNull(getCommand("xdragons")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("xdragons")).setTabCompleter(cmd);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new DragonListener(this), this);
    }

    public ConfigManager getFH() {
        return fh;
    }

    public DragonManager getDragonManager() {
        return dragonManager;
    }
}