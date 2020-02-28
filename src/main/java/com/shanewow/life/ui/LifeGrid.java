package com.shanewow.life.ui;

import com.shanewow.life.config.LifeContext;
import com.shanewow.life.config.LifeProperties;
import com.shanewow.life.core.Cell;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class LifeGrid extends JPanel {

    private final LifeProperties lifeProperties;
    private final LifeContext lifeContext;

    public LifeGrid(LifeProperties lifeProperties, LifeContext lifeContext){
        this.lifeProperties = lifeProperties;
        this.lifeContext = lifeContext;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
        lifeProperties.getXMax() * lifeProperties.getSize(),
        lifeProperties.getYMax() * lifeProperties.getSize()
        );
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final int size = lifeProperties.getSize();

        g.setColor(Color.BLACK);

        lifeContext.getCells()
                .stream()
                .filter(Cell::isOn)
                .forEach(cell -> g.fillRect(cell.getX() * size, cell.getY() * size, size, size));

        g.dispose();
    }
}
