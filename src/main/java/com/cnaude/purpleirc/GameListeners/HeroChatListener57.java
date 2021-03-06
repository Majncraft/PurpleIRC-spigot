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
package com.cnaude.purpleirc.GameListeners;

import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import com.cnaude.purpleirc.TemplateName;
import com.herochat.api.Channel;
import com.herochat.api.ChatResult;
import com.herochat.api.Chatter;
import com.herochat.api.event.ChannelChatEvent;
import com.herochat.channel.ConversationChannel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 *
 * @author Chris Naude
 */
public class HeroChatListener57 implements Listener {

    final PurpleIRC plugin;

    /**
     *
     * @param plugin the PurpleIRC plugin
     */
    public HeroChatListener57(PurpleIRC plugin) {
        this.plugin = plugin;
    }

    /**
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onChannelChatEvent(ChannelChatEvent event) {
        Chatter chatter = event.getChatter();
        Channel channel = chatter.getActiveChannel();
        if (channel instanceof ConversationChannel || event.getFormat().contains("{convopartner}")) {
            plugin.logDebug("Ignoring private message: " + event.getMessage());
            return;
        }
        ChatResult result = event.getResult();
        plugin.logDebug("HC Format: " + event.getFormat());
        plugin.logDebug("HC Result: " + event.getResult());
        if (plugin.heroPrivateChatFormat.equals(event.getFormat())) {
            plugin.logDebug("HC Private: TRUE");
            return;
        }

        ChatColor chatColor = event.getChannel().getColor();
        Player player = chatter.getPlayer();
        if (player.hasPermission("irc.message.gamechat")
                && chatter.getChannels().contains(event.getChannel())
                && result.equals(ChatResult.ALLOWED)) {
            for (PurpleBot ircBot : plugin.ircBots.values()) {
                if (plugin.heroChatEmoteFormat.equals(event.getFormat())) {
                    plugin.logDebug("HC Emote: TRUE");
                    heroAction(ircBot, chatter, chatColor, event.getMessage());
                } else {
                    plugin.logDebug("HC Emote: FALSE");
                    heroChat(ircBot, chatter, chatColor, event.getMessage());
                }
            }
        }
    }

    /**
     * Called from HeroChat listener
     *
     * @param ircBot
     * @param chatter
     * @param chatColor
     * @param message
     */
    public void heroChat(PurpleBot ircBot, Chatter chatter, ChatColor chatColor, String message) {
        if (!ircBot.isConnected()) {
            return;
        }
        Player player = chatter.getPlayer();
        if (ircBot.floodChecker.isSpam(player)) {
            ircBot.sendFloodWarning(player);
            return;
        }
        for (String channelName : ircBot.botChannels) {
            if (ircBot.isPlayerInValidWorld(player, channelName)) {
                String hChannel = chatter.getActiveChannel().getName();
                String hNick = chatter.getActiveChannel().getNick();
                String hColor = chatColor.toString();
                plugin.logDebug("HC Channel: " + hChannel);
                if (ircBot.isMessageEnabled(channelName, "hero-" + hChannel + "-chat")
                        || ircBot.isMessageEnabled(channelName, TemplateName.HERO_CHAT)) {
                    ircBot.asyncIRCMessage(channelName, plugin.tokenizer
                            .chatHeroTokenizer(player, message, hColor, hChannel,
                                    hNick, plugin.getHeroChatTemplate(ircBot.botNick, hChannel)));
                } else {
                    plugin.logDebug("Player " + player.getName() + " is in \""
                            + hChannel + "\" but hero-" + hChannel + "-chat is disabled.");
                }
            }
        }
    }

    /**
     *
     * @param ircBot
     * @param chatter
     * @param chatColor
     * @param message
     */
    public void heroAction(PurpleBot ircBot, Chatter chatter, ChatColor chatColor, String message) {
        if (!ircBot.isConnected()) {
            return;
        }
        Player player = chatter.getPlayer();
        if (ircBot.floodChecker.isSpam(player)) {
            ircBot.sendFloodWarning(player);
            return;
        }
        for (String channelName : ircBot.botChannels) {
            if (!ircBot.isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            String hChannel = chatter.getActiveChannel().getName();
            String hNick = chatter.getActiveChannel().getNick();
            String hColor = chatColor.toString();
            plugin.logDebug("HC Channel: " + hChannel);
            if (ircBot.isMessageEnabled(channelName, "hero-" + hChannel + "-action")
                    || ircBot.isMessageEnabled(channelName, "hero-action")) {
                ircBot.asyncIRCMessage(channelName, plugin.tokenizer
                        .chatHeroTokenizer(player, message, hColor, hChannel,
                                hNick, plugin.getHeroActionTemplate(ircBot.botNick, hChannel)));
            } else {
                plugin.logDebug("Player " + player.getName() + " is in \""
                        + hChannel + "\" but hero-" + hChannel + "-action is disabled.");
            }
        }
    }

}
