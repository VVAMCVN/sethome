package nonemc.sethome.listeners;

import nonemc.sethome.SetHomePlugin;
import nonemc.sethome.commands.HomeGuiCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {
    private final SetHomePlugin plugin;
    private final Map<UUID, Integer> activeTeleportTask = new HashMap<>();

    public InventoryClickListener(SetHomePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player player)) {
            return;
        }

        if (!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(HomeGuiCommand.INVENTORY_TITLE))) {
            return;
        }

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
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

        if (rawSlot == 35) {
            event.setCancelled(true);
            boolean enabled = plugin.toggleShowDetails(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + (enabled ? "Bật hiển thị chi tiết home." : "Tắt hiển thị chi tiết home."));
            new HomeGuiCommand(plugin).openHomeGui(player);
            return;
        }

        if (rawSlot >= homeStart && rawSlot < homeStart + 5) {
            slotIndex = rawSlot - homeStart;
            if (plugin.getHomeManager().hasHome(player, slotIndex)) {
                Location homeLocation = plugin.getHomeManager().getHome(player, slotIndex);
                if (homeLocation != null) {
                    int delaySeconds = plugin.getTeleportDelaySeconds();
                    player.closeInventory();
                    player.sendMessage(ChatColor.GREEN + "Đang dịch chuyển đến sethome " + (slotIndex + 1) + " sau " + delaySeconds + " giây...");
                    if (delaySeconds <= 0) {
                        player.teleport(homeLocation);
                        player.sendMessage(ChatColor.GREEN + "Đã dịch chuyển đến sethome " + (slotIndex + 1));
                        return;
                    }

                    cancelExistingTeleport(player.getUniqueId());
                    int countdownTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
                        private int remaining = delaySeconds;

                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                cancelExistingTeleport(player.getUniqueId());
                                return;
                            }
                            if (remaining <= 0) {
                                cancelExistingTeleport(player.getUniqueId());
                                Location delayedLocation = plugin.getHomeManager().getHome(player, slotIndex);
                                if (delayedLocation != null) {
                                    player.teleport(delayedLocation);
                                    player.sendMessage(ChatColor.GREEN + "Đã dịch chuyển đến sethome " + (slotIndex + 1));
                                }
                                return;
                            }
                            if (plugin.isShowTeleportActionBar()) {
                                sendActionBar(player, formatActionBar(delaySeconds, remaining));
                            }
                            remaining--;
                        }
                    }, 0L, 20L).getTaskId();
                    activeTeleportTask.put(player.getUniqueId(), countdownTaskId);
                }
            } else {
                Location location = player.getLocation();
                plugin.getHomeManager().setHome(player, slotIndex, location);
                plugin.getHomeManager().saveHomes();
                player.sendMessage(ChatColor.GREEN + "Đã lưu sethome " + (slotIndex + 1));
                new HomeGuiCommand(plugin).openHomeGui(player);
            }
        } else if (rawSlot >= deleteStart && rawSlot < deleteStart + 5) {
            slotIndex = rawSlot - deleteStart;
            if (plugin.getHomeManager().hasHome(player, slotIndex)) {
                plugin.getHomeManager().removeHome(player, slotIndex);
                plugin.getHomeManager().saveHomes();
                player.sendMessage(ChatColor.RED + "Đã xóa sethome " + (slotIndex + 1));
                new HomeGuiCommand(plugin).openHomeGui(player);
            }
        } else {
            return;
        }
    }

    private void cancelExistingTeleport(UUID playerUuid) {
        Integer taskId = activeTeleportTask.remove(playerUuid);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    private String formatActionBar(int totalSeconds, int remainingSeconds) {
        String template = plugin.getTeleportActionBarFormat();
        return template.replace("{seconds}", String.valueOf(remainingSeconds));
    }

    private void sendActionBar(Player player, String message) {
        BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!plugin.isCancelTeleportOnDamage()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        UUID playerUuid = player.getUniqueId();
        if (activeTeleportTask.containsKey(playerUuid)) {
            cancelExistingTeleport(playerUuid);
            player.sendMessage(ChatColor.RED + "Dịch chuyển về sethome đã bị hủy vì bạn bị tấn công.");
        }
    }
}
