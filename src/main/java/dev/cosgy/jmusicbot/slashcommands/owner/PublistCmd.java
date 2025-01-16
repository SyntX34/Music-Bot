package dev.cosgy.jmusicbot.slashcommands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import dev.cosgy.jmusicbot.playlist.MylistLoader;
import dev.cosgy.jmusicbot.playlist.PubliclistLoader;
import dev.cosgy.jmusicbot.playlist.PubliclistLoader.Playlist;
import dev.cosgy.jmusicbot.slashcommands.OwnerCommand;
import dev.cosgy.jmusicbot.slashcommands.admin.AutoplaylistCmd;
import dev.cosgy.jmusicbot.slashcommands.music.MylistCmd;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kosugikun
 */
public class PublistCmd extends OwnerCommand {
    private final Bot bot;

    public PublistCmd(Bot bot) {
        this.bot = bot;
        this.guildOnly = false;
        this.name = "publist";
        this.arguments = "<append|delete|make|all|show>";
        this.help = "Playlist Management";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.children = new OwnerCommand[]{
                new ListCmd(),
                new AppendlistCmd(),
                new DeletelistCmd(),
                new MakelistCmd(),
                new ShowTracksCmd()
        };
    }

    @Override
    protected void execute(SlashCommandEvent slashCommandEvent) {

    }

    @Override
    public void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Playlist Management Commands:\n");
        for (Command cmd : this.children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments() == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.reply(builder.toString());
    }

    public static class DefaultlistCmd extends AutoplaylistCmd {
        public DefaultlistCmd(Bot bot) {
            super(bot);
            this.name = "setdefault";
            this.aliases = new String[]{"default"};
            this.arguments = "<playlistname|NONE>";
            this.guildOnly = true;
        }
    }

    public class ShowTracksCmd extends OwnerCommand {
        public ShowTracksCmd() {
            this.name = "show";
            this.help = "Display songs in a specified playlist";
            this.arguments = "<name>";
            this.guildOnly = false;

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "Playlist Name", true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String playlistName = event.getOption("name").getAsString();

            PubliclistLoader.Playlist playlist = bot.getPublistLoader().getPlaylist(playlistName);
            if (playlist == null) {
                event.reply(event.getClient().getError() + " `Playlist " + playlistName + "`was not found.").queue();
                return;
            }

            if (playlist.getItems().isEmpty()) {
                event.reply(event.getClient().getWarning() + " Playlist `" + playlistName + "` has no songs.").queue();
                return;
            }

            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + "Playlist" + playlistName + " Songs in:\n");
            for (int i = 0; i < playlist.getItems().size(); i++) {
                builder.append(i + 1).append(". ").append(playlist.getItems().get(i)).append("\n");
            }

            if (builder.length() > 2000) {
                builder.setLength(1997); // Discord Supports message limits
                builder.append("...");
            }

            event.reply(builder.toString()).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            String playlistName = event.getArgs().trim();

            if (playlistName.isEmpty()) {
                event.reply(event.getClient().getError() + " Specify the playlist name.");
                return;
            }

            PubliclistLoader.Playlist playlist = bot.getPublistLoader().getPlaylist(playlistName);
            if (playlist == null) {
                event.reply(event.getClient().getError() + " Playlist" + playlistName + " was not found.");
                return;
            }

            if (playlist.getItems().isEmpty()) {
                event.reply(event.getClient().getWarning() + " Playlist" + playlistName + " There are no songs on .");
                return;
            }

            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Playlist" + playlistName + " Songs in:\n");
            for (int i = 0; i < playlist.getItems().size(); i++) {
                builder.append(i + 1).append(". ").append(playlist.getItems().get(i)).append("\n");
            }

            if (builder.length() > 2000) {
                builder.setLength(1997); // Discord Supports message limits
                builder.append("...");
            }

