package com.shanewow.life.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("com.shanewow.life")
@Data
public class LifeProperties {
    private int xMax;
    private int yMax;
    private int size;
}
