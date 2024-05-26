package com.thenexusreborn.discordbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class DiscordListener extends ListenerAdapter {
    
    @Override
    public void onReady(ReadyEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            guild.updateCommands().addCommands(Commands.slash("mclink", "Link your Discord Account to your Minecraft Account.")).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        event.getHook().setEphemeral(true);
        if (event.getName().equals("mclink")) {
            event.deferReply().setEphemeral(true).queue();
            NexusBot.linkChannels.put(event.getMember().getId(), new Pair<>(event.getGuild().getId(), event.getChannelId()));
            NexusBot.socket.sendMessage("link " + event.getMember().getId(), "linkcode " + event.getMember().getId(), codeCmd -> {
                event.getHook().sendMessage("Please type /verify " + codeCmd.split(" ")[2] + " on the server to finish linking.").setEphemeral(true).queue();
            });
        }
    }
}
