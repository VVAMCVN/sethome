package nonemc.sethome;

import nonemc.sethome.commands.HomeGuiCommand;
import nonemc.sethome.listeners.InventoryClickListener;
import org.bukkit.plugin.java.JavaPlugin;

public class SetHomePlugin extends JavaPlugin {
    private SetHomeManager homeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        homeManager = new SetHomeManager(this);
        homeManager.loadHomes();

        getCommand("sethome").setExecutor(new HomeGuiCommand(this));
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
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
}
