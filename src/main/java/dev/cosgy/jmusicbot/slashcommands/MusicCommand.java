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
package dev.cosgy.jmusicbot.slashcommands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.settings.Settings;
import dev.cosgy.jmusicbot.util.MaintenanceInfo;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public abstract class MusicCommand extends SlashCommand {
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;
    Logger log = LoggerFactory.getLogger("MusicCommand");

    public MusicCommand(Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("Music");
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel channel = settings.getTextChannel(event.getGuild());
        if (bot.getConfig().getCosgyDevHost()) {
            try {

                MaintenanceInfo.CommandInfo(event, event.getClient());
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        bot.getPlayerManager().setUpHandler(event.getGuild());
        if (bePlaying && !((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError() + "Playback must be active to use this command.").queue();
            return;
        }
        if (beListening) {
            AudioChannelUnion current = event.getGuild().getSelfMember().getVoiceState().getChannel();

            if (current == null)
                current = (AudioChannelUnion) settings.getVoiceChannel(event.getGuild());
            GuildVoiceState userState = event.getMember().getVoiceState();

            if (!userState.inAudioChannel() || userState.isDeafened() || (current != null && !userState.getChannel().equals(current))) {
                event.reply(event.getClient().getError() + String.format("You must be a member of %s to use this command!", (current == null ? "Voice Channel" : "**" + current.getAsMention() + "**"))).queue();
                return;
            }
            if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                    event.getGuild().getAudioManager().setSelfDeafened(true);
                } catch (PermissionException ex) {
                    event.reply(event.getClient().getError() + String.format("**%s**Can't connect to!", userState.getChannel().getAsMention())).queue();
                    return;
                }
                if (userState.getChannel().getType() == ChannelType.STAGE) {
                    event.getTextChannel().sendMessage(event.getClient().getWarning() + String.format("You have joined the Stage channel. You must manually invite speakers to use %s on the Stage channel.", event.getGuild().getSelfMember().getNickname())).queue();
                }
            }
        }

        doCommand(event);
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel channel = settings.getTextChannel(event.getGuild());
        if (bot.getConfig().getCosgyDevHost()) {
            try {
                MaintenanceInfo.CommandInfo(event);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        if (channel != null && !event.getTextChannel().equals(channel)) {
            try {
                event.getMessage().delete().queue();
            } catch (PermissionException ignore) {
            }
            event.replyInDm(event.getClient().getError() + String.format("Command can only be executed on %s", channel.getAsMention()));
            return;
        }
        bot.getPlayerManager().setUpHandler(event.getGuild()); // no point constantly checking for this later

        if (bePlaying && !((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError() + "Playback must be active to use this command.");
            return;
        }
        if (beListening) {
            AudioChannelUnion current = event.getGuild().getSelfMember().getVoiceState().getChannel();

            if (current == null)
                current = (AudioChannelUnion) settings.getVoiceChannel(event.getGuild());
            GuildVoiceState userState = event.getMember().getVoiceState();
            if (!userState.inAudioChannel() || userState.isDeafened() || (current != null && !userState.getChannel().equals(current))) {
                event.replyError(String.format("You must be a member of %s to use this command!", (current == null ? "Audio Channels" : "**" + current.getName() + "**")));
                return;
            }
            if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                } catch (PermissionException ex) {
                    event.reply(event.getClient().getError() + String.format("**%s**Can't connect to!", userState.getChannel().getName()));
                    return;
                }
                if (userState.getChannel().getType() == ChannelType.STAGE) {
                    event.getTextChannel().sendMessage(event.getClient().getWarning() + String.format("You have joined the Stage channel. You must manually invite speakers to use %s on the Stage channel.", event.getGuild().getSelfMember().getNickname())).queue();
                }
            }
        }

        doCommand(event);
    }

    public abstract void doCommand(CommandEvent event);

    public abstract void doCommand(SlashCommandEvent event);
}
