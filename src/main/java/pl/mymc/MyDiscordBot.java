package pl.mymc;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class MyDiscordBot extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {

        String token = "MTI1NzMyNDcwNzAzMjg1ODc4MQ.GXqFsY.AuOj1swDyycI9ZhY1DnA7wtYPdp60ZyrAU9has"; // token Domiego MTI1Nzc1MTE1NTQzMDI2NDk0NA.GPMiEC.GrxP1Lip7NyhLnPhYIktd5O7hPT7U2UuZJPMIc
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

            if (content.equals("!ping")) {
                event.getChannel().sendMessage("pong").queue();
            }

            if (content.toLowerCase().contains("test")) {
                message.delete().queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}