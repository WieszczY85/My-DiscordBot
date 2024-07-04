package pl.mymc;

import com.sun.tools.javac.Main;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MyDiscordBot extends ListenerAdapter {
    private static final String CHANNEL_ID = "1257994853024010351"; // Replace with your channel ID

    public static void main(String[] args) throws LoginException {
        String token = "token";
        JDABuilder.createDefault(token)
                .addEventListeners(new MyDiscordBot())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            Message message = event.getMessage();
            String content = message.getContentRaw();
            String channelId = event.getChannel().getId();

            // Usuwanie wiadomości tylko na wybranym kanale
            if (channelId.equals(CHANNEL_ID)) {
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

    // Metoda obsługująca komendy
    private void handleCommand(MessageReceivedEvent event, String content) {
        // Sprawdzenie uprawnień administratora
        if (!hasAdministratorPermission(event)) {
            event.getChannel().sendMessage("Nie masz uprawnień do używania tej komendy.").queue(
                    sentMessage -> deleteMessageAfterDelayIfInChannel(sentMessage)
            );
            return;
        }

        // Obsługa komendy !ping
        if (content.equals("!ping")) {
            event.getChannel().sendMessage("pongggg").queue(sentMessage ->
                    deleteMessageAfterDelayIfInChannel(sentMessage)
            );
        }

        // Obsługa komendy !update
        else if (content.equals("!update")) {
            boolean success = updateBot();
            if (success) {
                event.getChannel().sendMessage("JAR file updated and server restarted successfully.").queue(sentMessage ->
                        deleteMessageAfterDelayIfInChannel(sentMessage)
                );
            } else {
                event.getChannel().sendMessage("Failed to update JAR file.").queue(sentMessage ->
                        deleteMessageAfterDelayIfInChannel(sentMessage)
                );
            }
        }

        // Obsługa komendy !exit
        else if (content.equals("!exit")) {
            event.getChannel().sendMessage("Shutting down the bot...").queue(sentMessage ->
                    deleteMessageAfterDelayIfInChannel(sentMessage)
            );
            System.exit(0); // Wyjście z aplikacji
        }

        // Obsługa komendy !komendy
        else if(content.equals("!komendy")) {
            sendCommandList(event);
        }
    }

    // Metoda wysyłająca listę komend
    private void sendCommandList(MessageReceivedEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("**Lista komend:**\n");
        builder.append("!ping - Wysyła wiadomość 'pongggg'.\n");
        builder.append("!update - Aktualizuje plik JAR.\n");
        builder.append("!exit - Wyłącza bota.\n");
        event.getChannel().sendMessage(builder.toString()).queue(
                sentMessage -> deleteMessageAfterDelayIfInChannel(sentMessage, 30)
        );
    }

    // Metoda sprawdzająca uprawnienia administratora użytkownika
    private boolean hasAdministratorPermission(MessageReceivedEvent event) {
        return event.getMember().hasPermission(Permission.ADMINISTRATOR);
    }

    // Metoda usuwająca wiadomość po określonym czasie
    private void deleteMessageAfterDelay(Message message) {
        message.delete().queueAfter(10, TimeUnit.SECONDS);
    }

    // Metoda usuwająca wiadomość po określonym czasie tylko na wybranym kanale
    private void deleteMessageAfterDelayIfInChannel(Message message) {
        deleteMessageAfterDelayIfInChannel(message, 10);
    }

    // Metoda usuwająca wiadomość po określonym czasie tylko na wybranym kanale
    private void deleteMessageAfterDelayIfInChannel(Message message, int delaySeconds) {
        if (message.getChannel().getId().equals(CHANNEL_ID)) {
            message.delete().queueAfter(delaySeconds, TimeUnit.SECONDS);
        }
    }

    // Metoda aktualizująca plik JAR za pomocą biblioteki GitCheck która załatwie wiele rzeczy zamiast sie męczyć z cholernym kodem
    private boolean updateBot() {
        Properties prop = new Properties();
        InputStream pomInput = Main.class.getResourceAsStream("/META-INF/maven/pl.mymc/My-DiscordBot/pom.properties");

        try {
            prop.load(pomInput);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String botName = prop.getProperty("artifactId");
        String currentVersion = prop.getProperty("version");

        boolean checkForUpdates = true;
        boolean autoDownloadUpdates = true;
        try {
            BotVersionChecker.checkVersion(botName, currentVersion, checkForUpdates, autoDownloadUpdates);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return checkForUpdates;
    }
}
