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

import java.util.ArrayList;
import java.util.List;

public class HomeGuiCommand implements CommandExecutor {
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
        Inventory gui = Bukkit.createInventory(null, 36, ChatColor.BLUE + "GUI SetHome");
        ItemStack filler = createFillerItem();

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        int homeStart = 11; // row 2, centered 5 items
        int deleteStart = 20; // row 3, centered 5 items
        for (int slot = 0; slot < 5; slot++) {
            gui.setItem(homeStart + slot, createBedItem(player, slot));
            gui.setItem(deleteStart + slot, createDeleteItem(player, slot));
        }

        player.openInventory(gui);
    }

    private ItemStack createFillerItem() {
        ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        return filler;
    }

    private ItemStack createBedItem(Player player, int slot) {
        boolean saved = plugin.getHomeManager().hasHome(player, slot);
        Material material = saved ? Material.LIME_BED : Material.WHITE_BED;
        String displayName = (saved ? ChatColor.GREEN : ChatColor.WHITE) + "Giường " + (slot + 1);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            if (saved) {
                lore.add(ChatColor.AQUA + "Đã lưu sethome.");
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

    private ItemStack createDeleteItem(Player player, int slot) {
        if (!plugin.getHomeManager().hasHome(player, slot)) {
            return createFillerItem();
        }

        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Xóa Home " + (slot + 1));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Nhấp để xóa home đã lưu.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
