package com.shanewow.life.config;

import com.shanewow.life.core.CellFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Configuration
public class LifeBeans {

    @Bean
    public Supplier<Boolean> booleanSupplier(){
        final Random random = new Random();
        final AtomicLong counter = new AtomicLong();

        //give a random value every 4th request
        return () -> Math.floorMod(counter.incrementAndGet(), random.nextInt(16) + 1) == 1
                ? random.nextBoolean()
                : false;
    }

    @Bean
    public LifeContext lifeContext(CellFactory cellFactory){
        return cellFactory.createContext();
    }
}
