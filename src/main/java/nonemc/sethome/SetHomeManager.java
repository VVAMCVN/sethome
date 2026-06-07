package nonemc.sethome;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SetHomeManager {
    private final SetHomePlugin plugin;
    private final File homeFile;
    private FileConfiguration homeConfig;
    private final Map<String, Location[]> playerHomes = new HashMap<>();

    public SetHomeManager(SetHomePlugin plugin) {
        this.plugin = plugin;
        this.homeFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homeFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                homeFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create homes.yml: " + e.getMessage());
            }
        }
        homeConfig = YamlConfiguration.loadConfiguration(homeFile);
    }

    public void loadHomes() {
        homeConfig = YamlConfiguration.loadConfiguration(homeFile);
        playerHomes.clear();

        for (String uuid : homeConfig.getKeys(false)) {
            Location[] homes = new Location[5];
            for (int i = 0; i < 5; i++) {
                String path = uuid + ".home" + (i + 1);
                if (homeConfig.contains(path + ".world")) {
                    homes[i] = deserializeLocation(path);
                }
            }
            playerHomes.put(uuid, homes);
        }
    }

    public void saveHomes() {
        for (Map.Entry<String, Location[]> entry : playerHomes.entrySet()) {
            String uuid = entry.getKey();
            Location[] homes = entry.getValue();
            for (int i = 0; i < homes.length; i++) {
                String path = uuid + ".home" + (i + 1);
                if (homes[i] != null) {
                    serializeLocation(path, homes[i]);
                } else {
                    homeConfig.set(path, null);
                }
            }
        }

        try {
            homeConfig.save(homeFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save sethome data: " + e.getMessage());
        }
    }

    public Location getHome(Player player, int slot) {
        Location[] homes = playerHomes.get(player.getUniqueId().toString());
        return homes != null && slot >= 0 && slot < homes.length ? homes[slot] : null;
    }

    public void setHome(Player player, int slot, Location location) {
        playerHomes.computeIfAbsent(player.getUniqueId().toString(), k -> new Location[5])[slot] = location;
    }

    public void removeHome(Player player, int slot) {
        Location[] homes = playerHomes.get(player.getUniqueId().toString());
        if (homes != null && slot >= 0 && slot < homes.length) {
            homes[slot] = null;
        }
    }

    public boolean hasHome(Player player, int slot) {
        return getHome(player, slot) != null;
    }

    private void serializeLocation(String path, Location location) {
        homeConfig.set(path + ".world", location.getWorld().getName());
        homeConfig.set(path + ".x", location.getX());
        homeConfig.set(path + ".y", location.getY());
        homeConfig.set(path + ".z", location.getZ());
        homeConfig.set(path + ".yaw", location.getYaw());
        homeConfig.set(path + ".pitch", location.getPitch());
    }

    private Location deserializeLocation(String path) {
        String worldName = homeConfig.getString(path + ".world");
        if (worldName == null) {
            return null;
        }
        return new Location(
                Bukkit.getWorld(worldName),
                homeConfig.getDouble(path + ".x"),
                homeConfig.getDouble(path + ".y"),
                homeConfig.getDouble(path + ".z"),
                (float) homeConfig.getDouble(path + ".yaw"),
                (float) homeConfig.getDouble(path + ".pitch")
        );
    }
}
