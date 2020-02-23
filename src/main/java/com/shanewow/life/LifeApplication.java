package com.shanewow.life;

import com.shanewow.life.ui.LifeUI;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.awt.*;

@SpringBootApplication
public class LifeApplication {

    public static void main(String[] args) {

        final ApplicationContext context = new SpringApplicationBuilder(LifeApplication.class)
                .headless(false)
                .run(args);

        EventQueue.invokeLater(() ->
            context.getBean(LifeUI.class)
                    .setVisible(true)
        );
    }

}
