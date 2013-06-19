package com.domsplace;

import com.domsplace.commands.*;
import com.domsplace.listeners.BanListener;
import java.io.InputStream;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SELBans extends JavaPlugin {
    public static boolean isPluginEnabled = false;
    public static YamlConfiguration pluginYML;
    
    public PluginManager pluginManager;
    
    public static BanListener listenerBan;
    
    @Override
    public void onEnable() {
        BansUtils.dataFolder = this.getDataFolder();
        
        InputStream is = this.getResource("plugin.yml");
        pluginYML = YamlConfiguration.loadConfiguration(is);
        
        pluginManager = getServer().getPluginManager();
        
        //Add Mockup Muted Commands//
        BansBase.mutedCommands.add("msg");
        BansBase.mutedCommands.add("tell");
        BansBase.mutedCommands.add("whisper");
        BansBase.mutedCommands.add("m");
        BansBase.mutedCommands.add("reply");
        BansBase.mutedCommands.add("r");
        
        if(!BansDataManager.checkConfig(this)) {
            disable();
            return;
        }
        
        /*** Register Commands ***/
        CommandBan commandBan = new CommandBan(this);
        CommandKick commandKick = new CommandKick(this);
        CommandMute commandMute = new CommandMute(this);
        CommandPardon commandPardon = new CommandPardon(this);
        CommandPlayerInfo commandPlayerInfo = new CommandPlayerInfo(this);
        
        getCommand("ban").setExecutor(commandBan);
        getCommand("kick").setExecutor(commandKick);
        getCommand("warn").setExecutor(commandKick);
        getCommand("mute").setExecutor(commandMute);
        getCommand("pardon").setExecutor(commandPardon);
        getCommand("playerinfo").setExecutor(commandPlayerInfo);
        getCommand("SELBans").setExecutor(commandPlayerInfo);
        
        /*** Register Listeners and start Threads ***/
        listenerBan = new BanListener(this);
        Bukkit.getPluginManager().registerEvents(listenerBan, this);
        
        isPluginEnabled = true;
        Bukkit.broadcastMessage("§dLoaded " + pluginYML.getString("name") + " version " + pluginYML.getString("version") + " successfully.");
    }
    
    @Override
    public void onDisable() {
        /*** Cancel Threads ***/
        if(listenerBan != null) {
            listenerBan.checkBans.cancel();
        }
        
        if(!isPluginEnabled) {
            BansUtils.msgConsole(BansBase.ChatError + "Plugin failed to load!");
            return;
        }
        
        BansUtils.sqlClose();
    }
    
    public void disable() {
        pluginManager.disablePlugin(this);
    }
}
