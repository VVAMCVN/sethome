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
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.BLUE + "GUI SetHome");

        for (int slot = 0; slot < 5; slot++) {
            gui.setItem(slot, createBedItem(player, slot));
        }

        player.openInventory(gui);
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
                lore.add(ChatColor.GRAY + "Nhấp để xóa home này.");
            } else {
                lore.add(ChatColor.GRAY + "Chưa lưu home.");
                lore.add(ChatColor.YELLOW + "Nhấp để lưu vị trí hiện tại.");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
