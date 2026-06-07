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

        int rawSlot = event.getRawSlot();
        int homeStart = 11;
        int deleteStart = 20;
        int slotIndex;

        if (rawSlot >= homeStart && rawSlot < homeStart + 5) {
            slotIndex = rawSlot - homeStart;
            if (plugin.getHomeManager().hasHome(player, slotIndex)) {
                Location homeLocation = plugin.getHomeManager().getHome(player, slotIndex);
                if (homeLocation != null) {
                    player.teleport(homeLocation);
                    player.sendMessage(ChatColor.GREEN + "Đã dịch chuyển đến home " + (slotIndex + 1));
                }
            } else {
                Location location = player.getLocation();
                plugin.getHomeManager().setHome(player, slotIndex, location);
                plugin.getHomeManager().saveHomes();
                player.sendMessage(ChatColor.GREEN + "Đã lưu sethome " + (slotIndex + 1));
            }
        } else if (rawSlot >= deleteStart && rawSlot < deleteStart + 5) {
            slotIndex = rawSlot - deleteStart;
            if (plugin.getHomeManager().hasHome(player, slotIndex)) {
                plugin.getHomeManager().removeHome(player, slotIndex);
                plugin.getHomeManager().saveHomes();
                player.sendMessage(ChatColor.RED + "Đã xóa sethome " + (slotIndex + 1));
            }
        } else {
            return;
        }

        new HomeGuiCommand(plugin).openHomeGui(player);
    }
}
