package com.domsplace.listeners;

import com.domsplace.BansBase;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class SELBansListenerBase extends BansBase implements Listener {
    public SELBansListenerBase() {
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }
}
