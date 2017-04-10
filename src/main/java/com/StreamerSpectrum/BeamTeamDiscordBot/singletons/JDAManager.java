package com.StreamerSpectrum.BeamTeamDiscordBot.singletons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;

import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.RandomMember;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.admin.RestartConstellation;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel.ChannelRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel.ChannelAdd;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.channel.ChannelList;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive.GoLiveDeleteOffline;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive.GoLiveRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.golive.GoLiveSet;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.log.LogRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.log.LogSet;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamAdd;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamList;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.team.TeamRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.teamrole.TeamRoleDistribute;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.teamrole.TeamRoleList;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.teamrole.TeamRoleRemove;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.teamrole.TeamRoleSet;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBListener;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.BTBRole;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.resource.GoLiveMessage;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.MemberInfo;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.MemberList;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BTBBeamChannel;
import com.StreamerSpectrum.BeamTeamDiscordBot.beam.resource.BeamTeamUser;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.CommandHelper;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.FollowReport;
import com.StreamerSpectrum.BeamTeamDiscordBot.discord.command.PrimaryTeam;

import me.jagrosh.jdautilities.commandclient.CommandClient;
import me.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import pro.beam.api.util.Enums;

public abstract class JDAManager {

	private static JDA				jda;
	private static CommandClient	commandClient;
	private static EventWaiter		waiter;