            event.reply(builder.toString());
        }
    }

    public class MakelistCmd extends OwnerCommand {
        public MakelistCmd() {
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "Create a new playlist";
            this.arguments = "<name>";
            this.guildOnly = false;

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "Playlist Name", true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String pname = event.getOption("name").getAsString().replaceAll("\\s+", "_");
            if (bot.getPublistLoader().getPlaylist(pname) == null) {
                try {
                    bot.getPublistLoader().createPlaylist(pname);
                    event.reply(event.getClient().getSuccess() + " `" + pname + "`I created a playlist called!").queue();
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " The playlist could not be created.:" + e.getLocalizedMessage()).queue();
                }
            } else
                event.reply(event.getClient().getError() + " Playlist`" + pname + "` already exists!").queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            if (bot.getPublistLoader().getPlaylist(pname) == null) {
                try {
                    bot.getPublistLoader().createPlaylist(pname);
                    event.reply(event.getClient().getSuccess() + " `" + pname + "`I created a playlist called!");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " The playlist could not be created.:" + e.getLocalizedMessage());
                }
            } else
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` already exists!");
        }
    }

    public class DeletelistCmd extends OwnerCommand {
        public DeletelistCmd() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "Delete an existing playlist";
            this.arguments = "<name>";
            this.guildOnly = false;

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "Playlist Name", true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String pname = event.getOption("name").getAsString().replaceAll("\\s+", "_");
            if (bot.getPublistLoader().getPlaylist(pname) == null)
                event.reply(event.getClient().getError() + " Playlist`" + pname + "` does not exist!").queue();
            else {
                try {
                    bot.getPublistLoader().deletePlaylist(pname);
                    event.reply(event.getClient().getSuccess() + " Playlist`" + pname + "`has been deleted.!").queue();
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " Failed to delete playlist: " + e.getLocalizedMessage()).queue();
                }
            }
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            if (bot.getPublistLoader().getPlaylist(pname) == null)
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` does not exist!");
            else {
                try {
                    bot.getPublistLoader().deletePlaylist(pname);
                    event.reply(event.getClient().getSuccess() + " Playlist `" + pname + "`I have deleted it!");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " Failed to delete playlist: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public class AppendlistCmd extends OwnerCommand {
        public AppendlistCmd() {
            this.name = "append";
            this.aliases = new String[]{"add"};
            this.help = "Add songs to an existing playlist";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = false;
            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "name", "Playlist Name", true));
            options.add(new OptionData(OptionType.STRING, "url", "URL", true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String pname = event.getOption("name").getAsString();
            Playlist playlist = bot.getPublistLoader().getPlaylist(pname);
            if (playlist == null)
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` does not exist!").queue();
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
                    bot.getPublistLoader().writePlaylist(pname, builder.toString());
                    event.reply(event.getClient().getSuccess() + urls.length + " Playlist Item `" + pname + "Added to!").queue();
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " Could not add to playlist:" + e.getLocalizedMessage()).queue();
                }
            }
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] parts = event.getArgs().split("\\s+", 2);
            if (parts.length < 2) {
                event.reply(event.getClient().getError() + " Include the playlist name and URL you want to add to.");
                return;
            }
            String pname = parts[0];
            Playlist playlist = bot.getPublistLoader().getPlaylist(pname);
            if (playlist == null)
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` does not exist!");
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
                    bot.getPublistLoader().writePlaylist(pname, builder.toString());
                    event.reply(event.getClient().getSuccess() + urls.length + " Playlist Item `" + pname + "`Added to !");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " Could not add to playlist: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public class ListCmd extends OwnerCommand {
        public ListCmd() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "View all available playlists.";
            this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (!bot.getPublistLoader().folderExists())
                bot.getPublistLoader().createFolder();
            if (!bot.getPublistLoader().folderExists()) {
                event.reply(event.getClient().getWarning() + " The playlist folder could not be created because it does not exist.").queue();
                return;
            }
            List<String> list = bot.getPublistLoader().getPlaylistNames();
            if (list == null)
                event.reply(event.getClient().getError() + " Could not load available playlists.").queue();
            else if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " There are no playlists in the playlist folder.").queue();
            else {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString()).queue();
            }
        }

        @Override
        protected void execute(CommandEvent event) {
            if (!bot.getPublistLoader().folderExists())
                bot.getPublistLoader().createFolder();
            if (!bot.getPublistLoader().folderExists()) {
                event.reply(event.getClient().getWarning() + " The playlist folder could not be created because it does not exist.");
                return;
            }
            List<String> list = bot.getPublistLoader().getPlaylistNames();
            if (list == null)
                event.reply(event.getClient().getError() + " Could not load available playlists.");
            else if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " There are no playlists in the playlist folder.");
            else {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available Playlists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }
}
