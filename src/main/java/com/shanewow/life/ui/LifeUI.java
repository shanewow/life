package com.shanewow.life.ui;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

@Component
public class LifeUI extends JFrame {

    private LifeGrid lifeGrid;

    public LifeUI(LifeGrid lifeGrid) {
        this.lifeGrid = lifeGrid;
        initUI();
    }

    private void initUI() {

        final JButton quitButton = new JButton("Quit");

        quitButton.addActionListener((ActionEvent event) -> System.exit(0));

        createLayout(quitButton, lifeGrid);

        setTitle("Life");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void createLayout(JComponent... arg) {

        final Container pane = getContentPane();
        final GroupLayout layout = new GroupLayout(pane);
        pane.setLayout(layout);

        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(arg[0])
                        .addComponent(arg[1])
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(arg[0])
                        .addComponent(arg[1])
        );
    }

}
