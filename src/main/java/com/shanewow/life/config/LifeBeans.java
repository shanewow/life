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
        final SplittableRandom splittableRandom = new SplittableRandom();
        final ThreadLocal<SplittableRandom> random = ThreadLocal.withInitial(() -> splittableRandom.split());
        //randomly turn on 10% of the cells
        return () -> random.get().nextDouble() < 0.10;
    }

    @Bean
    public LifeContext lifeContext(CellFactory cellFactory){
        return cellFactory.createContext();
    }
}
