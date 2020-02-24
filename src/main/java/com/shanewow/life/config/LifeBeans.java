package com.shanewow.life.config;

import com.shanewow.life.core.CellFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.function.Supplier;

@Configuration
public class LifeBeans {

    @Bean
    public Supplier<Boolean> booleanSupplier(){
        final Random random = new Random();
        //randomly turn on 20% of the cells
        return () -> random.nextDouble() < 0.20
                ? random.nextBoolean()
                : false;
    }

    @Bean
    public LifeContext lifeContext(CellFactory cellFactory){
        return cellFactory.createContext();
    }
}
