package com.domsplace.commands;

import com.domsplace.BansBase;
import static com.domsplace.BansBase.getOfflinePlayer;
import com.domsplace.BansUtils;
import com.domsplace.SELBans;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandKick extends BansBase implements CommandExecutor {
    /* References Main Plugin */
    private final SELBans plugin;
    
    /* Basic Constructor */
    public CommandKick(SELBans base) {
        plugin = base;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if(cmd.getName().equalsIgnoreCase("kick")) {
            if(args.length < 1) {
                sender.sendMessage(ChatError + "Enter a player name!");
                return false;
            }
            
            if(args.length < 2) {
                sender.sendMessage(ChatError + "Enter a reason.");
                return false;
            }
            
            /* Is target Valid? */
            Player target = getPlayer(args[0], sender);
            if(target == null) {
                sender.sendMessage(ChatError + args[0] + " isn't online!");
                return true;
            }
            String message = "";
            
            for(int i = 1; i < args.length; i++) {
                message += args[i];
                if(i < (args.length - 1)) {
                    message += " ";
                }
            }
            /* Fixing the Permission errors @macintosh264 */
            if(!sender.hasPermission("SELBans.kick")) {
                sender.sendMessage(ChatError + "You don't have permission to kick.");
                return true;
            }
            
            BansUtils.KickPlayer(target, message, sender);
            return true;
        }
        
        if(cmd.getName().equalsIgnoreCase("warn")) {
            if(args.length < 1) {
                sender.sendMessage(ChatError + "Enter a player name!");
                return false;
            }
            
            if(args.length < 2) {
                sender.sendMessage(ChatError + "Enter a reason.");
                return false;
            }
            
            /* Is target Valid? */
            OfflinePlayer target = getOfflinePlayer(args[0], sender);
            if(target == null) {
                sender.sendMessage(ChatError + args[0] + " isn't online!");
                return true;
            }
            String message = "";
            
            for(int i = 1; i < args.length; i++) {
                message += args[i];
                if(i < (args.length - 1)) {
                    message += " ";
                }
            }
            if(!sender.hasPermission("SELBans.warn")) {
                sender.sendMessage(ChatError + "You don't have permission to warn.");
                return true;
            }
            BansUtils.WarnPlayer(target, message, sender);
            return true;
        }
        return false;
    }
}
