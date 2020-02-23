package com.shanewow.life.config;

import com.shanewow.life.core.CellFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class LifeBeans {

    @Bean
    public Random random(){
        return new Random();
    }

    @Bean
    public LifeContext lifeContext(CellFactory cellFactory){
        return cellFactory.createContext();
    }
}
