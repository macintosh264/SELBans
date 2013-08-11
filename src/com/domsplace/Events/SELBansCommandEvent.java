package com.domsplace.Events;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class SELBansCommandEvent extends SELBansCustomEventBase {
    
    private Command cmd;
    private Player player;
    private String label;
    private String[] args;
    
    public SELBansCommandEvent(Command cmd, Player sender, String label, String[] args) {
        this.cmd = cmd;
        this.player = sender;
        this.label = label;
        this.args = args;
    }
    
    public Command getCommand() {
        return this.cmd;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public String[] getArgs() {
        return this.args;
    }
}
