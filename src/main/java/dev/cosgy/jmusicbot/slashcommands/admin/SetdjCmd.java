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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class SetdjCmd extends AdminCommand {
    public SetdjCmd(Bot bot) {
        this.name = "setdj";
        this.help = "Sets the role DJ who can use bot commands.";
        this.arguments = "<role name|NONE|none>";
        this.aliases = bot.getConfig().getAliases(this.name);

        this.children = new SlashCommand[]{new SetRole(), new None()};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (checkAdminPermission(event.getClient(), event)) {
            event.reply(event.getClient().getWarning() + "Unable to execute due to lack of permissions。").queue();
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());

        if (event.getOption("role") != null) {
            s.setDJRole(event.getOption("role").getAsRole());
            event.reply(event.getClient().getSuccess() + "DJ command role,**" + event.getOption("role").getAsRole().getName() + "**It has been set up to be available to users.").queue();
            return;
        }
        if (event.getOption("none").getAsString().toLowerCase().matches("(none)")) {
            s.setDJRole(null);
            event.reply(event.getClient().getSuccess() + "The DJ role has been reset. Only admins can use DJ commands.").queue();
        } else {
            event.reply("Incorrect command.").queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        Logger log = LoggerFactory.getLogger("SetDjCmd");
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "Give it a name for the role or something like NONE.");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().toLowerCase().matches("(none)")) {
            s.setDJRole(null);
            event.reply(event.getClient().getSuccess() + "The DJ role has been reset. Only admins can use DJ commands.");
        } else {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + "No roles found\"" + event.getArgs() + "\"");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfRoles(list, event.getArgs()));
            else {
                s.setDJRole(list.get(0));
                log.info("New roles have been added that can use DJ commands.(" + list.get(0).getName() + ")");
                event.reply(event.getClient().getSuccess() + "DJ command role,**" + list.get(0).getName() + "**It has been set up to be available to users.");
            }
        }
    }

    private static class SetRole extends AdminCommand {
        public SetRole() {
            this.name = "set";
            this.help = "Set the role that will grant DJ privileges.";

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.ROLE, "role", "Roles that grant permissions", true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            Role role = event.getOption("role").getAsRole();

            s.setDJRole(role);
            event.reply(event.getClient().getSuccess() + "DJ command role,**" + role.getName() + "**It has been set up to be available to users.").queue();
        }
    }

    private static class None extends AdminCommand {
        public None() {
            this.name = "none";
            this.help = "Resetting the DJ role";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setDJRole(null);
            event.reply(event.getClient().getSuccess() + "The DJ role has been reset. Only admins can use DJ commands.").queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setDJRole(null);
            event.replySuccess("The DJ role has been reset. Only admins can use DJ commands.");
        }
    }

}
