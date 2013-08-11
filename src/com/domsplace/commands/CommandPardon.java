package com.domsplace.commands;

import com.domsplace.BansBase;
import com.domsplace.BansUtils;
import com.domsplace.SELBans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandPardon extends BansBase implements CommandExecutor {
    /* References Main Plugin */
    private final SELBans plugin;
    
    /* Basic Constructor */
    public CommandPardon(SELBans base) {
        plugin = base;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if(cmd.getName().equalsIgnoreCase("pardon")) {
            if(args.length < 1) {
                sender.sendMessage(ChatError + "Please supply a player.");
                return false;
            }
            if(args.length > 1) {
                sender.sendMessage(ChatError + "Too many arguments!");
                return false;
            }
            
            OfflinePlayer target = getOfflinePlayer(args[0], sender);
            if(target == null) {
                sender.sendMessage(ChatError + args[0] + " has never played before!");
                return true;
            }
            
            if(!BansUtils.isPlayerBanned(target)) {
                sender.sendMessage(ChatError + args[0] + " isn't banned!");
                return true;
            }
            
            BansUtils.PardonPlayer(target, "ban");
            target.setBanned(false);
            
            sender.sendMessage(ChatDefault + "Unbanned " + ChatImportant + target.getName() + ChatDefault + ".");
            return true;
        }
        return false;
    }
}
