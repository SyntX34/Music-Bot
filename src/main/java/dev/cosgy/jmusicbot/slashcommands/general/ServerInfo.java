package dev.cosgy.jmusicbot.slashcommands.general;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ServerInfo extends SlashCommand {
    public ServerInfo(Bot bot) {
        this.name = "serverinfo";
        this.help = "Shows information about the server";
        this.guildOnly = true;
        this.category = new Category("General");
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String GuildName = event.getGuild().getName();
        String GuildIconURL = event.getGuild().getIconUrl();
        String GuildId = event.getGuild().getId();
        String GuildOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getName() + "#" + event.getGuild().getOwner().getUser().getDiscriminator();
        String GuildCreatedDate = event.getGuild().getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

        String GuildRolesCount = String.valueOf(event.getGuild().getRoles().size());
        String GuildMember = String.valueOf(event.getGuild().getMembers().size());
        String GuildCategoryCount = String.valueOf(event.getGuild().getCategories().size());
        String GuildTextChannelCount = String.valueOf(event.getGuild().getTextChannels().size());
        String GuildVoiceChannelCount = String.valueOf(event.getGuild().getVoiceChannels().size());
        String GuildStageChannelCount = String.valueOf(event.getGuild().getStageChannels().size());
        String GuildForumChannelCount = String.valueOf(event.getGuild().getForumChannels().size());
        String GuildLocation = event.getGuild().getLocale().getNativeName();
                /*
                .replace("japan", ":flag_jp: Japan")
                .replace("singapore", ":flag_sg: Singapore")
                .replace("hongkong", ":flag_hk: Hong Kong")
                .replace("Brazil", ":flag_br: Brazil")
                .replace("us-central", ":flag_us: Central America")
                .replace("us-west", ":flag_us: West America")
                .replace("us-east", ":flag_us: East America")
                .replace("us-south", ":flag_us: South America")
                .replace("sydney", ":flag_au: Sydney")
                .replace("eu-west", ":flag_eu: West Europe")
                .replace("eu-central", ":flag_eu: Central Europe")
                .replace("russia", ":flag_ru: Russia");
                 */

        EmbedBuilder eb = new EmbedBuilder();

        eb.setAuthor("Server " + GuildName + " info", null, GuildIconURL);

        eb.addField("Server ID", GuildId, true);
        eb.addField("Server Primary Language", GuildLocation, true);
        eb.addField("Server Owner", GuildOwner, true);
        eb.addField("Number of Members", GuildMember, true);
        eb.addField("Number of Roles", GuildRolesCount, true);
        eb.addField("Number of Categories", GuildCategoryCount, true);
        eb.addField("Number of Text Channels", GuildTextChannelCount, true);
        eb.addField("Number of Voice Channels", GuildVoiceChannelCount, true);
        eb.addField("Number of Stage Channels", GuildStageChannelCount, true);
        eb.addField("Number of Forum Channels", GuildForumChannelCount, true);

        eb.setFooter("Server creation date: " + GuildCreatedDate, null);

        event.replyEmbeds(eb.build()).queue();
    }

    @Override
    public void execute(CommandEvent event) {
        String GuildName = event.getGuild().getName();
        String GuildIconURL = event.getGuild().getIconUrl();
        String GuildId = event.getGuild().getId();
        String GuildOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getName() + "#" + event.getGuild().getOwner().getUser().getDiscriminator();
        String GuildCreatedDate = event.getGuild().getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

        String GuildRolesCount = String.valueOf(event.getGuild().getRoles().size());
        String GuildMember = String.valueOf(event.getGuild().getMembers().size());
        String GuildCategoryCount = String.valueOf(event.getGuild().getCategories().size());
        String GuildTextChannelCount = String.valueOf(event.getGuild().getTextChannels().size());
        String GuildVoiceChannelCount = String.valueOf(event.getGuild().getVoiceChannels().size());
        String GuildStageChannelCount = String.valueOf(event.getGuild().getStageChannels().size());
        String GuildForumChannelCount = String.valueOf(event.getGuild().getForumChannels().size());
        String GuildLocation = event.getGuild().getLocale().getNativeName();
            /*.replace("japan", ":flag_jp: Japan")
            .replace("singapore", ":flag_sg: Singapore")
            .replace("hongkong", ":flag_hk: Hong Kong")
            .replace("Brazil", ":flag_br: Brazil")
            .replace("us-central", ":flag_us: Central America")
            .replace("us-west", ":flag_us: West America")
            .replace("us-east", ":flag_us: East America")
            .replace("us-south", ":flag_us: South America")
            .replace("sydney", ":flag_au: Sydney")
            .replace("eu-west", ":flag_eu: West Europe")
            .replace("eu-central", ":flag_eu: Central Europe")
            .replace("russia", ":flag_ru: Russia");*/


        EmbedBuilder eb = new EmbedBuilder();

        eb.setAuthor("Server " + GuildName + " info", null, GuildIconURL);

        eb.addField("Server ID", GuildId, true);
        eb.addField("Server Primary Language", GuildLocation, true);
        eb.addField("Server Owner", GuildOwner, true);
        eb.addField("Number of Members", GuildMember, true);
        eb.addField("Number of Roles", GuildRolesCount, true);
        eb.addField("Number of Categories", GuildCategoryCount, true);
        eb.addField("Number of Text Channels", GuildTextChannelCount, true);
        eb.addField("Number of Voice Channels", GuildVoiceChannelCount, true);
        eb.addField("Number of Stage Channels", GuildStageChannelCount, true);
        eb.addField("Number of Forum Channels", GuildForumChannelCount, true);

        eb.setFooter("Server Created Date: " + GuildCreatedDate, null);

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}
