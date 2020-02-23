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

@Service()
public class CellService implements DisposableBean, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellService.class);

    private boolean running;
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
        start();
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }


    public void start(){
        LOGGER.info("Starting...");

        running = true;
        stopWatch.start("Processing");

        promise = CompletableFuture.supplyAsync(() ->{
            while(running){
                try {
                    CompletableFuture.allOf(
                        lifeContext.getCells()
                                .parallelStream()
                                .map(Cell::calculateNextAsync)
                                .toArray(CompletableFuture[]::new)
                    )
                    .thenAcceptAsync(result -> lifeContext.getCells()
                        .parallelStream()
                        .forEach(Cell::applyNext)
                    )
                    .whenComplete((r,e) -> updateScreen())
                    .get();
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted while processing", e);
                } catch (ExecutionException e) {
                    LOGGER.error("Unexpected exception while processing", e);
                }
            }
           return true;
        });
    }

    public Boolean stop() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Stopping...");
        running = false;
        return promise
                .whenComplete((r,e) -> {
                    stopWatch.stop();
                    LOGGER.info("Processed {} in {} or {} per sec", atomicLong.get(), stopWatch.getTotalTimeSeconds(), atomicLong.get() / stopWatch.getTotalTimeSeconds());
                })
                .get(1, TimeUnit.MINUTES);
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
