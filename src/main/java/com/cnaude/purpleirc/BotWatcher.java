/*
 * Copyright (C) 2014 cnaude
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cnaude.purpleirc;

import org.bukkit.scheduler.BukkitTask;

/**
 * This thread checks each bot for connectivity and reconnects when appropriate.
 *
 * @author Chris Naude
 *  */
public class BotWatcher {
    
    private final PurpleIRC plugin;
    private final BukkitTask bt;
    
    /**
     * Run the BotWatcher thread asynchronously at configured interval.
     * 
     * @param plugin the PurpleIRC plugin
     */
    public BotWatcher(final PurpleIRC plugin) {
        this.plugin = plugin;

        bt = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                for (PurpleBot ircBot : plugin.ircBots.values()) {
                    if (ircBot.isConnectedBlocking()) {
                        ircBot.setConnected(true);
                    } else {
                        ircBot.setConnected(false);
                        if (ircBot.autoConnect) {
                            plugin.logDebug("[" + ircBot.botNick + "] Attempting reconnect...");
                            ircBot.reload();
                        }
                    }
                }
            }
        }, plugin.ircConnCheckInterval, plugin.ircConnCheckInterval);
    }
    
    /**
     * Cancel the scheduled BukkitTask. Call this when
     * shutting down.
     */
    public void cancel() {
        bt.cancel();
    }

}