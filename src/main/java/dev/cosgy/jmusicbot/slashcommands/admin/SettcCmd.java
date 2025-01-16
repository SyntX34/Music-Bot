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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class SettcCmd extends AdminCommand {
    public SettcCmd(Bot bot) {
        this.name = "settc";
        this.help = "Sets the command channel for the bot";
        this.arguments = "<channel name|NONE|none>";
        this.aliases = bot.getConfig().getAliases(this.name);

        this.children = new SlashCommand[]{new Set(), new None()};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    // This is a normal command
    @Override
    protected void execute(CommandEvent event) {
        Logger log = LoggerFactory.getLogger("SettcCmd");
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "Include channel or NONE.");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().toLowerCase().matches("(none)")) {
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess() + "Music commands can now be used on any channel.");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + "No matching channels found\"" + event.getArgs() + "\"");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfTChannels(list, event.getArgs()));
            else {
                s.setTextChannel(list.get(0));
                log.info("I set up a channel for music commands.");
                event.reply(event.getClient().getSuccess() + "Music commands <#" + list.get(0).getId() + "> is set to be available only in");
            }
        }
    }

    private static class Set extends AdminCommand {
        public Set() {
            this.name = "set";
            this.help = "Set a channel for music commands";

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.CHANNEL, "channel", "Text Channels", true));

            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (checkAdminPermission(event.getClient(), event)) {
                event.reply(event.getClient().getWarning() + "Cannot execute due to lack of permission.").queue();
                return;
            }
            Settings s = event.getClient().getSettingsFor(event.getGuild());


            if (event.getOption("channel").getChannelType() != ChannelType.TEXT) {
                event.reply(event.getClient().getError() + "Set up a text channel.").queue();
                return;
            }
            Long channelId = event.getOption("channel").getAsLong();
            TextChannel tc = event.getGuild().getTextChannelById(channelId);

            s.setTextChannel(tc);
            event.reply(client.getSuccess() + "Music commands <#" + tc.getId() + "> is set to be available only in").queue();

        }
    }

    private static class None extends AdminCommand {
        public None() {
            this.name = "none";
            this.help = "Disables the channel for music commands.";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (checkAdminPermission(event.getClient(), event)) {
                event.reply(event.getClient().getWarning() + "Cannot execute due to lack of permission.").queue();
                return;
            }
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess() + "Music commands can now be used on any channel.").queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setTextChannel(null);
            event.replySuccess("Music commands can now be used on any channel.");
        }
    }

}
