package nonemc.sethome;

import nonemc.sethome.commands.HomeGuiCommand;
import nonemc.sethome.listeners.InventoryClickListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetHomePlugin extends JavaPlugin {
    private SetHomeManager homeManager;
    private final Map<UUID, Boolean> showDetailsState = new HashMap<>();
    private int teleportDelaySeconds;
    private boolean showDetailsByDefault;
    private boolean showTeleportActionBar;
    private String teleportActionBarFormat;
    private boolean cancelTeleportOnDamage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();

        homeManager = new SetHomeManager(this);
        homeManager.loadHomes();

        getCommand("sethome").setExecutor(new HomeGuiCommand(this));
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
    }

    private void loadConfigValues() {
        FileConfiguration config = getConfig();
        teleportDelaySeconds = config.getInt("teleport-delay-seconds", 3);
        showDetailsByDefault = config.getBoolean("show-details-by-default", true);
        showTeleportActionBar = config.getBoolean("show-action-bar", true);
        teleportActionBarFormat = config.getString("teleport-action-bar-format", "&eDịch chuyển về sethome {seconds} giây...");
        cancelTeleportOnDamage = config.getBoolean("cancel-teleport-on-damage", true);
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.saveHomes();
        }
    }

    public SetHomeManager getHomeManager() {
        return homeManager;
    }

    public boolean getShowDetails(UUID playerUuid) {
        return showDetailsState.getOrDefault(playerUuid, showDetailsByDefault);
    }

    public boolean toggleShowDetails(UUID playerUuid) {
        boolean current = getShowDetails(playerUuid);
        showDetailsState.put(playerUuid, !current);
        return !current;
    }

    public int getTeleportDelaySeconds() {
        return teleportDelaySeconds;
    }

    public boolean isShowTeleportActionBar() {
        return showTeleportActionBar;
    }

    public String getTeleportActionBarFormat() {
        return teleportActionBarFormat;
    }

    public boolean isCancelTeleportOnDamage() {
        return cancelTeleportOnDamage;
    }
}
