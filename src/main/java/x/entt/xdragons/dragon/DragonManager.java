package x.entt.XDragons.dragon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import x.entt.XDragons.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DragonManager {
    private final Main plugin;
    private final Map<Integer, Dragon> dragons;
    private final Map<UUID, Integer> selectedDragons;
    private BukkitRunnable movementTask;
    private BukkitRunnable abilityTask;

    public DragonManager(Main plugin) {
        this.plugin = plugin;
        this.dragons = new HashMap<>();
        this.selectedDragons = new HashMap<>();
        loadDragonsFromConfig();
        startTasks();
    }

    private void startTasks() {
        movementTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Dragon dragon : dragons.values()) {
                    dragon.updateMovement();
                }
            }
        };
        movementTask.runTaskTimer(plugin, 0L, 10L); // Every 0.5 seconds

        abilityTask = new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> nearbyPlayers = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    nearbyPlayers.add(player);
                }
                for (Dragon dragon : dragons.values()) {
                    dragon.updateAbilities(nearbyPlayers);
                }
            }
        };
        abilityTask.runTaskTimer(plugin, 0L, 40L); // Every 2 seconds
    }

    public void loadDragonsFromConfig() {
        dragons.clear();
        if (plugin.getFH().getDragons().contains("dragons")) {
            for (String idStr : Objects.requireNonNull(plugin.getFH().getDragons().getConfigurationSection("dragons")).getKeys(false)) {
                try {
                    int id = Integer.parseInt(idStr);
                    dragons.put(id, new Dragon(plugin, id));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid dragon ID in config: " + idStr);
                }
            }
        }
    }

    public Dragon getDragon(int id) {
        return dragons.get(id);
    }

    public void addDragon(int id, Dragon dragon) {
        dragons.put(id, dragon);
    }

    public void removeDragon(int id) {
        Dragon dragon = dragons.remove(id);
        if (dragon != null) {
            dragon.removeDragon();
            plugin.getFH().getDragons().set("dragons." + id, null);
            plugin.getFH().saveDragons();
        }
    }

    public Map<Integer, Dragon> getAllDragons() {
        return new HashMap<>(dragons);
    }

    public boolean hasSelectedDragon(Player player) {
        return selectedDragons.containsKey(player.getUniqueId());
    }

    public int getSelectedDragon(Player player) {
        return selectedDragons.getOrDefault(player.getUniqueId(), -1);
    }

    public void selectDragon(Player player, int dragonID) {
        selectedDragons.put(player.getUniqueId(), dragonID);
    }

    public void clearAllDragons() {
        for (Dragon dragon : dragons.values()) {
            dragon.removeDragon();
        }
        dragons.clear();
        selectedDragons.clear();
        if (movementTask != null) {
            movementTask.cancel();
            movementTask = null;
        }
        if (abilityTask != null) {
            abilityTask.cancel();
            abilityTask = null;
        }
    }
}