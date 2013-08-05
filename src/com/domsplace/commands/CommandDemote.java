package com.domsplace.commands;

import com.domsplace.BansBase;
import com.domsplace.BansDataManager;
import com.domsplace.BansUtils;
import com.domsplace.SELBans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDemote extends BansBase implements CommandExecutor {
    /* References Main Plugin */
    private final SELBans plugin;
    
    /* Basic Constructor */
    public CommandDemote(SELBans base) {
        plugin = base;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if(cmd.getName().equalsIgnoreCase("demote")) {
            if(args.length < 1) {
                sender.sendMessage(ChatError + "Enter a player name!");
                return false;
            }
            
            if(args.length < 2) {
                sender.sendMessage(ChatError + "Enter a group name.");
                return false;
            }
            
            if(args.length < 3) {
                sender.sendMessage(ChatError + "Enter a reason.");
                return false;
            }
            
            /* Is target Valid? */
            OfflinePlayer target = getOfflinePlayer(args[0], sender);
            if(target == null) {
                sender.sendMessage(ChatError + args[0] + " hasn't played before!");
                return true;
            }
            String message = "";
            
            for(int i = 2; i < args.length; i++) {
                message += args[i];
                if(i < (args.length - 1)) {
                    message += " ";
                }
            }
            
            BansUtils.DemotePlayer(target, message, sender, args[1]);
            
            for(String c : BansDataManager.config.getStringList("demote.commands")) {
                c = c.replaceAll("%player%", target.getName()).replaceAll("%group%", args[1]);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
            }
            return true;
        }
        
        return false;
    }
}
