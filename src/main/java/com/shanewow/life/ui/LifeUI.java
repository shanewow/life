package com.shanewow.life.ui;

import com.shanewow.life.core.CellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class LifeUI extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifeUI.class);

    private LifeGrid lifeGrid;
    private CellService cellService;
    private List<Consumer<LifeUIState>> stateChangeListeners;

    public LifeUI(LifeGrid lifeGrid, CellService cellService) {
        this.lifeGrid = lifeGrid;
        this.cellService = cellService;
        this.stateChangeListeners = initUI();
        callListeners(LifeUIState.STARTED);
    }

    private void changeState(LifeUIState state){
        callListeners(state);
    }

    private void callListeners(LifeUIState state){
        stateChangeListeners.forEach(consumer -> consumer.accept(state));
    }

    private List<Consumer<LifeUIState>> initUI() {

        final List<Consumer<LifeUIState>> stateListeners = new ArrayList<>();

        final Container pane = getContentPane();
        final FlowLayout flowLayout = new FlowLayout();
        pane.setLayout(flowLayout);

        final JButton quitButton = new JButton("Quit");
        quitButton.addActionListener((ActionEvent event) -> System.exit(0));
        pane.add(quitButton);


        final JButton pauseButton = new JButton("Stop");
        pauseButton.addActionListener((ActionEvent event) -> {
            try {
                cellService.stop();
                changeState(LifeUIState.STOPPED);
            } catch (Exception e) {
                LOGGER.error("Unexpected exception while waiting for threads to stop", e);
            }
        });
        stateListeners.add(state -> pauseButton.setEnabled(LifeUIState.STARTED.equals(state)));
        pane.add(pauseButton);

        final JButton resetButton = new JButton("Reset");
        resetButton.addActionListener((ActionEvent event) -> {
            cellService.reset();
        });
        stateListeners.add(state -> resetButton.setEnabled(LifeUIState.STOPPED.equals(state)));
        pane.add(resetButton);

        final JButton startButton = new JButton("Start");
        startButton.addActionListener((ActionEvent event) -> {
                changeState(LifeUIState.STARTED);
                cellService.start();
        });
        stateListeners.add(state -> startButton.setEnabled(LifeUIState.STOPPED.equals(state)));
        pane.add(startButton);
        pane.add(lifeGrid);

        setTitle("Life");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        return stateListeners;
    }

}