	public static JDA getJDA() {
		if (null == jda) {
			try (BufferedReader br = new BufferedReader(new FileReader(new File("resources/config.txt")))) {
				String botToken = br.readLine();

				jda = new JDABuilder(AccountType.BOT).setToken(botToken).setStatus(OnlineStatus.DO_NOT_DISTURB)
						.setGame(Game.of("loading...")).addListener(getWaiter()).addListener(getCommandClient())
						.addListener(new BTBListener()).buildAsync();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RateLimitedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return jda;
	}

	private static CommandClient getCommandClient() {
		if (null == commandClient) {
			commandClient = new CommandClientBuilder().useDefaultGame().setPrefix("!btb ")
					.addCommands(new TeamAdd(), new TeamRemove(), new TeamList(), new RandomMember(), new PrimaryTeam(),
							new MemberInfo(), new FollowReport(), new MemberList(), new GoLiveSet(), new GoLiveRemove(),
							new GoLiveDeleteOffline(), new ChannelAdd(), new ChannelRemove(), new ChannelList(),
							new TeamRoleSet(), new TeamRoleRemove(), new TeamRoleList(), new TeamRoleDistribute(),
							new LogSet(), new LogRemove(), new RestartConstellation())
					.build();
		}

		return commandClient;
	}

	public static EventWaiter getWaiter() {
		if (null == waiter) {
			waiter = new EventWaiter();
		}

		return waiter;
	}

	public static void sendGoLiveMessage(String channelID, MessageEmbed embed, BTBBeamChannel channel) {
		getJDA().getTextChannelById(channelID).sendMessage(embed).queue(new Consumer<Message>() {

			@Override
			public void accept(Message t) {
				DbManager.createGoLiveMessage(new GoLiveMessage(t.getId(), t.getChannel().getId(),
						Long.parseLong(t.getGuild().getId()), channel.id));

			}
		});
	}

	public static void sendMessage(String channelID, Message msg) {
		getJDA().getTextChannelById(channelID).sendMessage(msg).queue();
	}

	public static void sendMessage(String channelID, MessageEmbed embed) {
		getJDA().getTextChannelById(channelID).sendMessage(embed).queue();
	}

	public static void sendMessage(String channelID, String text) {
		getJDA().getTextChannelById(channelID).sendMessage(text).queue();
	}

	public static void sendMessage(String channelID, String format, Object... args) {
		getJDA().getTextChannelById(channelID).sendMessage(format, args).queue();
	}

	public static void sendMessage(CommandEvent event, Message msg) {
		sendMessage(event.getChannel().getId(), msg);
	}

	public static void sendMessage(CommandEvent event, MessageEmbed embed) {
		sendMessage(event.getChannel().getId(), embed);
	}

	public static void sendMessage(CommandEvent event, String text) {
		sendMessage(event.getChannel().getId(), text);
	}

	public static void sendMessage(CommandEvent event, String format, Object... args) {
		sendMessage(event.getChannel().getId(), format, args);
	}

	public static void deleteMessage(GoLiveMessage message) {
		deleteMessage(message.getMessageID(), message.getGoLiveChannelID());
	}

	public static void deleteMessage(String messageID, String goLiveChannelID) {
		getJDA().getTextChannelById(goLiveChannelID).deleteMessageById(messageID).queue();
	}

	public static MessageEmbed buildGoLiveEmbed(BTBBeamChannel channel) {
		return new EmbedBuilder()
				.setTitle(String.format("%s is now live!", channel.user.username),
						String.format("https://beam.pro/%s", channel.user.username))
				.setThumbnail(String.format("https://beam.pro/api/v1/users/%d/avatar?_=%d", channel.user.id,
						new Random().nextInt()))
				.setDescription(StringUtils.isBlank(channel.user.bio) ? "No bio" : channel.user.bio)
				.addField(channel.name, channel.type == null ? "No game selected" : channel.type.name, false)
				.addField("Followers", Integer.toString(channel.numFollowers), true)
				.addField("Views", Integer.toString(channel.viewersTotal), true)
				.addField("Rating", Enums.serializedName(channel.audience), true)
				.setImage(String.format("https://thumbs.beam.pro/channel/%d.small.jpg?_=%d", channel.id,
						new Random().nextInt()))
				.setFooter("Beam.pro", CommandHelper.BEAM_LOGO_URL).setTimestamp(Instant.now())
				.setColor(CommandHelper.COLOR).build();
	}

	public static PrivateChannel getPrivateChannel(User user) {
		if (!user.hasPrivateChannel()) {
			user.openPrivateChannel();
		}

		return user.getPrivateChannel();
	}

	public static void sendDM(CommandEvent event, Message msg) {
		getPrivateChannel(event.getAuthor()).sendMessage(msg);
	}

	public static void sendDM(CommandEvent event, MessageEmbed embed) {
		getPrivateChannel(event.getAuthor()).sendMessage(embed);
	}

	public static void sendDM(CommandEvent event, String text) {
		getPrivateChannel(event.getAuthor()).sendMessage(text);
	}

	public static void sendDM(CommandEvent event, String format, Object... args) {
		getPrivateChannel(event.getAuthor()).sendMessage(format, args);
	}

	public static void giveTeamRoleToUser(BTBRole role, User user) {
		if (null != user) {
			Guild guild = getJDA().getGuildById(Long.toString(role.getGuildID()));
			Member member = guild.getMember(user);

			if (null != member) {
				guild.getController().addRolesToMember(member, guild.getRoleById(role.getRoleID())).queue();
			}
		}
	}

	public static void giveTeamRoleToUserOnAllGuilds(int teamID, User user) {
		List<BTBRole> roles = DbManager.readTeamRolesForTeam(teamID);

		for (BTBRole role : roles) {
			giveTeamRoleToUser(role, user);
		}
	}

	public static void distributeTeamRoleToGuildTeamMembers(BTBRole role) {
		List<BeamTeamUser> members = BeamManager.getTeamMembers(role.getTeamID());

		for (BeamTeamUser member : members) {
			if (null != member.social && StringUtils.isNotBlank(member.social.discord)) {
				giveTeamRoleToUser(role, getUserForDiscordTag(member.social.discord));
			}
		}
	}

	public static void removeTeamRoleFromUser(BTBRole role, User user) {
		Guild guild = getJDA().getGuildById(Long.toString(role.getGuildID()));
		Member member = guild.getMember(user);

		if (null != member) {
			guild.getController().removeRolesFromMember(member, guild.getRoleById(role.getRoleID())).queue();
		}
	}

	public static void removeTeamRoleFromUserOnAllGuilds(int teamID, User user) {
		List<BTBRole> roles = DbManager.readTeamRolesForTeam(teamID);

		for (BTBRole role : roles) {
			removeTeamRoleFromUser(role, user);
		}
	}

	public static User getUserForDiscordTag(String tag) {
		List<User> potentialUsers = getJDA().getUsersByName(tag.substring(0, tag.indexOf("#")), true);

		for (User user : potentialUsers) {
			if (StringUtils.contains(tag, user.getDiscriminator())) {
				return user;
			}
		}

		return null;
	}

}
