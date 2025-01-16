package dev.cosgy.jmusicbot.slashcommands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import dev.cosgy.jmusicbot.playlist.MylistLoader;
import dev.cosgy.jmusicbot.slashcommands.DJCommand;
import dev.cosgy.jmusicbot.slashcommands.MusicCommand;
import dev.cosgy.jmusicbot.util.StackTraceUtil;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kosugikun
 */
public class MylistCmd extends MusicCommand {

    public MylistCmd(Bot bot) {
        super(bot);
        this.guildOnly = false;
        this.name = "mylist";
        this.arguments = "<append|delete|make|all|show>";
        this.help = "Manage your own playlists";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.children = new MusicCommand[]{
                new PlayCmd(bot),
                new MakelistCmd(bot),
                new DeletelistCmd(bot),
                new AppendlistCmd(bot),
                new ListCmd(bot),
                new ShowTracksCmd(bot)
        };
    }

    @Override
    public void doCommand(CommandEvent event) {

        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " My List Management Command:\n");
        for (Command cmd : this.children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments() == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.reply(builder.toString());
    }

    @Override
    public void doCommand(SlashCommandEvent slashCommandEvent) {
    }

    public static class ShowTracksCmd extends MusicCommand {
        public ShowTracksCmd(Bot bot) {
            super(bot);
            this.name = "show";
            this.help = "Show songs in the specified My List";
            this.arguments = "<name>";
            this.guildOnly = false;
            this.ownerCommand = false;

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "Playlist name", true));
            this.options = options;
        }

        @Override
        public void doCommand(CommandEvent event) {
            String userId = event.getAuthor().getId();
            String playlistName = event.getArgs().trim();

            if (playlistName.isEmpty()) {
                event.reply(event.getClient().getError() + "Please specify a playlist name.");
                return;
            }

            MylistLoader.Playlist playlist = bot.getMylistLoader().getPlaylist(userId, playlistName);
            if (playlist == null) {
                event.reply(event.getClient().getError() + "My list `" + playlistName + "` was not found.");
                return;
            }

            if (playlist.getTracks().isEmpty()) {
                event.reply(event.getClient().getWarning() + "There are no songs in my list `" + playlistName + "`.");
                return;
            }

            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Songs in MyList `" + playlistName + "`:\n" );
            for (int i = 0; i < playlist.getTracks().size(); i++) {
                builder.append(i + 1).append(". ").append(playlist.getTracks().get(i).getInfo().title).append("\n");
            }

            if (builder.length() > 2000) {
                builder.setLength(1997); // Discord Message Limit
                builder.append("...");
            }

            event.reply(builder.toString());
        }

        @Override
        public void doCommand(SlashCommandEvent event) {
            String userId = event.getUser().getId();
            String playlistName = event.getOption("name").getAsString();

            MylistLoader.Playlist playlist = bot.getMylistLoader().getPlaylist(userId, playlistName);
            if (playlist == null) {
                event.reply(event.getClient().getError() + "My list `" + playlistName + "` was not found.").queue();
                return;
            }

            if (playlist.getTracks().isEmpty()) {
                event.reply(event.getClient().getWarning() + "There are no songs in my list `" + playlistName + "`.").queue();
                return;
            }

            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Songs in MyList `" + playlistName + "`:\n" );
            for (int i = 0; i < playlist.getTracks().size(); i++) {
                builder.append(i + 1).append(". ").append(playlist.getTracks().get(i).getInfo().title).append("\n");
            }

            if (builder.length() > 2000) {
                builder.setLength(1997); // Discord Message Limit
                builder.append("...");
            }

            event.reply(builder.toString()).queue();
        }
    }


    public class PlayCmd extends MusicCommand {
        public PlayCmd(Bot bot) {
            super(bot);
            this.name = "play";
            this.aliases = new String[]{"pl"};
            this.arguments = "<name>";
            this.help = "Play My List";
            this.beListening = true;
            this.bePlaying = false;

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "My List Name", true));
            this.options = options;
        }

        @Override
        public void doCommand(CommandEvent event) {
            String userId = event.getAuthor().getId();
            if (event.getArgs().isEmpty()) {
                event.reply(event.getClient().getError() + "Please include the My List name.");
                return;
            }
            MylistLoader.Playlist playlist = bot.getMylistLoader().getPlaylist(userId, event.getArgs());
            if (playlist == null) {
                event.replyError("Could not find `" + event.getArgs() + ".txt`");
                return;
            }
            event.getChannel().sendMessage(":calling: My List **" + event.getArgs() + "Loading **... (" + playlist.getItems().size() + " songs)").queue(m ->
            {
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                playlist.loadTracks(bot.getPlayerManager(), (at) -> handler.addTrack(new QueuedTrack(at, event.getAuthor())), () -> {
                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                            ? event.getClient().getWarning() + "No songs loaded."
                            : event.getClient().getSuccess() + "**" + playlist.getTracks().size() + "** Song(s) loaded.");
                    if (!playlist.getErrors().isEmpty())
                        builder.append("\nThe following songs could not be loaded:");
                    playlist.getErrors().forEach(err -> builder.append("\n`[").append(err.getIndex() + 1)
                            .append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
                    String str = builder.toString();
                    if (str.length() > 2000)
                        str = str.substring(0, 1994) + " (omitted below)";
                    m.editMessage(FormatUtil.filter(str)).queue();
                });
            });
        }

        @Override
        public void doCommand(SlashCommandEvent event) {
            String userId = event.getUser().getId();

            String name = event.getOption("name").getAsString();

            MylistLoader.Playlist playlist = bot.getMylistLoader().getPlaylist(userId, name);
            if (playlist == null) {
                event.reply(event.getClient().getError() + "Could not find `" + name + ".txt`").queue();
                return;
            }
            event.reply(":calling: My List **" + name + "Loading **... (" + playlist.getItems().size() + " songs)").queue(m ->
            {
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                playlist.loadTracks(bot.getPlayerManager(), (at) -> handler.addTrack(new QueuedTrack(at, event.getUser())), () -> {
                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                            ? event.getClient().getWarning() + "No songs loaded."
                            : event.getClient().getSuccess() + "**" + playlist.getTracks().size() + "** Song(s) loaded.");
                    if (!playlist.getErrors().isEmpty())
                        builder.append("\nThe following songs could not be loaded:");
                    playlist.getErrors().forEach(err -> builder.append("\n`[").append(err.getIndex() + 1)
                            .append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
                    String str = builder.toString();
                    if (str.length() > 2000)
                        str = str.substring(0, 1994) + " (The following is omitted)";
                    m.editOriginal(FormatUtil.filter(str)).queue();
                });
            });
        }
    }


    public static class MakelistCmd extends DJCommand {
        public MakelistCmd(Bot bot) {
            super(bot);
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "Create a new playlist";
            this.arguments = "<name>";
            this.guildOnly = true;
            this.ownerCommand = false;

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "Playlist Name", true));
            this.options = options;
        }

        @Override
        public void doCommand(CommandEvent event) {

            String pName = event.getArgs().replaceAll("\\s+", "_");
            String userId = event.getAuthor().getId();

            if (pName.isEmpty()) {
                event.replyError("Specify the playlist name.");
                return;
            }

            if (bot.getMylistLoader().getPlaylist(userId, pName) == null) {
                try {
                    bot.getMylistLoader().createPlaylist(userId, pName);
                    event.reply(event.getClient().getSuccess() + "My List`" + pName + "` Created");
                } catch (IOException e) {
                    if (event.isOwner() || event.getMember().isOwner()) {
                        event.replyError("There was an error loading songs.\n" +
                                "**Error content: " + e.getLocalizedMessage() + "**");
                        StackTraceUtil.sendStackTrace(event.getTextChannel(), e);
                        return;
                    }

                    event.reply(event.getClient().getError() + " My list could not be created.:" + e.getLocalizedMessage());
                }
            } else {
                event.reply(event.getClient().getError() + " My List`" + pName + "` already exists");
            }
        }

        @Override
        public void doCommand(SlashCommandEvent event) {
            String pName = event.getOption("name").getAsString().replaceAll("\\s+", "_");
            String userId = event.getUser().getId();

            if (pName.isEmpty()) {
                event.reply(event.getClient().getError() + "Specify the playlist name.").queue();
                return;
            }

            if (bot.getMylistLoader().getPlaylist(userId, pName) == null) {
                try {
                    bot.getMylistLoader().createPlaylist(userId, pName);
                    event.reply(event.getClient().getSuccess() + "My List`" + pName + "` Created").queue();
                } catch (IOException e) {
                    if (event.getClient().getOwnerId() == event.getMember().getId() || event.getMember().isOwner()) {
                        event.reply(event.getClient().getError() + "There was an error loading songs.\n" +
                                "**Error content: " + e.getLocalizedMessage() + "**").queue();
                        StackTraceUtil.sendStackTrace(event.getTextChannel(), e);
                        return;
                    }

                    event.reply(event.getClient().getError() + " My list could not be created.:" + e.getLocalizedMessage()).queue();
                }
            } else {
                event.reply(event.getClient().getError() + " My List`" + pName + "` already exists").queue();
            }
        }
    }

    public static class DeletelistCmd extends MusicCommand {
        public DeletelistCmd(Bot bot) {
            super(bot);
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "Delete existing My List";
            this.arguments = "<name>";
            this.guildOnly = true;
            this.ownerCommand = false;

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "Playlist Name", true));
            this.options = options;
        }

        @Override
        public void doCommand(CommandEvent event) {

            String pName = event.getArgs().replaceAll("\\s+", "_");
            String userId = event.getAuthor().getId();
            if (!pName.equals("")) {
                if (bot.getMylistLoader().getPlaylist(userId, pName) == null)
                    event.reply(event.getClient().getError() + " My List does not exist:`" + pName + "`");
                else {
                    try {
                        bot.getMylistLoader().deletePlaylist(userId, pName);
                        event.reply(event.getClient().getSuccess() + " My list has been deleted:`" + pName + "`");
                    } catch (IOException e) {
                        event.reply(event.getClient().getError() + " Could not delete my list: " + e.getLocalizedMessage());
                    }
                }
            } else {
                event.reply(event.getClient().getError() + "Please include the name of your list");
            }
        }

        @Override
        public void doCommand(SlashCommandEvent event) {
            String pName = event.getOption("name").getAsString().replaceAll("\\s+", "_");
            String userId = event.getUser().getId();

            if (bot.getMylistLoader().getPlaylist(userId, pName) == null)
                event.reply(event.getClient().getError() + " My List does not exist:`" + pName + "`").queue();
            else {
                try {
                    bot.getMylistLoader().deletePlaylist(userId, pName);
                    event.reply(event.getClient().getSuccess() + " My list has been deleted:`" + pName + "`").queue();
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " Could not delete my list: " + e.getLocalizedMessage()).queue();
                }
            }
        }
    }

    public static class AppendlistCmd extends MusicCommand {
        public AppendlistCmd(Bot bot) {
            super(bot);
            this.name = "append";
            this.aliases = new String[]{"add"};
            this.help = "Add songs to an existing My List";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = true;
            this.ownerCommand = false;
            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "Playlist Name", true));
            options.add(new OptionData(OptionType.STRING, "url", "URL", true));
            this.options = options;
        }

        @Override
        public void doCommand(CommandEvent event) {

            String[] parts = event.getArgs().split("\\s+", 2);
            String userId = event.getAuthor().getId();
            if (parts.length < 2) {
                event.reply(event.getClient().getError() + " Please include the name and URL of the list you want to add it to. ");
                return;
            }
            String pName = parts[0];
            MylistLoader.Playlist playlist = bot.getMylistLoader().getPlaylist(userId, pName);
            if (playlist == null)
                event.reply(event.getClient().getError() + " My List does not exist:`" + pName + "`");
            else {
                StringBuilder builder = new StringBuilder();
                playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
                String[] urls = parts[1].split("\\|");
                for (String url : urls) {
                    String u = url.trim();
                    if (u.startsWith("<") && u.endsWith(">"))
                        u = u.substring(1, u.length() - 1);
                    builder.append("\r\n").append(u);
                }
                try {
                    bot.getMylistLoader().writePlaylist(userId, pName, builder.toString());
                    event.reply(event.getClient().getSuccess() + urls.length + " Item added to my list:`" + pName + "`");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " Could not add to my list: " + e.getLocalizedMessage());
                }
            }
        }

        @Override
        public void doCommand(SlashCommandEvent event) {
            String userId = event.getUser().getId();
            String pname = event.getOption("name").getAsString();
            MylistLoader.Playlist playlist = bot.getMylistLoader().getPlaylist(userId, pname);
            if (playlist == null)
                event.reply(event.getClient().getError() + " My List does not exist:`" + pname + "`").queue();
            else {
                StringBuilder builder = new StringBuilder();
                playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
                String[] urls = event.getOption("url").getAsString().split("\\|");
                for (String url : urls) {
                    String u = url.trim();
                    if (u.startsWith("<") && u.endsWith(">"))
                        u = u.substring(1, u.length() - 1);
                    builder.append("\r\n").append(u);
                }
                try {
                    bot.getMylistLoader().writePlaylist(userId, pname, builder.toString());
                    event.reply(event.getClient().getSuccess() + urls.length + " Item added to my list:`" + pname + "`").queue();
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " Could not add to my list: " + e.getLocalizedMessage()).queue();
                }
            }
        }
    }

    public static class ListCmd extends MusicCommand {
        public ListCmd(Bot bot) {
            super(bot);
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "View all available My Lists";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        public void doCommand(CommandEvent event) {
            String userId = event.getAuthor().getId();

            if (!bot.getMylistLoader().folderUserExists(userId))
                bot.getMylistLoader().createUserFolder(userId);
            if (!bot.getMylistLoader().folderUserExists(userId)) {
                event.reply(event.getClient().getWarning() + " Could not create My List folder because it does not exist.");
                return;
            }
            List<String> list = bot.getMylistLoader().getPlaylistNames(userId);
            if (list == null)
                event.reply(event.getClient().getError() + " Could not load my available list.");
            else if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " There are no playlists in the My Lists folder.");
            else {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available My Lists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }

        @Override
        public void doCommand(SlashCommandEvent event) {
            String userId = event.getUser().getId();

            if (!bot.getMylistLoader().folderUserExists(userId))
                bot.getMylistLoader().createUserFolder(userId);
            if (!bot.getMylistLoader().folderUserExists(userId)) {
                event.reply(event.getClient().getWarning() + " Could not create My List folder because it does not exist.").queue();
                return;
            }
            List<String> list = bot.getMylistLoader().getPlaylistNames(userId);
            if (list == null)
                event.reply(event.getClient().getError() + " Could not load my available list.").queue();
            else if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " There are no playlists in the My Lists folder.").queue();
            else {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available My Lists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString()).queue();
            }
        }
    }
}
