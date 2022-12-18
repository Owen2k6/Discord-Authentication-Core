package com.johnymuffin.beta.discordauth;

import com.projectposeidon.api.PoseidonUUID;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class DiscordAuthListener extends ListenerAdapter {
    private DiscordAuthentication plugin;


    public DiscordAuthListener(DiscordAuthentication plugin) {
        this.plugin = plugin;

    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("link")) {
            //event.reply("Hello!").setEphemeral(true).queue();
            String linkto = String.valueOf(event.getOption("username"));
            Boolean found = false;
            Player p = null;
            if (plugin.getData().isDiscordIDAlreadyLinked(event.getUser().getId())) {
                event.reply("This Discord account is already linked to an MC account.\nYou can unlink using /unlink\nYou can see your link status with /status").setEphemeral(true).queue();
                return;
            }
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (linkto.equalsIgnoreCase(p2.getName())) {
                    //Player is online
                    found = true;
                    p = p2;
                    break;
                }
            }
            if (!found) {
                event.reply("Could not find a player online with that username\nPlease ensure you are online and have specified the correct name").setEphemeral(true).queue();
                return;
            }
            //Check we have a UUID in Storage
            UUID uuid = plugin.getPlayerUUID(p.getName());

            if (uuid == null) {
                event.reply("Sorry, we couldn't find a UUID linked to that username" + "\nPlease ensure you are using a Premium account or try again later. If the issue persists please contact staff").setEphemeral(true).queue();
                return;
            }
            if (plugin.getData().isUUIDAlreadyLinked(uuid.toString())) {
                event.reply("Sorry, this username is already linked to a Discord.\nYou can unlink in-game with `/discordauth unlink`").setEphemeral(true).queue();
                return;
            }
            String securityCode = generateCode();
            event.reply("You have started the linking process" + "\nPlease run the command \"/discordauth link " + securityCode + "\" in-game").setEphemeral(true).queue();
            plugin.getCache().addCodeToken(uuid.toString(), securityCode, event.getUser().getId());
        } else if (event.getName().equals("unlink")) {
            //event.reply("Hello!").setEphemeral(true).queue();
            if (plugin.getData().isDiscordIDAlreadyLinked(event.getUser().getId())) {
                if (plugin.getData().removeLinkFromDiscordID(event.getUser().getId())) {
                    event.reply("This Discord has been unlinked from any account").setEphemeral(true).queue();
                } else {
                    event.reply("An error occurred during unlinking. Please contact staff").setEphemeral(true).queue();
                }
                return;
            } else {
                event.reply("This Discord account is currently not linked to any account").setEphemeral(true).queue();
            }
        } else if (event.getName().equals("status")) {
            //event.reply("Hello!").setEphemeral(true).queue();
            //event.getOption("user").getAsUser().getId();
            if (!event.getOption("user").toString().isEmpty()) {
                if (plugin.getData().isDiscordIDAlreadyLinked(event.getOption("user").getAsUser().getId())) {

                    String uuid = plugin.getData().getUUIDFromDiscordID(event.getOption("user").getAsUser().getId());
                    String message = "We found the link details on this user below";
                    message = message + "\nUUID: " + uuid;
                    String username = null;
                    if (plugin.isPoseidonPresent()) {
                        username = PoseidonUUID.getPlayerUsernameFromUUID(UUID.fromString(uuid));
                    }
                    if (username == null) username = "Unknown User";
                    message = message + "\nCurrent Username: " + username;
                    String finalMessage = message;

                    event.reply(finalMessage).setEphemeral(true).queue();


                } else {
                    event.reply("This member's account is currently not linked to any account").setEphemeral(true).queue();
                }
                return;
            } else {
                String lookup = event.getOption("user").getAsUser().getId();
                if (plugin.getData().isDiscordIDAlreadyLinked(lookup)) {

                    String uuid = plugin.getData().getUUIDFromDiscordID(lookup);
                    String message = "We found the link details below";
                    message = message + "\nUUID: " + uuid;
                    String username = null;
                    if (plugin.isPoseidonPresent()) {
                        username = PoseidonUUID.getPlayerUsernameFromUUID(UUID.fromString(uuid));
                    }
                    if (username == null) username = "Unknown User";
                    message = message + "\nCurrent Username: " + username;
                    String finalMessage = message;

                    event.reply(finalMessage).setEphemeral(true).queue();


                } else {
                    event.reply("This Discord account is currently not linked to any account").setEphemeral(true).queue();
                }
            }
        }
    }

    private String generateCode() {
        Random rnd = new Random();
        int n = 100000 + rnd.nextInt(900000);
        return String.valueOf(n);
    }
}
