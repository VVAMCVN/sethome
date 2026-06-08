package nonemc.sethome;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SetHomeManager {
    private final SetHomePlugin plugin;
    private final File homesFolder;
    private final Map<String, HomeEntry[]> playerHomes = new HashMap<>();

    private record HomeEntry(Location location, long timestamp) {}

    public SetHomeManager(SetHomePlugin plugin) {
        this.plugin = plugin;
        this.homesFolder = new File(plugin.getDataFolder(), "homes");
        if (!homesFolder.exists()) {
            homesFolder.mkdirs();
        }
    }

    public void loadHomes() {
        playerHomes.clear();

        if (!homesFolder.exists() || !homesFolder.isDirectory()) {
            return;
        }

        File[] files = homesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            String uuid = fileName.substring(0, fileName.length() - 4);
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            HomeEntry[] homes = new HomeEntry[5];
            for (int i = 0; i < 5; i++) {
                String path = "home" + (i + 1);
                if (config.contains(path + ".world")) {
                    homes[i] = deserializeHomeEntry(config, path);
                }
            }
            playerHomes.put(uuid, homes);
        }
    }

    public void saveHomes() {
        if (!homesFolder.exists()) {
            homesFolder.mkdirs();
        }

        for (Map.Entry<String, HomeEntry[]> entry : playerHomes.entrySet()) {
            String uuid = entry.getKey();
            HomeEntry[] homes = entry.getValue();
            File playerFile = new File(homesFolder, uuid + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            boolean hasAnyHome = false;

            if (homes != null) {
                for (int i = 0; i < homes.length; i++) {
                    String path = "home" + (i + 1);
                    HomeEntry homeEntry = homes[i];
                    if (homeEntry != null && homeEntry.location() != null) {
                        serializeHomeEntry(config, path, homeEntry);
                        hasAnyHome = true;
                    } else {
                        config.set(path, null);
                    }
                }
            }

            if (hasAnyHome) {
                try {
                    config.save(playerFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save sethome data for " + uuid + ": " + e.getMessage());
                }
            } else if (playerFile.exists()) {
                if (!playerFile.delete()) {
                    plugin.getLogger().warning("Could not delete empty home file for " + uuid);
                }
            }
        }
    }

    public Location getHome(org.bukkit.entity.Player player, int slot) {
        HomeEntry[] homes = playerHomes.get(player.getUniqueId().toString());
        return homes != null && slot >= 0 && slot < homes.length && homes[slot] != null ? homes[slot].location() : null;
    }

    public long getHomeTimestamp(org.bukkit.entity.Player player, int slot) {
        HomeEntry[] homes = playerHomes.get(player.getUniqueId().toString());
        return homes != null && slot >= 0 && slot < homes.length && homes[slot] != null ? homes[slot].timestamp() : 0L;
    }

    public void setHome(org.bukkit.entity.Player player, int slot, Location location) {
        HomeEntry[] homes = playerHomes.computeIfAbsent(player.getUniqueId().toString(), k -> new HomeEntry[5]);
        homes[slot] = new HomeEntry(location, System.currentTimeMillis());
    }

    public void removeHome(org.bukkit.entity.Player player, int slot) {
        HomeEntry[] homes = playerHomes.get(player.getUniqueId().toString());
        if (homes != null && slot >= 0 && slot < homes.length) {
            homes[slot] = null;
        }
    }

    public boolean hasHome(org.bukkit.entity.Player player, int slot) {
        return getHome(player, slot) != null;
    }

    private void serializeHomeEntry(FileConfiguration config, String path, HomeEntry entry) {
        Location location = entry.location();
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        config.set(path + ".timestamp", entry.timestamp());
    }

    private HomeEntry deserializeHomeEntry(FileConfiguration config, String path) {
        String worldName = config.getString(path + ".world");
        if (worldName == null) {
            return null;
        }
        Location location = new Location(
                Bukkit.getWorld(worldName),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
        long timestamp = config.getLong(path + ".timestamp", 0L);
        return new HomeEntry(location, timestamp);
    }
}
