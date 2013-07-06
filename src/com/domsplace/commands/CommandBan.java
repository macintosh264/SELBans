package com.domsplace.commands;

import com.domsplace.BansBase;
import static com.domsplace.BansBase.ChatError;
import com.domsplace.BansUtils;
import com.domsplace.SELBans;
import java.util.Date;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandBan extends BansBase implements CommandExecutor {
    /* References Main Plugin */
    private final SELBans plugin;
    
    /* Basic Constructor */
    public CommandBan(SELBans base) {
        plugin = base;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if(cmd.getName().equalsIgnoreCase("ban")) {
            if(args.length < 1) {
                sender.sendMessage(ChatError + "Enter a player name!");
                return false;
            }
            
            if(args.length < 2) {
                sender.sendMessage(ChatError + "Enter a reason and/or a duration.");
                return false;
            }
            
            /* Is target Valid? */
            OfflinePlayer target = Bukkit.getServer().getPlayer(args[0]);
            if(target == null) {
                target = Bukkit.getServer().getOfflinePlayer(args[0]);
                if(!target.hasPlayedBefore()) {
                    sender.sendMessage(ChatError + args[0] + " hasn't played before!");
                    return true;
                }
            }
            
            /* Checks to see if arg[1] is a valid time */
            boolean useTime = BansUtils.isValidTime(args[1]);
            
            if(useTime && args.length < 3) {
                sender.sendMessage(ChatError + "Enter a reason!");
                return false;
            }
            
            Date unbanDate = new Date();
            String message = "";
            
            for(int i = 1; i < args.length; i++) {
                message += args[i];
                if(i < (args.length - 1)) {
                    message += " ";
                }
            }
            
            if(useTime) {
                unbanDate = BansUtils.nowAndString(args[1]);
                
                if(BansBase.MaxBanTime > -1) {
                    long maxTime = new Date(BansUtils.getNow()).getTime() + BansBase.MaxBanTime*3600000;
                    if(unbanDate.getTime() > maxTime) {
                        sender.sendMessage(ChatError + "Please enter a time less than " + BansBase.MaxBanTime + " hours.");
                        return true;
                    }
                }
                
                message = "";
                for(int i = 2; i < args.length; i++) {
                    message += args[i];
                    if(i < (args.length - 1)) {
                        message += " ";
                    }
                }
            }
            
            BansUtils.BanPlayer(target, message, sender, unbanDate, useTime);
            return true;
        }
        return false;
    }
}
