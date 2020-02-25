package com.shanewow.life.core;

import com.shanewow.life.config.LifeContext;
import com.shanewow.life.ui.LifeGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service()
public class CellService implements DisposableBean, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellService.class);

    private Boolean running = false;
    private CompletableFuture<Boolean> promise;
    private LifeContext lifeContext;
    private LifeGrid lifeGrid;
    private CellFactory cellFactory;

    private AtomicLong atomicLong = new AtomicLong();
    private StopWatch stopWatch = new StopWatch();

    public CellService(LifeContext lifeContext, LifeGrid lifeGrid, CellFactory cellFactory){
        this.lifeContext = lifeContext;
        this.lifeGrid = lifeGrid;
        this.cellFactory = cellFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        start();
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }


    public void start(){
        LOGGER.info("Starting...");

        synchronized (running) {
            if(!running){
                running = true;
                stopWatch.start("Processing");

                promise = CompletableFuture.supplyAsync(() ->{
                    while(running){
                        lifeContext.getCells()
                            .parallelStream()
                            .filter(Cell::calculateNext)
                            .collect(Collectors.toList())
                            .parallelStream()
                            .forEach(Cell::applyNext);

                        updateScreen();
                    }
                    return true;
                });
            }else {
                LOGGER.info("Already running.");
            }
        }
    }

    public void stop() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Stopping...");
        synchronized (running){
            if(running){
                running = false;
                promise
                    .whenComplete((r,e) -> {
                        stopWatch.stop();
                        LOGGER.info("Processed {} in {} or {} per sec", atomicLong.get(), stopWatch.getTotalTimeSeconds(), atomicLong.get() / stopWatch.getTotalTimeSeconds());
                    })
                    .get(1, TimeUnit.MINUTES);
            }else {
                LOGGER.info("Stopped called while not running.");
            }
        }
    }

    public void reset(){
        lifeContext.apply(cellFactory.createContext());
        lifeGrid.repaint();
    }

    private void updateScreen(){
        lifeGrid.repaint();
        atomicLong.incrementAndGet();
    }

}
