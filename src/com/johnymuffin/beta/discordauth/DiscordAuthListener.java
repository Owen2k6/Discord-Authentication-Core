package com.johnymuffin.beta.discordauth;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

import static com.johnymuffin.uuidcore.UUIDAPI.getUserName;
import static com.johnymuffin.uuidcore.UUIDAPI.getUserUUID;

public class DiscordAuthListener extends ListenerAdapter {
    private DiscordAuthentication plugin;


    public DiscordAuthListener(DiscordAuthentication plugin) {
        this.plugin = plugin;

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake()) return;
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
                event.getChannel().sendMessage("Sorry, this account has already been linked.\nYou can unlike with !unlink").queue();
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
            UUID uuid = getUserUUID(p.getName(), false);


            if (uuid == null) {
                event.getChannel().sendMessage("<@" + event.getAuthor().getId() + "> Sorry, we couldn't find a UUID linked to that username" +
                        "\nPlease ensure you are using a Premium account or try again later. If the issue persists please contact staff").queue();
                return;
            }


            if (plugin.getData().isUUIDAlreadyLinked(uuid.toString())) {
                event.getChannel().sendMessage("Sorry, this username is already linked to a Discord.\nYou can unlink in-game with /discord unlink").queue();
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


            if (plugin.getData().isDiscordIDAlreadyLinked(event.getAuthor().getId())) {

                Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        String uuid = plugin.getData().getUUIDFromDiscordID(event.getAuthor().getId());
                        String message = "We found the link details below";
                        message = message + "\nUUID: " + uuid;
                        message = message + "\nInitial Link Username: " + plugin.getData().getInitialUsernameFromDiscordID(event.getAuthor().getId());
                        message = message + "\nCurrent Username: " + getUserName(UUID.fromString(uuid), true);
                        String finalMessage = message;
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {

                                event.getChannel().sendMessage(finalMessage).queue();


                            }
                        }, 0L);
                    }
                }, 0L);


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
