package org.jeka.demowebinar1no_react;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BorisovLLMApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BorisovLLMApplication.class, args);
    }
}
