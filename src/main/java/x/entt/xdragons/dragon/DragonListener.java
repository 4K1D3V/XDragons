package x.entt.XDragons.dragon;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import x.entt.XDragons.Main;
import x.entt.XDragons.util.Messenger;


import java.util.List;
import java.util.Objects;

public class DragonListener implements Listener {
    private final Main plugin;
    private final Waypoints waypoints;

    public DragonListener(Main plugin) {
        this.plugin = plugin;
        this.waypoints = new Waypoints(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final boolean GUI_TITLE = event.getView().getTitle().equalsIgnoreCase("Dragon Editor");

        if (GUI_TITLE) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            DragonManager dragonManager = new DragonManager(plugin);

            if (!dragonManager.hasSelectedDragon(player)) {
                player.sendMessage(Messenger.color("&cNo dragon selected."));
                return;
            }

            int dragonID = dragonManager.getSelectedDragon(player);
            Dragon dragon = dragonManager.getDragon(dragonID);
            if (dragon == null) {
                player.sendMessage(Messenger.color("&cInvalid dragon ID."));
                return;
            }

            if (event.getCurrentItem() != null) {
                String displayName = Objects.requireNonNull(event.getCurrentItem().getItemMeta()).getDisplayName();
                if (displayName.equals("§aSet Waypoint")) {
                    addWaypointToDragon(player, dragonID);
                } else if (displayName.equals("§cRemove Last Waypoint")) {
                    removeLastWaypoint(player, dragonID);
                }
            }
        }
    }

    private void addWaypointToDragon(Player player, int dragonID) {
        waypoints.addWaypoint(dragonID, player.getLocation());
        player.sendMessage(Messenger.color("&aWaypoint added for Dragon ID " + dragonID + "."));
    }

    private void removeLastWaypoint(Player player, int dragonID) {
        List<Location> waypointsList = waypoints.getWaypoints(dragonID);
        if (waypointsList.isEmpty()) {
            player.sendMessage(Messenger.color("&cNo waypoints to remove."));
            return;
        }
        waypointsList.remove(waypointsList.size() - 1);
        waypoints.saveWaypoints(dragonID, waypointsList);
        player.sendMessage(Messenger.color("&cLast waypoint removed."));
    }
}