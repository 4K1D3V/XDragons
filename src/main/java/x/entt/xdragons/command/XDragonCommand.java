package x.entt.XDragons.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import x.entt.XDragons.Main;
import x.entt.XDragons.dragon.Dragon;
import x.entt.XDragons.dragon.DragonManager;
import x.entt.XDragons.dragon.Waypoints;
import x.entt.XDragons.util.Messenger;

import java.util.ArrayList;
import java.util.List;

public class XDragonCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final DragonManager dragonManager;
    private final Waypoints waypoints;
    private final String prefix = "&8[&cXDragons&8] ";

    public XDragonCommand(Main plugin) {
        this.plugin = plugin;
        this.dragonManager = plugin.getDragonManager();
        this.waypoints = new Waypoints(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messenger.color(prefix + "&cThis command can only be used by players."));
            return false;
        }

        if (!cmd.getName().equalsIgnoreCase("xdragons")) return true;

        if (args.length < 1) {
            player.sendMessage(Messenger.color(prefix + "&cUsage: /xdragons <create | edit | delete | spawn | gui | select | reload> [ID] [options]"));
            return true;
        }

        String action = args[0].toLowerCase();
        if (action.equals("reload") && args.length == 1) {
            if (!player.hasPermission("xdragons.admin")) {
                player.sendMessage(Messenger.color(prefix + "&cNo permission."));
                return false;
            }
            plugin.getFH().reloadConfig();
            plugin.getFH().reloadDragons();
            dragonManager.loadDragonsFromConfig();
            player.sendMessage(Messenger.color(prefix + "&2Plugin Reloaded!"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Messenger.color(prefix + "&cUsage: /xdragons <create | edit | delete | spawn | gui | select> <ID> [options]"));
            return true;
        }

        int dragonID;
        try {
            dragonID = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Messenger.color(prefix + "&cInvalid dragon ID."));
            return false;
        }

        switch (action) {
            case "create" -> {
                if (!player.hasPermission("xdragons.admin")) {
                    player.sendMessage(Messenger.color(prefix + "&cNo permission."));
                    return false;
                }
                createDragon(player, dragonID);
            }
            case "edit" -> {
                if (!player.hasPermission("xdragons.admin")) {
                    player.sendMessage(Messenger.color(prefix + "&cNo permission."));
                    return false;
                }
                editDragon(player, dragonID, args);
            }
            case "delete" -> {
                if (!player.hasPermission("xdragons.admin")) {
                    player.sendMessage(Messenger.color(prefix + "&cNo permission."));
                    return false;
                }
                deleteDragon(player, dragonID);
            }
            case "spawn" -> {
                if (!player.hasPermission("xdragons.spawn")) {
                    player.sendMessage(Messenger.color(prefix + "&cNo permission."));
                    return false;
                }
                spawnDragon(player, dragonID);
            }
            case "gui" -> {
                if (!player.hasPermission("xdragons.admin")) {
                    player.sendMessage(Messenger.color(prefix + "&cNo permission."));
                    return false;
                }
                openGui(player, dragonID);
            }
            case "select" -> {
                if (!player.hasPermission("xdragons.admin")) {
                    player.sendMessage(Messenger.color(prefix + "&cNo permission."));
                    return false;
                }
                selectDragon(player, dragonID);
            }
            default -> player.sendMessage(Messenger.color(prefix + "&cInvalid action. Use create, edit, delete, spawn, gui, select, or reload."));
        }
        return true;
    }

    private void createDragon(Player player, int dragonID) {
        if (plugin.getFH().getDragons().contains("dragons." + dragonID)) {
            player.sendMessage(Messenger.color(prefix + "&cDragon with ID " + dragonID + " already exists."));
            return;
        }

        Dragon dragon = new Dragon(plugin, dragonID);
        dragon.setName("New Dragon");
        dragon.setMaxHealth(100.0);
        dragon.setShowBossBar(true);
        dragon.saveToConfig();
        dragonManager.addDragon(dragonID, dragon);

        player.sendMessage(Messenger.color(prefix + "&aDragon with ID " + dragonID + " successfully created."));
    }

    private void editDragon(Player player, int dragonID, String[] args) {
        Dragon dragon = dragonManager.getDragon(dragonID);
        if (dragon == null) {
            player.sendMessage(Messenger.color(prefix + "&cDragon with ID " + dragonID + " does not exist."));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Messenger.color(prefix + "&cUsage: /xdragons edit <ID> <property> <value>"));
            return;
        }

        String property = args[2].toLowerCase();

        switch (property) {
            case "name" -> {
                if (args.length < 4) {
                    player.sendMessage(Messenger.color(prefix + "&cPlease specify a name."));
                    return;
                }
                dragon.setName(args[3]);
                player.sendMessage(Messenger.color(prefix + "&aDragon name updated."));
            }
            case "maxhealth" -> {
                if (args.length < 4) {
                    player.sendMessage(Messenger.color(prefix + "&cPlease specify a health value."));
                    return;
                }
                try {
                    double health = Double.parseDouble(args[3]);
                    dragon.setMaxHealth(health);
                    player.sendMessage(Messenger.color(prefix + "&aDragon max health updated."));
                } catch (NumberFormatException ex) {
                    player.sendMessage(Messenger.color(prefix + "&cInvalid max health value."));
                }
            }
            case "bossbar" -> {
                if (args.length < 4) {
                    player.sendMessage(Messenger.color(prefix + "&cPlease specify true or false."));
                    return;
                }
                boolean show = Boolean.parseBoolean(args[3]);
                dragon.setShowBossBar(show);
                player.sendMessage(Messenger.color(prefix + (show ? "&aBossBar enabled." : "&cBossBar disabled.")));
            }
            case "waypoints" -> {
                if (args.length < 4) {
                    player.sendMessage(Messenger.color(prefix + "&cUsage: /xdragons edit <ID> waypoints add|clear"));
                    return;
                }
                if (args[3].equalsIgnoreCase("add")) {
                    waypoints.addWaypoint(dragonID, player.getLocation());
                    player.sendMessage(Messenger.color(prefix + "&aWaypoint added at your location."));
                } else if (args[3].equalsIgnoreCase("clear")) {
                    waypoints.saveWaypoints(dragonID, new ArrayList<>());
                    player.sendMessage(Messenger.color(prefix + "&cAll waypoints cleared."));
                } else {
                    player.sendMessage(Messenger.color(prefix + "&cInvalid waypoint action. Use 'add' or 'clear'."));
                }
            }
            default -> player.sendMessage(Messenger.color(prefix + "&cInvalid property."));
        }
    }

    private void deleteDragon(Player player, int dragonID) {
        if (dragonManager.getDragon(dragonID) == null) {
            player.sendMessage(Messenger.color(prefix + "&cDragon with ID " + dragonID + " does not exist."));
            return;
        }
        dragonManager.removeDragon(dragonID);
        waypoints.saveWaypoints(dragonID, new ArrayList<>());
        player.sendMessage(Messenger.color(prefix + "&aDragon with ID " + dragonID + " has been deleted."));
    }

    private void spawnDragon(Player player, int dragonID) {
        Dragon dragon = dragonManager.getDragon(dragonID);
        if (dragon == null) {
            player.sendMessage(Messenger.color(prefix + "&cDragon with ID " + dragonID + " does not exist."));
            return;
        }

        dragon.spawnDragon(player.getLocation());
        player.sendMessage(Messenger.color(prefix + "&aYou have summoned Dragon with ID " + dragonID + " at your location."));
    }

    private void openGui(Player player, int dragonID) {
        if (!dragonManager.hasSelectedDragon(player) || dragonManager.getSelectedDragon(player) != dragonID) {
            player.sendMessage(Messenger.color(prefix + "&cPlease select dragon ID " + dragonID + " first with /xdragons select " + dragonID));
            return;
        }
        Dragon dragon = dragonManager.getDragon(dragonID);
        if (dragon == null) {
            player.sendMessage(Messenger.color(prefix + "&cDragon with ID " + dragonID + " does not exist."));
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 9, "Dragon Editor");
        ItemStack setWaypoint = new ItemStack(Material.EMERALD);
        ItemMeta setMeta = setWaypoint.getItemMeta();
        setMeta.setDisplayName("§aSet Waypoint");
        setWaypoint.setItemMeta(setMeta);

        ItemStack removeWaypoint = new ItemStack(Material.REDSTONE);
        ItemMeta removeMeta = removeWaypoint.getItemMeta();
        removeMeta.setDisplayName("§cRemove Last Waypoint");
        removeWaypoint.setItemMeta(removeMeta);

        gui.setItem(3, setWaypoint);
        gui.setItem(5, removeWaypoint);

        player.openInventory(gui);
    }

    private void selectDragon(Player player, int dragonID) {
        Dragon dragon = dragonManager.getDragon(dragonID);
        if (dragon == null) {
            player.sendMessage(Messenger.color(prefix + "&cDragon with ID " + dragonID + " does not exist."));
            return;
        }
        dragonManager.selectDragon(player, dragonID);
        player.sendMessage(Messenger.color(prefix + "&aSelected dragon ID " + dragonID + "."));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (!(sender instanceof Player)) return suggestions;

        if (args.length == 1) {
            suggestions.addAll(List.of("create", "edit", "delete", "spawn", "gui", "select", "reload"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("edit")) {
            suggestions.addAll(List.of("name", "maxhealth", "bossbar", "waypoints"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("waypoints")) {
            suggestions.addAll(List.of("add", "clear"));
        }
        return suggestions;
    }
}