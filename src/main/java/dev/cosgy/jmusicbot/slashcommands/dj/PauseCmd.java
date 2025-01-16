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
package dev.cosgy.jmusicbot.slashcommands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.PlayStatus;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import dev.cosgy.jmusicbot.slashcommands.DJCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PauseCmd extends DJCommand {
    Logger log = LoggerFactory.getLogger("Pause");

    public PauseCmd(Bot bot) {
        super(bot);
        this.name = "pause";
        this.help = "Pauses the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().isPaused()) {
            event.replyWarning("The song is already paused. You can unpause it using `" + event.getClient().getPrefix() + " play`.");
            return;
        }
        handler.getPlayer().setPaused(true);
        log.info(event.getGuild().getName() + "paused on " + handler.getPlayer().getPlayingTrack().getInfo().title + "");
        event.replySuccess("**" + handler.getPlayer().getPlayingTrack().getInfo().title + "** has been paused. Use `" + event.getClient().getPrefix() + " play` to unpause.");
        Bot.updatePlayStatus(event.getGuild(), event.getGuild().getSelfMember(), PlayStatus.PAUSED);
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        if (!checkDJPermission(event.getClient(), event)) {
            event.reply(event.getClient().getWarning() + "Cannot execute due to insufficient permission.").queue();
            return;
        }
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().isPaused()) {
            event.reply(event.getClient().getWarning() + "The song is already paused. `" + event.getClient().getPrefix() + "You can unpause it using play`.").queue();
            return;
        }
        handler.getPlayer().setPaused(true);
        log.info(event.getGuild().getName() + "paused on " + handler.getPlayer().getPlayingTrack().getInfo().title + "");
        event.reply(event.getClient().getSuccess() + "**" + handler.getPlayer().getPlayingTrack().getInfo().title + "** has been paused. `" + event.getClient().getPrefix() + " Use play` to unpause.").queue();

        Bot.updatePlayStatus(event.getGuild(), event.getGuild().getSelfMember(), PlayStatus.PAUSED);
    }
}
