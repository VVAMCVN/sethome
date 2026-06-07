package nonemc.sethome.listeners;

import nonemc.sethome.SetHomePlugin;
import nonemc.sethome.commands.HomeGuiCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryClickListener implements Listener {
    private final SetHomePlugin plugin;

    public InventoryClickListener(SetHomePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player player)) {
            return;
        }

        if (!event.getView().getTitle().equals(ChatColor.BLUE + "GUI SetHome")) {
            return;
        }

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        if (!displayName.startsWith("Giường ")) {
            return;
        }

        int slotIndex;
        try {
            slotIndex = Integer.parseInt(displayName.substring(7)) - 1;
        } catch (NumberFormatException e) {
            return;
        }

        if (slotIndex < 0 || slotIndex >= 5) {
            return;
        }

        if (plugin.getHomeManager().hasHome(player, slotIndex)) {
            plugin.getHomeManager().removeHome(player, slotIndex);
            player.sendMessage(ChatColor.RED + "Đã xóa sethome " + (slotIndex + 1));
        } else {
            Location location = player.getLocation();
            plugin.getHomeManager().setHome(player, slotIndex, location);
            player.sendMessage(ChatColor.GREEN + "Đã lưu sethome " + (slotIndex + 1));
        }

        plugin.getHomeManager().saveHomes();
        new HomeGuiCommand(plugin).openHomeGui(player);
    }
}
