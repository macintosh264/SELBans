package com.domsplace.listeners;

import com.domsplace.BansBase;
import static com.domsplace.BansBase.MuteMessageChat;
import com.domsplace.BansDataManager;
import com.domsplace.BansUtils;
import com.domsplace.SELBans;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitTask;

public class BanListener extends BansBase implements Listener {
    
    /* References Main Plugin */
    private final SELBans plugin;
    
    public BukkitTask checkBans;
    
    /* Basic Constructor */
    public BanListener(SELBans base) {
        plugin = base;
        
        checkBans = Bukkit.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
                BansUtils.checkBans();
            }
        }, 60L, 200L);
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void playerLogin(PlayerLoginEvent event) {
        BansUtils.checkBans();
        if(event.getPlayer().isBanned()){
            String reason = BansUtils.getBanReason(event.getPlayer(), "ban");
            String banner = BansUtils.getBanner(event.getPlayer(), "ban");
            if(reason != "Unknown reason") {
                event.setKickMessage(BansUtils.KickMessageFormat(KickMessage, reason, banner));
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            }
            return;
        }
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
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(BansUtils.CanPlayerTalk(e.getPlayer())) {
            return;
        }
        String reason = BansUtils.getBanReason(e.getPlayer(), "mute");
        String banner = BansUtils.getBanner(e.getPlayer(), "mute");
        
        e.getPlayer().sendMessage(BansUtils.KickMessageFormat(MuteMessageChat, reason, banner));
        Bukkit.getLogger().info(e.getPlayer().getName() + " tried to say \"" + e.getMessage() + "\" but is muted.");
        e.setCancelled(true);
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
}
