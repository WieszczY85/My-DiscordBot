package pl.mymc;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.security.auth.login.LoginException;

public class MyDiscordBot extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {

        String token = "token";
        JDABuilder.createDefault(token)
                .addEventListeners(new MyDiscordBot())
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();

        if (content.equals("!ping")) {
            event.getChannel().sendMessage("pong").queue();
        }

        if (content.toLowerCase().contains("test")) {
            message.delete().queue();
        }
    }
}