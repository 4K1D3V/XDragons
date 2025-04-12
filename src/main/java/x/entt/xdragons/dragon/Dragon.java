package x.entt.XDragons.dragon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import x.entt.XDragons.Main;
import x.entt.XDragons.util.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Dragon {
    private final Main plugin;
    private final int id;
    private String name;
    private double maxHealth;
    private boolean showBossBar;
    private UUID entityId;
    private BossBar bossBar;
    private EnderDragon dragonEntity;
    private int currentWaypointIndex;
    private BukkitTask bossBarTask;
    private final Random random;
    private final Waypoints waypointsManager;
    private double movementSpeed;
    private double movementThreshold;
    private double attackChance;
    private double attackRange;
    private double attackDamage;
    private double fireballChance;
    private double fireballRange;
    private double thunderboltChance;
    private double thunderboltRange;
    private double rushChance;
    private double rushRange;
    private double rushDistance;
    private List<Phase> phases;

    public Dragon(Main plugin, int id) {
        this.plugin = plugin;
        this.id = id;
        this.currentWaypointIndex = 0;
        this.random = new Random();
        this.waypointsManager = new Waypoints(plugin);
        this.phases = new ArrayList<>();
        loadFromConfig();
    }

    private void loadFromConfig() {
        ConfigManager fh = plugin.getFH();
        FileConfiguration dragons = fh.getDragons();

        if (!dragons.contains("dragons." + id)) return;

        this.name = dragons.getString("dragons." + id + ".name", "Unnamed Dragon");
        this.maxHealth = dragons.getDouble("dragons." + id + ".max-health", 200.0);
        this.showBossBar = dragons.getBoolean("dragons." + id + ".show-bossbar", true);
        this.entityId = dragons.contains("dragons." + id + ".uuid")
                ? UUID.fromString(Objects.requireNonNull(dragons.getString("dragons." + id + ".uuid")))
                : null;

        this.movementSpeed = dragons.getDouble("dragons." + id + ".movement.speed", 0.8);
        this.movementThreshold = dragons.getDouble("dragons." + id + ".movement.threshold", 3.0);
        this.attackChance = dragons.getDouble("dragons." + id + ".attack.chance", 0.03);
        this.attackRange = dragons.getDouble("dragons." + id + ".attack.range", 5.0);
        this.attackDamage = dragons.getDouble("dragons." + id + ".attack.damage", 0.0);
        this.fireballChance = dragons.getDouble("dragons." + id + ".abilities.fireball.chance", 0.05);
        this.fireballRange = dragons.getDouble("dragons." + id + ".abilities.fireball.range", 30.0);
        this.thunderboltChance = dragons.getDouble("dragons." + id + ".abilities.thunderbolt.chance", 0.03);
        this.thunderboltRange = dragons.getDouble("dragons." + id + ".abilities.thunderbolt.range", 20.0);
        this.rushChance = dragons.getDouble("dragons." + id + ".abilities.rush.chance", 0.03);
        this.rushRange = dragons.getDouble("dragons." + id + ".abilities.rush.range", 20.0);
        this.rushDistance = dragons.getDouble("dragons." + id + ".abilities.rush.distance", 10.0);

        phases.clear();
        if (dragons.contains("dragons." + id + ".phases")) {
            for (String phaseKey : Objects.requireNonNull(dragons.getConfigurationSection("dragons." + id + ".phases")).getKeys(false)) {
                double healthThreshold = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".health", -1);
                if (healthThreshold >= 0) {
                    Phase phase = new Phase();
                    phase.healthThreshold = healthThreshold;
                    phase.fireballChance = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".abilities.fireball.chance", fireballChance);
                    phase.fireballRange = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".abilities.fireball.range", fireballRange);
                    phase.thunderboltChance = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".abilities.thunderbolt.chance", thunderboltChance);
                    phase.thunderboltRange = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".abilities.thunderbolt.range", thunderboltRange);
                    phase.rushChance = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".abilities.rush.chance", rushChance);
                    phase.rushRange = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".abilities.rush.range", rushRange);
                    phase.rushDistance = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".abilities.rush.distance", rushDistance);
                    phase.attackChance = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".attack.chance", attackChance);
                    phase.attackRange = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".attack.range", attackRange);
                    phase.attackDamage = dragons.getDouble("dragons." + id + ".phases." + phaseKey + ".attack.damage", attackDamage);
                    phases.add(phase);
                }
            }
            phases.sort((p1, p2) -> Double.compare(p2.healthThreshold, p1.healthThreshold)); // High to low
        }
    }

    public void saveToConfig() {
        ConfigManager fh = plugin.getFH();
        FileConfiguration dragons = fh.getDragons();

        dragons.set("dragons." + id + ".name", name);
        dragons.set("dragons." + id + ".max-health", maxHealth);
        dragons.set("dragons." + id + ".show-bossbar", showBossBar);
        dragons.set("dragons." + id + ".uuid", entityId != null ? entityId.toString() : null);
        fh.saveDragons();
    }

    public void spawnDragon(Location location) {
        if (location == null || location.getWorld() == null) {
            plugin.getLogger().severe("Invalid location to spawn dragon: " + location);
            return;
        }

        removeDragon();

        Entity spawnedEntity = location.getWorld().spawnEntity(location, EntityType.ENDER_DRAGON);
        if (!(spawnedEntity instanceof EnderDragon)) {
            plugin.getLogger().severe("Failed to spawn EnderDragon for ID " + id);
            return;
        }

        dragonEntity = (EnderDragon) spawnedEntity;
        dragonEntity.setHealth(Math.min(maxHealth, dragonEntity.getMaxHealth()));
        dragonEntity.setCustomName(null); // No visible name
        entityId = dragonEntity.getUniqueId();

        if (showBossBar) {
            bossBar = Bukkit.createBossBar(name, BarColor.RED, BarStyle.SOLID);
            bossBar.setVisible(true);
            updateBossBarPlayers();
            startBossBarTask();
        }

        plugin.getLogger().info("Spawned dragon ID " + id + " at " + location);
    }

    public void updateMovement() {
        if (dragonEntity == null || dragonEntity.isDead()) return;

        List<Location> waypoints = waypointsManager.getWaypoints(id);
        if (waypoints.isEmpty()) return;

        Location target = waypoints.get(currentWaypointIndex);
        if (target == null || target.getWorld() == null || !target.getWorld().equals(dragonEntity.getWorld())) {
            plugin.getLogger().warning("Invalid waypoint for dragon " + id + " at index " + currentWaypointIndex);
            return;
        }

        Location current = dragonEntity.getLocation();
        Vector direction = target.toVector().subtract(current.toVector());
        double distance = direction.length();

        if (distance < movementThreshold) {
            currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size();
            return;
        }

        direction.normalize().multiply(movementSpeed);
        dragonEntity.setVelocity(direction);
    }

    public void updateAbilities(List<Player> nearbyPlayers) {
        if (dragonEntity == null || dragonEntity.isDead()) return;

        Phase currentPhase = getCurrentPhase();
        double fbChance = currentPhase != null ? currentPhase.fireballChance : fireballChance;
        double fbRange = currentPhase != null ? currentPhase.fireballRange : fireballRange;
        double tbChance = currentPhase != null ? currentPhase.thunderboltChance : thunderboltChance;
        double tbRange = currentPhase != null ? currentPhase.thunderboltRange : thunderboltRange;
        double rChance = currentPhase != null ? currentPhase.rushChance : rushChance;
        double rRange = currentPhase != null ? currentPhase.rushRange : rushRange;
        double rDistance = currentPhase != null ? currentPhase.rushDistance : rushDistance;
        double aChance = currentPhase != null ? currentPhase.attackChance : attackChance;
        double aRange = currentPhase != null ? currentPhase.attackRange : attackRange;
        double aDamage = currentPhase != null ? currentPhase.attackDamage : attackDamage;

        if (random.nextDouble() < fbChance) {
            Player target = getRandomPlayer(fbRange, nearbyPlayers);
            if (target != null) {
                launchFireball(target);
            }
        }

        if (random.nextDouble() < tbChance) {
            Player target = getRandomPlayer(tbRange, nearbyPlayers);
            if (target != null) {
                launchThunderbolt(target);
            }
        }

        if (random.nextDouble() < rChance) {
            Player target = getRandomPlayer(rRange, nearbyPlayers);
            if (target != null) {
                launchRush(target, rDistance);
            }
        }

        if (random.nextDouble() < aChance) {
            Player target = getRandomPlayer(aRange, nearbyPlayers);
            if (target != null && aDamage > 0) {
                launchAttack(target, aDamage);
            }
        }
    }

    private Phase getCurrentPhase() {
        if (dragonEntity == null) return null;
        double currentHealth = dragonEntity.getHealth();
        for (Phase phase : phases) {
            if (currentHealth >= phase.healthThreshold) {
                return phase;
            }
        }
        return null;
    }

    private void launchFireball(Player target) {
        if (dragonEntity == null || target == null) return;
        Location loc = dragonEntity.getLocation().add(0, 2, 0);
        Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(loc.toVector()).normalize();
        Fireball fireball = (Fireball) loc.getWorld().spawnEntity(loc, EntityType.FIREBALL);
        fireball.setDirection(direction);
        fireball.setYield(2.0f);
        fireball.setIsIncendiary(true);
    }

    private void launchThunderbolt(Player target) {
        if (dragonEntity == null || target == null) return;
        Location loc = target.getLocation();
        loc.getWorld().strikeLightning(loc);
    }

    private void launchRush(Player target, double distance) {
        if (dragonEntity == null || target == null) return;
        Location targetLoc = target.getLocation();
        Location dragonLoc = dragonEntity.getLocation();
        Vector direction = targetLoc.toVector().subtract(dragonLoc.toVector()).normalize().multiply(distance);
        Location newLoc = dragonLoc.add(direction);
        if (newLoc.getWorld() != null && newLoc.getY() > 0) {
            dragonEntity.teleport(newLoc);
            plugin.getLogger().info("Dragon " + id + " rushed toward " + target.getName());
        }
    }

    private void launchAttack(Player target, double damage) {
        if (dragonEntity == null || target == null) return;
        target.damage(damage, dragonEntity);
        plugin.getLogger().info("Dragon " + id + " attacked " + target.getName() + " for " + damage + " damage");
    }

    private Player getRandomPlayer(double range, List<Player> nearbyPlayers) {
        if (dragonEntity == null) return null;
        List<Player> players = new ArrayList<>();
        for (Player player : nearbyPlayers) {
            if (player.getWorld().equals(dragonEntity.getWorld())
                    && player.getLocation().distance(dragonEntity.getLocation()) <= range) {
                players.add(player);
            }
        }
        return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
    }

    private void startBossBarTask() {
        if (bossBarTask != null) {
            bossBarTask.cancel();
        }
        bossBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (bossBar == null || dragonEntity == null || dragonEntity.isDead()) {
                    cancel();
                    return;
                }
                bossBar.setProgress(Math.max(0, dragonEntity.getHealth() / maxHealth));
                updateBossBarPlayers();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateBossBarPlayers() {
        if (bossBar == null || dragonEntity == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(dragonEntity.getWorld())
                    && player.getLocation().distance(dragonEntity.getLocation()) <= 100) {
                bossBar.addPlayer(player);
            } else {
                bossBar.removePlayer(player);
            }
        }
    }

    public void addWaypoint(Location location) {
        waypointsManager.addWaypoint(id, location);
    }

    public void clearWaypoints() {
        waypointsManager.saveWaypoints(id, new ArrayList<>());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (bossBar != null) {
            bossBar.setTitle(name);
        }
    }

    public boolean isShowBossBar() {
        return showBossBar;
    }

    public void setShowBossBar(boolean showBossBar) {
        this.showBossBar = showBossBar;
        if (bossBar != null) {
            bossBar.setVisible(showBossBar);
            if (showBossBar) {
                updateBossBarPlayers();
                startBossBarTask();
            } else {
                bossBar.removeAll();
            }
        }
    }

    public List<Location> getWaypoints() {
        return waypointsManager.getWaypoints(id);
    }

    public void setWaypoints(List<Location> waypoints) {
        waypointsManager.saveWaypoints(id, waypoints);
    }

    public EnderDragon getDragonEntity() {
        return dragonEntity;
    }

    public void removeDragon() {
        if (bossBarTask != null) {
            bossBarTask.cancel();
            bossBarTask = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (dragonEntity != null) {
            dragonEntity.remove();
            dragonEntity = null;
        }
        entityId = null;
        saveToConfig();
    }

    public void setMaxHealth(double health) {
        this.maxHealth = health;
        if (dragonEntity != null) {
            dragonEntity.setHealth(Math.min(health, dragonEntity.getMaxHealth()));
        }
    }

    private static class Phase {
        double healthThreshold;
        double fireballChance;
        double fireballRange;
        double thunderboltChance;
        double thunderboltRange;
        double rushChance;
        double rushRange;
        double rushDistance;
        double attackChance;
        double attackRange;
        double attackDamage;
    }
}