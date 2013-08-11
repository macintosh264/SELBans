package com.domsplace.Events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

class SELBansCustomEventBase extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
        this.isCancelled = bln;
    }
}
