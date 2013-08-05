package com.domsplace;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BansBase {
    public static String ChatDefault = "§7";
    public static String ChatImportant = "§9";
    public static String ChatError = "§c";
    
    public static String WarnMessage = "You have been warned for %reason% by %player%";
    public static String KickMessage = "You have been kicked for %reason% by %player%";
    public static String DemoteMessage = "You have been demoted for %reason% by %player%";
    public static String BanMessage = "You have been Banned for %reason% by %player%";
    public static String BanMessageTemp = "You have been Banned for %reason% by %player%, for %time%";
    public static String MuteMessage = "You have been Muted for %reason% by %player%";
    public static String MuteMessageChat = "You have been Muted for %reason% by %player%, and you cannot talk.";
    public static String MuteMessageCommand = "You have been Muted for %reason% by %player%, and you cannot run this command.";
    public static String MuteMessageTemp = "You have been Muted for %reason% by %player%, for %time%";
    
    public static List<String> mutedCommands = new ArrayList<String>();
    
    public static long MaxBanTime = -1;
    public static long MaxMuteTime = -1;
    
    public static boolean hideDeathMessage = true;
    
    public static OfflinePlayer getOfflinePlayer(String player, CommandSender sender) {
        OfflinePlayer p = Bukkit.getPlayer(player);
        if(p == null) {
            p = Bukkit.getOfflinePlayer(player);
            return p;
        }
        
        if(!(p instanceof Player)) {
            return p;
        }
        
        if(p.isOnline() && !((Player) sender).canSee(p.getPlayer())) {
            p = Bukkit.getOfflinePlayer(player);
        }
        
        return p;
    }
}
