/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package dev.cosgy.jmusicbot.slashcommands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import dev.cosgy.jmusicbot.slashcommands.AdminCommand;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class SetvcCmd extends AdminCommand {
    public SetvcCmd(Bot bot) {
        this.name = "setvc";
        this.help = "Fixes the audio channel to use for playback.";
        this.arguments = "<channel name|NONE|None>";
        this.aliases = bot.getConfig().getAliases(this.name);

        this.children = new SlashCommand[]{new Set(), new None()};
    }

    @Override
    protected void execute(SlashCommandEvent slashCommandEvent) {
    }

    @Override
    protected void execute(CommandEvent event) {
        Logger log = LoggerFactory.getLogger("SetVcCmd");
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "Include audio channel or NONE.");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().toLowerCase().matches("(none)")) {
            s.setVoiceChannel(null);
            event.reply(event.getClient().getSuccess() + "Music can be played on any audio channel.");
        } else {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + "No matching audio channels found\"" + event.getArgs() + "\"");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfVChannels(list, event.getArgs()));
            else {
                s.setVoiceChannel(list.get(0));
                log.info("I set up a music channel.");
                event.reply(event.getClient().getSuccess() + "Music is**" + list.get(0).getAsMention() + "**Now it can only be played on .");
            }
        }
    }

    private static class Set extends AdminCommand {
        public Set() {
            this.name = "set";
            this.help = "Set the audio channel to use for playback";

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.CHANNEL, "channel", "Audio Channels", true));

            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (checkAdminPermission(event.getClient(), event)) {
                event.reply(event.getClient().getWarning() + "Cannot execute due to lack of permission.").queue();
                return;
            }
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            Long channel = event.getOption("channel").getAsLong();

            if (event.getOption("channel").getChannelType() != ChannelType.VOICE) {
                event.reply(event.getClient().getError() + "Set the audio channel").queue();
            }

            VoiceChannel vc = event.getGuild().getVoiceChannelById(channel);
            s.setVoiceChannel(vc);
            event.reply(event.getClient().getSuccess() + "Music**" + vc.getAsMention() + "**Now it can only be played on .").queue();
        }
    }

    private static class None extends AdminCommand {
        public None() {
            this.name = "none";
            this.help = "Resets the audio channel used for playback.";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (checkAdminPermission(event.getClient(), event)) {
                event.reply(event.getClient().getWarning() + "Cannot execute due to lack of permission.").queue();
                return;
            }
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setVoiceChannel(null);
            event.reply(event.getClient().getSuccess() + "Music can be played on any audio channel.").queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setVoiceChannel(null);
            event.replySuccess("Music can be played on any audio channel.");
        }
    }
}
