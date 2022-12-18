package com.johnymuffin.beta.discordauth;

import com.google.errorprone.annotations.CheckReturnValue;
import com.projectposeidon.api.PoseidonUUID;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.InteractionType;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.UUID;

public class DiscordAuthListener extends ListenerAdapter {
    private DiscordAuthentication plugin;


    public DiscordAuthListener(DiscordAuthentication plugin) {
        this.plugin = plugin;

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        String[] ags = event.getMessage().getContentRaw().split(" ");
        if (ags[0].toLowerCase().equals("!link")) {
            if (ags.length != 2) {

                event.getChannel().sendMessage("Incorrect Usage: \"!link username\"").queue();
                return;
            }
            //Start Verification
            Boolean found = false;
            Player p = null;
            if (plugin.getData().isDiscordIDAlreadyLinked(event.getAuthor().getId())) {
                event.getChannel().sendMessage("Sorry, this account has already been linked.\nYou can unlink with !unlink").queue();
                return;
            }


            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (ags[1].equalsIgnoreCase(p2.getName())) {
                    //Player is online
                    found = true;
                    p = p2;
                    break;
                }
            }
            if (!found) {
                event.getChannel().sendMessage("Could not find a player online with that username\nPlease ensure you are online and have specified the correct name").queue();
                return;
            }
            //Check we have a UUID in Storage
            UUID uuid = plugin.getPlayerUUID(p.getName());

            if (uuid == null) {
                event.getChannel().sendMessage("<@" + event.getAuthor().getId() + "> Sorry, we couldn't find a UUID linked to that username" +
                        "\nPlease ensure you are using a Premium account or try again later. If the issue persists please contact staff").queue();
                return;
            }


            if (plugin.getData().isUUIDAlreadyLinked(uuid.toString())) {
                event.getChannel().sendMessage("Sorry, this username is already linked to a Discord.\nYou can unlink in-game with `/discordauth unlink`").queue();
                return;
            }


            event.getChannel().sendMessage("<@" + event.getAuthor().getId() + "> I have direct messaged you info to continue the linking process" +
                    "\nPlease ensure you have Direct Messages enabled").queue();
            event.getAuthor().openPrivateChannel().queue((channel) ->
            {
                String securityCode = generateCode();
                channel.sendMessage("You have started the linking process" +
                        "\nPlease run the command \"/discordauth link " + securityCode + "\" in-game").queue();
                plugin.getCache().addCodeToken(uuid.toString(), securityCode, event.getAuthor().getId());
            });


        } else if (ags[0].toLowerCase().equals("!unlink")) {
            if (plugin.getData().isDiscordIDAlreadyLinked(event.getAuthor().getId())) {
                if (plugin.getData().removeLinkFromDiscordID(event.getAuthor().getId())) {
                    event.getChannel().sendMessage("This Discord has been unlinked from any account").queue();
                } else {
                    event.getChannel().sendMessage("An error occurred during unlinking. Please contact staff").queue();
                }
                return;
            } else {
                event.getChannel().sendMessage("This Discord account is currently not linked to any account").queue();
            }

        } else if (ags[0].toLowerCase().equals("!status")) {

            if (ags.length == 1) {
                if (plugin.getData().isDiscordIDAlreadyLinked(event.getAuthor().getId())) {

                    String uuid = plugin.getData().getUUIDFromDiscordID(event.getAuthor().getId());
                    String message = "We found the link details below";
                    message = message + "\nUUID: " + uuid;
                    String username = null;
                    if (plugin.isPoseidonPresent()) {
                        username = PoseidonUUID.getPlayerUsernameFromUUID(UUID.fromString(uuid));
                    }
                    if (username == null) username = "Unknown User";
                    message = message + "\nCurrent Username: " + username;
                    String finalMessage = message;

                    event.getChannel().sendMessage(finalMessage).queue();


                } else {
                    event.getChannel().sendMessage("This Discord account is currently not linked to any account").queue();
                }
                return;
            }
            if (ags.length > 2) {
                event.getChannel().sendMessage("Too many arguments! " +
                        "\nTo grab your link status just type \"!status\" " +
                        "\nTo grab another players link status, type \"!status {userID}\"").queue();
                return;
            }
                String lookup = ags[1];
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

                    event.getChannel().sendMessage(finalMessage).queue();


                } else {
                    event.getChannel().sendMessage("This Discord account is currently not linked to any account").queue();
                }
            }


        }



    private String generateCode() {
        Random rnd = new Random();
        int n = 100000 + rnd.nextInt(900000);
        return String.valueOf(n);
    }
}
