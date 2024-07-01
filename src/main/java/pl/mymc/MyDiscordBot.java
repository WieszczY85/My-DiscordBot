package pl.mymc;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class MyDiscordBot {
    public static void main(String[] args) {
        JDABuilder.createDefault("token")
                .setActivity(Activity.playing("1.. 2... 3... Próba bota!"))
                .build();
    }
}
