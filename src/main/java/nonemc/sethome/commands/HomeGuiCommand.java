package nonemc.sethome.commands;

import nonemc.sethome.SetHomePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HomeGuiCommand implements CommandExecutor {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private final SetHomePlugin plugin;

    public HomeGuiCommand(SetHomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Chỉ người chơi mới có thể dùng lệnh này.");
            return true;
        }

        openHomeGui(player);
        return true;
    }

    public void openHomeGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, ChatColor.BLUE + "GUI sethome");
        int homeStart = 11; // row 2, centered 5 items
        int deleteStart = 20; // row 3, centered 5 items
        for (int slot = 0; slot < 5; slot++) {
            gui.setItem(homeStart + slot, createBedItem(player, slot));
            ItemStack deleteItem = createDeleteItem(player, slot);
            if (deleteItem != null) {
                gui.setItem(deleteStart + slot, deleteItem);
            }
        }
        gui.setItem(35, createSettingsItem(player));

        player.openInventory(gui);
    }

    private ItemStack createSettingsItem(Player player) {
        boolean showDetails = plugin.getShowDetails(player.getUniqueId());
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Cài đặt sethome");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + (showDetails ? "Đang hiển thị chi tiết home" : "Đang ẩn chi tiết home"));
            lore.add(ChatColor.GRAY + "Click để thay đổi cách hiển thị.");
            lore.add(ChatColor.AQUA + "Delay dịch chuyển: " + plugin.getTeleportDelaySeconds() + " giây");
            lore.add(ChatColor.AQUA + "ActionBar: " + (plugin.isShowTeleportActionBar() ? "Bật" : "Tắt"));
            lore.add(ChatColor.GRAY + "Cấu hình action bar trong config.yml.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBedItem(Player player, int slot) {
        boolean saved = plugin.getHomeManager().hasHome(player, slot);
        boolean showDetails = plugin.getShowDetails(player.getUniqueId());
        Material material = saved ? Material.BLUE_BED : Material.WHITE_BED;
        String displayName = ChatColor.BLUE + "sethome " + (slot + 1);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            if (saved) {
                if (showDetails) {
                    lore.add(ChatColor.AQUA + "sethome " + (slot + 1));
                    lore.add(ChatColor.GRAY + "Toạ độ: " + getCoordinateText(player, slot));
                    lore.add(ChatColor.GRAY + "Lưu: " + getFormattedDate(player, slot));
                } else {
                    lore.add(ChatColor.GRAY + "Đã lưu. Hover để xem chi tiết.");
                }
                lore.add(ChatColor.YELLOW + "Nhấp để dịch chuyển tới home này.");
            } else {
                lore.add(ChatColor.GRAY + "Chưa lưu home.");
                lore.add(ChatColor.YELLOW + "Nhấp để lưu vị trí hiện tại.");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getCoordinateText(Player player, int slot) {
        var home = plugin.getHomeManager().getHome(player, slot);
        if (home == null) {
            return "-";
        }
        return home.getBlockX() + ", " + home.getBlockY() + ", " + home.getBlockZ();
    }

    private String getFormattedDate(Player player, int slot) {
        long timestamp = plugin.getHomeManager().getHomeTimestamp(player, slot);
        return timestamp <= 0 ? "-" : DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    private ItemStack createDeleteItem(Player player, int slot) {
        if (!plugin.getHomeManager().hasHome(player, slot)) {
            return null;
        }

        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Xóa sethome " + (slot + 1));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Nhấp để xóa home đã lưu.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
