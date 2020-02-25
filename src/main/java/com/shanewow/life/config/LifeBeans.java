package com.shanewow.life.config;

import com.shanewow.life.core.CellFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.SplittableRandom;
import java.util.function.Supplier;

@Configuration
public class LifeBeans {

    @Bean
    public Supplier<Boolean> booleanSupplier(){
        final SplittableRandom random = new SplittableRandom();
        //randomly turn on 10% of the cells
        return () -> random.nextDouble() < 0.10;
    }

    @Bean
    public LifeContext lifeContext(CellFactory cellFactory){
        return cellFactory.createContext();
    }
}
