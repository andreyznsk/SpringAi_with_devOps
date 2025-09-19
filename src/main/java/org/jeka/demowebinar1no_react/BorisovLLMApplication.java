package org.jeka.demowebinar1no_react;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BorisovLLMApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BorisovLLMApplication.class, args);
        ChatClient chatClient = context.getBean(ChatClient.class);
//        System.out.println(chatClient.prompt().user("Дай первую строчку Bohemian Rhapsody").call().content());
    }



}
