package com.domsplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
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
        
        if(!(sender instanceof Player)) {
            return p;
        }
        
        if(p.isOnline() && !((Player) sender).canSee(p.getPlayer())) {
            p = Bukkit.getOfflinePlayer(player);
        }
        
        return p;
    }
    
    public static Player getPlayer(String player, CommandSender sender) {
        Player p = Bukkit.getPlayer(player);
        if(p == null) {
            return null;
        }
        
        if(!(sender instanceof Player)) {
            return p;
        }
        
        if(p.isOnline() && !((Player) sender).canSee(p.getPlayer())) {
            p = Bukkit.getPlayerExact(player);
        }
        
        return p;
    }
    
    public static void debug(Object obj) {
        Bukkit.getServer().broadcastMessage("§aDEBUG: §b" + obj.toString());
    }
    
    public static com.domsplace.SELBans getPlugin() {
        return com.domsplace.SELBans.getPlugin();
    }
    
    public Map<String, String[]> getAliases() {
        Map<String, String[]> aliases = new HashMap<String, String[]>();
        
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(getPlugin().getResource("plugin.yml"));
        
        for(String cmd : ((MemorySection) yml.get("commands")).getKeys(false)) {
            List<String> repl = new ArrayList<String>();
            List<String> al;
            try {
                al = yml.getStringList("commands." + cmd + ".aliases");
                if(al == null) {
                    al = repl;
                }
            } catch(Exception ex) {
                al = repl;
            }
            
            String[] als = new String[al.size()];
            for(int i = 0; i < als.length; i++) {
                als[i] = al.get(i);
            }
            
            aliases.put(cmd, als);
        }
        
        return aliases;
    }
}
