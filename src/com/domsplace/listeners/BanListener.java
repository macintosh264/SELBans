package com.domsplace.listeners;

import static com.domsplace.BansBase.MuteMessageChat;
import com.domsplace.BansDataManager;
import com.domsplace.BansUtils;
import com.domsplace.Events.SELBansCommandEvent;
import com.domsplace.SELBans;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitTask;

public class BanListener extends SELBansListenerBase {
    
    /* References Main Plugin */
    public BukkitTask checkBans;
    
    /* Basic Constructor */
    public BanListener() {
        checkBans = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), new Runnable() {
            @Override
            public void run() {
                BansUtils.checkBans();
            }
        }, 60L, 200L);
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void playerLogin(PlayerLoginEvent event) {
        BansUtils.checkBans();
        if(!BansUtils.isPlayerBanned(event.getPlayer())){
            return;
        }
        String reason = BansUtils.getBanReason(event.getPlayer(), "ban");
        String banner = BansUtils.getBanner(event.getPlayer(), "ban");
        if(!"Unknown reason".equals(reason)) {
            event.setKickMessage(BansUtils.KickMessageFormat(KickMessage, reason, banner));
        }
        event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if(BansUtils.CanPlayerTalk(e.getPlayer())) {
            return;
        }
        for(String tc : mutedCommands) {
            if(!e.getMessage().toLowerCase().startsWith("/" + tc.toLowerCase())) {
                continue;
            }
            
            if(!e.getMessage().toLowerCase().replaceAll("/" + tc.toLowerCase(), "").startsWith(" ")) {
                if(!e.getMessage().toLowerCase().replaceAll("/" + tc.toLowerCase(), "").equalsIgnoreCase("")) {
                    continue;
                }
            }
            
            String reason = BansUtils.getBanReason(e.getPlayer(), "mute");
            String banner = BansUtils.getBanner(e.getPlayer(), "mute");

            e.getPlayer().sendMessage(BansUtils.KickMessageFormat(MuteMessageCommand, reason, banner));
            Bukkit.getLogger().info(e.getPlayer().getName() + " tried to run the command /" + tc + " but is muted.");
            e.setCancelled(true);
            e.setMessage("");
            return;
        }
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if(!hideDeathMessage) {
            return;
        }
        
        if(e.getEntity() == null) {
            return;
        }
        
        if(e.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        
        Player player = (Player) e.getEntity();
        
        if(!BansUtils.CanPlayerTalk(player)) {
            e.setDeathMessage(null);
            return;
        }
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onAsyncChat(AsyncPlayerChatEvent e) {
        if(BansUtils.CanPlayerTalk(e.getPlayer())) {
            return;
        }
        
        String reason = BansUtils.getBanReason(e.getPlayer(), "mute");
        String banner = BansUtils.getBanner(e.getPlayer(), "mute");
        
        e.getPlayer().sendMessage(BansUtils.KickMessageFormat(MuteMessageChat, reason, banner));
        Bukkit.getLogger().info(e.getPlayer().getName() + " tried to say \"" + e.getMessage() + "\" but is muted.");
        e.setCancelled(true);
        e.setMessage("");
        e.setFormat("");
    }
    
    //Added Depreciated Chat Event (Should help with some other chat plugins)
    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void onChat(PlayerChatEvent e) {
        if(BansUtils.CanPlayerTalk(e.getPlayer())) {
            return;
        }
        String reason = BansUtils.getBanReason(e.getPlayer(), "mute");
        String banner = BansUtils.getBanner(e.getPlayer(), "mute");
        
        e.getPlayer().sendMessage(BansUtils.KickMessageFormat(MuteMessageChat, reason, banner));
        Bukkit.getLogger().info(e.getPlayer().getName() + " tried to say \"" + e.getMessage() + "\" but is muted.");
        e.setCancelled(true);
        e.setMessage("");
        e.setFormat("");
    }
    
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void cmdAliases(PlayerCommandPreprocessEvent e) {
        for(String tc : BansDataManager.config.getStringList("demote.aliases")) {
            if(!e.getMessage().toLowerCase().startsWith("/" + tc.toLowerCase())) {
                continue;
            }
            
            if(!e.getMessage().toLowerCase().replaceAll("/" + tc.toLowerCase(), "").startsWith(" ")) {
                if(!e.getMessage().toLowerCase().replaceAll("/" + tc.toLowerCase(), "").equalsIgnoreCase("")) {
                    continue;
                }
            }
        
            String[] args = e.getMessage().split(" ");
            List<String> fargs = new ArrayList<String>();
            for(String a : args) {
                if(a == "") {
                    continue;
                }
                fargs.add(a);
            }

            args = new String[0];
            try {
                args = new String[fargs.size()-1];
                for(int i = 1 ; i < fargs.size(); i++) {
                    args[i-1] = fargs.get(i);
                }
            } catch(Exception ex) {
                args = new String[0];
            }
            
            SELBans.getPlugin().getCommand("demote").execute(e.getPlayer(), tc, args);
            
            e.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void checkForCommand(PlayerCommandPreprocessEvent e) {
        if(!e.getMessage().replaceAll(" ", "").startsWith("/")) {
            return;
        }
        
        String msg = e.getMessage().replaceFirst("/", "");
        String[] msgs = e.getMessage().split(" ");
        List<String> fargs = new ArrayList<String>();
        for(String s :  msgs) {
            if(s.equals("") || s.equals(" ") || s.equals("/")) {
                continue;
            }
            
            fargs.add(s);
        }
        
        if(fargs.size() < 1) {
            return;
        }
        
        //Try to get command
        Map<String, String[]> aliases = getAliases();
        
        String label = fargs.get(0).replaceAll("/", "");
        String cmd = null;
        for(String c : aliases.keySet()) {
            String[] al = aliases.get(c);
            if(c.equalsIgnoreCase(label)) {
                cmd = c;
                break;
            }
            
            for(String a : al) {
                if(a.equalsIgnoreCase(label)) {
                    cmd = c;
                    break;
                }
            }
        }
        
        if(cmd == null) {
            return;
        }
        
        String[] args;
        try {
            args = new String[fargs.size()-1];
            for(int i = 1 ; i < fargs.size(); i++) {
                args[i-1] = fargs.get(i);
            }
        } catch(Exception ex) {
            args = new String[0];
        }
        
        try {
            Command command = Bukkit.getPluginCommand(cmd);
            
            SELBansCommandEvent event = new SELBansCommandEvent(command, e.getPlayer(), label, args);
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()) {
                e.setCancelled(true);
            }
        } catch(Exception ex) {
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void playDirty(SELBansCommandEvent e) {
        if(!BansDataManager.config.getBoolean("playdirty")) {
            return;
        }
        getPlugin().dispatchCommand(e);
        e.setCancelled(true);
    }
}
