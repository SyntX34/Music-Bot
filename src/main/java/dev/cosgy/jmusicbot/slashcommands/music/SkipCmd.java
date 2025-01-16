/*
 * Copyright 2018 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.cosgy.jmusicbot.slashcommands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import dev.cosgy.jmusicbot.slashcommands.MusicCommand;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkipCmd extends MusicCommand {
    public SkipCmd(Bot bot) {
        super(bot);
        this.name = "skip";
        this.help = "Request to skip the currently playing track";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

        RequestMetadata rm = handler.getRequestMetadata();
        if (event.getAuthor().getIdLong() == rm.getOwner()) {
            event.reply(event.getClient().getSuccess() + "**" + (handler.getPlayer().getPlayingTrack().getInfo().uri.contains("https://stream.gensokyoradio.net/") ? "Gensokyo Radio" : handler.getPlayer().getPlayingTrack().getInfo().title) + "** was skipped.");
            handler.getPlayer().stopTrack();
        } else {
            // Number of listeners in the voice channel (excluding bots and deafened users)
            int listeners = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened() && m.getUser().getIdLong() != handler.getRequestMetadata().getOwner()).count();

            // Message to send
            String msg;

            // Check if the sender has already voted to skip
            if (handler.getVotes().contains(event.getAuthor().getId())) {
                msg = event.getClient().getWarning() + " You have already requested to skip the current track. `[";
            } else {
                msg = event.getClient().getSuccess() + "You requested to skip the current track. `[";
                handler.getVotes().add(event.getAuthor().getId());
            }

            // Count the number of votes to skip among listeners
            int skippers = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();

            int required = (int) Math.ceil(listeners * bot.getSettingsManager().getSettings(event.getGuild()).getSkipRatio());
            msg += skippers + " votes, " + required + "/" + listeners + " required]`";

            // If the required votes differ from the total listeners
            if (required != listeners) {
                msg += " Skip requests are at " + skippers + ". " + required + "/" + listeners + " required to skip.";
            } else {
                msg = "";
            }

            // Check if the number of votes meets the requirement
            if (skippers >= required) {
                msg += "\n" + event.getClient().getSuccess() + "**" + (handler.getPlayer().getPlayingTrack().getInfo().uri.contains("https://stream.gensokyoradio.net/") ? "Gensokyo Radio" : handler.getPlayer().getPlayingTrack().getInfo().title)
                        + "** was skipped. " + (rm.getOwner() == 0L ? "(Auto-play)" : "(**" + rm.user.username + "** requested)");
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

        RequestMetadata rm = handler.getRequestMetadata();
        if (event.getUser().getIdLong() == rm.getOwner()) {
            event.reply(event.getClient().getSuccess() + "**" + (handler.getPlayer().getPlayingTrack().getInfo().uri.contains("https://stream.gensokyoradio.net/") ? "Gensokyo Radio" : handler.getPlayer().getPlayingTrack().getInfo().title) + "** was skipped.").queue();
            handler.getPlayer().stopTrack();
        } else {
            // Number of listeners in the voice channel (excluding bots and deafened users)
            int listeners = (int) event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened() && m.getUser().getIdLong() != handler.getRequestMetadata().getOwner()).count();

            // Message to send
            String msg;

            // Check if the sender has already voted to skip
            if (handler.getVotes().contains(event.getUser().getId())) {
                msg = event.getClient().getWarning() + " You have already requested to skip the current track. `[";
            } else {
                msg = event.getClient().getSuccess() + "You requested to skip the current track. `[";
                handler.getVotes().add(event.getUser().getId());
            }

            // Count the number of votes to skip among listeners
            int skippers = (int) event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();

            // Required votes (55% of listeners)
            int required = (int) Math.ceil(listeners * .55);

            // If the required votes differ from the total listeners
            if (required != listeners) {
                msg += " Skip requests are at " + skippers + ". " + required + "/" + listeners + " required to skip.";
            } else {
                msg = "";
            }

            // Check if the number of votes meets the requirement
            if (skippers >= required) {
                msg += "\n" + event.getClient().getSuccess() + "**" + (handler.getPlayer().getPlayingTrack().getInfo().uri.contains("https://stream.gensokyoradio.net/") ? "Gensokyo Radio" : handler.getPlayer().getPlayingTrack().getInfo().title)
                        + "** was skipped. " + (rm.getOwner() == 0L ? "(Auto-play)" : "(**" + rm.user.username + "** requested)");
                handler.getPlayer().stopTrack();
            }
            event.reply(msg).queue();
        }
    }
}

