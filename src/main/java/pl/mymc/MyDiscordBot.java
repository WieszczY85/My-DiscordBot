package pl.mymc;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MyDiscordBot extends ListenerAdapter {
    private static final String DELETE_CHANNEL_ID = "1259487263407210547"; // ID kanału do usuwania wiadomości
    private static final String ROLE_ASSIGN_CHANNEL_ID = "798568822583197707"; // ID kanału do przydzielania roli
    private static final String ROLE_ASSIGN_MESSAGE_ID = "1266347621270818948"; // ID wiadomości do przydzielania roli
    private static final String ROLE_ID = "771003077771526164"; // ID roli do przydzielenia

    private static final String SECOND_ROLE_ASSIGN_CHANNEL_ID = "771004333303005204"; // ID drugiego kanału do przydzielania roli
    private static final String SECOND_ROLE_ASSIGN_MESSAGE_ID = "1266323773594931230"; // ID drugiej wiadomości do przydzielania roli
    private static final String FIRST_REACTION_ROLE_ID = "798559833829015613"; // ID pierwszej roli do przydzielenia na drugim kanale
    private static final String SECOND_REACTION_ROLE_ID = "798559842402435118"; // ID drugiej roli do przydzielenia na drugim kanale

    private static final String token = "x";

    public static void main(String[] args) {
        JDABuilder.createDefault(token)

                .addEventListeners(new MyDiscordBot())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            Message message = event.getMessage();
            String content = message.getContentRaw();
            String channelId = event.getChannel().getId();

            // Usuwanie wiadomości tylko na wybranym kanale
            if (channelId.equals(DELETE_CHANNEL_ID)) {
                deleteMessageAfterDelay(message);
            }

            // Obsługa komend
            if (content.startsWith("!")) {
                handleCommand(event, content);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        String messageId = event.getMessageId();
        String channelId = event.getChannel().getId();

        if (messageId.equals(ROLE_ASSIGN_MESSAGE_ID) && channelId.equals(ROLE_ASSIGN_CHANNEL_ID)) {
            Role role = event.getGuild().getRoleById(ROLE_ID);
            if (role != null) {
                event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getUserId()), role).queue();
            }
        } else if (messageId.equals(SECOND_ROLE_ASSIGN_MESSAGE_ID) && channelId.equals(SECOND_ROLE_ASSIGN_CHANNEL_ID)) {
            Role roleToAssign = null;
            String reactionEmote = event.getEmoji().getName();

            if (reactionEmote.equals("🇵🇱")) {
                roleToAssign = event.getGuild().getRoleById(FIRST_REACTION_ROLE_ID);
            } else if (reactionEmote.equals("\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F")) { // England flag unicode sequence
                roleToAssign = event.getGuild().getRoleById(SECOND_REACTION_ROLE_ID);
            }

            if (roleToAssign != null) {
                event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getUserId()), roleToAssign).queue();
            }
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        String messageId = event.getMessageId();
        String channelId = event.getChannel().getId();

        if (messageId.equals(SECOND_ROLE_ASSIGN_MESSAGE_ID) && channelId.equals(SECOND_ROLE_ASSIGN_CHANNEL_ID)) {
            Role roleToRemove = null;
            String reactionEmote = event.getEmoji().getName();

            if (reactionEmote.equals("🇵🇱")) {
                roleToRemove = event.getGuild().getRoleById(FIRST_REACTION_ROLE_ID);
            } else if (reactionEmote.equals("\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F")) { // England flag unicode sequence
                roleToRemove = event.getGuild().getRoleById(SECOND_REACTION_ROLE_ID);
            }

            if (roleToRemove != null) {
                event.getGuild().removeRoleFromMember(UserSnowflake.fromId(event.getUserId()), roleToRemove).queue();
            }
        }
    }

    // Metoda obsługująca komendy
    private void handleCommand(MessageReceivedEvent event, String content) {
        // Sprawdzenie uprawnień administratora
        if (!hasAdministratorPermission(event)) {
            event.getChannel().sendMessage("Nie masz uprawnień do używania tej komendy.").queue(
                    this::deleteMessageAfterDelayIfInChannel
            );
            return;
        }

        // Obsługa komendy !ping
        switch (content.split(" ")[0]) {
            case "!test" -> event.getChannel().sendMessage("Bot uruchomiony").queue(this::deleteMessageAfterDelayIfInChannel);

            // Obsługa komendy !update
            case "!update" -> {
                boolean success = updateBot();
                if (success) {
                    event.getChannel().sendMessage("JAR file updated and server restarted successfully.").queue(this::deleteMessageAfterDelayIfInChannel);
                } else {
                    event.getChannel().sendMessage("Failed to update JAR file.").queue(this::deleteMessageAfterDelayIfInChannel);
                }
            }

            // Obsługa komendy !exit
            case "!exit" -> {
                event.getChannel().sendMessage("Shutting down the bot...").queue(this::deleteMessageAfterDelayIfInChannel);
                System.exit(0); // Wyjście z aplikacji
            }

            // Obsługa komendy !komendy
            case "!komendy" -> sendCommandList(event);

            // Obsługa komendy !say
            case "!say" -> {
                String messageToSay = content.substring("!say".length()).trim();
                if (!messageToSay.isEmpty()) {
                    event.getChannel().sendMessage(messageToSay).queue();
                } else {
                    event.getChannel().sendMessage("Użycie: !say <wiadomość>").queue(this::deleteMessageAfterDelayIfInChannel);
                }
            }
        }
    }

    // Metoda wysyłająca listę komend
    private void sendCommandList(MessageReceivedEvent event) {
        String builder = """
                **Lista komend:**
                !test - Wysyła wiadomość 'Bot uruchomiony'.
                !update - Aktualizuje plik JAR.
                !exit - Wyłącza bota.
                !say <wiadomość> - Wysyła wiadomość jako bot.
                """;
        event.getChannel().sendMessage(builder).queue(
                sentMessage -> deleteMessageAfterDelayIfInChannel(sentMessage, 30)
        );
    }

    // Metoda sprawdzająca uprawnienia administratora użytkownika
    private boolean hasAdministratorPermission(MessageReceivedEvent event) {
        return event.getMember().hasPermission(Permission.ADMINISTRATOR);
    }

    // Metoda usuwająca wiadomość po określonym czasie
    private void deleteMessageAfterDelay(Message message) {
        message.delete().queueAfter(172800, TimeUnit.SECONDS);
    }

    // Metoda usuwająca wiadomość po określonym czasie tylko na wybranym kanale
    private void deleteMessageAfterDelayIfInChannel(Message message) {
        deleteMessageAfterDelayIfInChannel(message, 172800);
    }

    // Metoda usuwająca wiadomość po określonym czasie tylko na wybranym kanale
    private void deleteMessageAfterDelayIfInChannel(Message message, int delaySeconds) {
        if (message.getChannel().getId().equals(DELETE_CHANNEL_ID)) {
            message.delete().queueAfter(delaySeconds, TimeUnit.SECONDS);
        }
    }

    // Metoda aktualizująca plik JAR za pomocą biblioteki GitCheck która załatwie wiele rzeczy zamiast sie męczyć z cholernym kodem
    private boolean updateBot() {
        URL pomUrl = Thread.currentThread().getContextClassLoader().getResource("META-INF/maven/pl.mymc/My-DiscordBot/pom.properties");
        //Debug (można usunąć) System.out.println(pomUrl);

        if (pomUrl != null) {
            Properties prop = new Properties();
            try (InputStream pomInput = pomUrl.openStream()) {
                prop.load(pomInput);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String botName = prop.getProperty("artifactId");
            String currentVersion = prop.getProperty("version");

            // Replace with your checkForUpdates and autoDownloadUpdates values
            boolean checkForUpdates = true;
            boolean autoDownloadUpdates = true;


            return true;
        }
        return false;
    }
}