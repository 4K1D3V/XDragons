package x.entt.XDragons.dragon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import x.entt.XDragons.Main;

import java.util.ArrayList;
import java.util.List;

public class Waypoints {
    private final Main plugin;

    public Waypoints(Main plugin) {
        this.plugin = plugin;
    }

    public Location getWaypoint(int index, int dragonID) {
        List<Location> waypoints = getWaypoints(dragonID);
        if (index >= 0 && index < waypoints.size()) {
            return waypoints.get(index);
        }
        return null;
    }

    public void setWaypoint(int index, int dragonID, Location location) {
        if (location == null || location.getWorld() == null) return;
        List<Location> waypoints = getWaypoints(dragonID);
        if (index >= 0) {
            while (waypoints.size() <= index) {
                waypoints.add(null);
            }
            waypoints.set(index, location);
            saveWaypoints(dragonID, waypoints);
        }
    }

    public void addWaypoint(int dragonID, Location location) {
        if (location == null || location.getWorld() == null) return;
        List<Location> waypoints = getWaypoints(dragonID);
        waypoints.add(location);
        saveWaypoints(dragonID, waypoints);
    }

    public void removeWaypoint(int index, int dragonID) {
        List<Location> waypoints = getWaypoints(dragonID);
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
            saveWaypoints(dragonID, waypoints);
        }
    }

    public boolean waypointExists(int index, int dragonID) {
        List<Location> waypoints = getWaypoints(dragonID);
        return index >= 0 && index < waypoints.size() && waypoints.get(index) != null;
    }

    public int getWaypointCount(int dragonID) {
        return getWaypoints(dragonID).size();
    }

    public List<Location> getWaypoints(int dragonID) {
        List<Location> waypoints = new ArrayList<>();
        List<String> waypointStrings = plugin.getFH().getDragons().getStringList("dragons." + dragonID + ".waypoints");
        for (String wp : waypointStrings) {
            String[] parts = wp.split(",");
            if (parts.length == 4) {
                World world = Bukkit.getWorld(parts[0].trim());
                if (world != null) {
                    try {
                        double x = Double.parseDouble(parts[1].trim());
                        double y = Double.parseDouble(parts[2].trim());
                        double z = Double.parseDouble(parts[3].trim());
                        waypoints.add(new Location(world, x, y, z));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid waypoint format for dragon " + dragonID + ": " + wp);
                    }
                }
            }
        }
        return waypoints;
    }

    public void saveWaypoints(int dragonID, List<Location> waypoints) {
        List<String> waypointStrings = new ArrayList<>();
        for (Location loc : waypoints) {
            if (loc != null && loc.getWorld() != null) {
                waypointStrings.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ());
            }
        }
        plugin.getFH().getDragons().set("dragons." + dragonID + ".waypoints", waypointStrings);
        plugin.getFH().saveDragons();
    }
}