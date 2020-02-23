package com.shanewow.life.core;

import com.shanewow.life.config.LifeContext;
import com.shanewow.life.ui.LifeGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.List;
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
//                processRowsWithFutures(); //ok 143.5
                processCellsWithoutFutures(); //best 157.8
//                processRowsWithoutFutures(); //ok 149.6
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


    private void processRowsWithFutures(){
        try {
            CompletableFuture.allOf(
                    lifeContext.getRows()
                            .parallelStream()
                            .map(CellService::processRowAsync)
                            .toArray(CompletableFuture[]::new)
            )
            .thenAcceptAsync(result ->
                    lifeContext.getCells()
                            .parallelStream()
                            .forEach(Cell::applyNext)
            )
            .whenComplete(this::updateScreen)
            .get();
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while processing", e);
        } catch (ExecutionException e) {
            LOGGER.error("Unexpected exception while processing", e);
        }
    }


    private void processRowsWithoutFutures(){
        lifeContext.getRows()
                .parallelStream()
                .forEach(CellService::processRow);

        lifeContext.getCells()
                .parallelStream()
                .forEach(Cell::applyNext);

        updateScreen();
    }

    private void processCellsWithoutFutures(){

        lifeContext.getCells()
                .parallelStream()
                .forEach(Cell::calculateNext);

        lifeContext.getCells()
                .parallelStream()
                .forEach(Cell::applyNext);

        updateScreen();
    }




    private void updateScreen(Object result, Throwable error){
        updateScreen();
    }

    private void updateScreen(){
        lifeGrid.repaint();
        atomicLong.incrementAndGet();
    }





    private static CompletableFuture<Boolean> processRowAsync(List<Cell> row){
        return CompletableFuture.supplyAsync(() -> processRow(row));
    }

    private static Boolean processRow(List<Cell> row){
        row.parallelStream().forEach(Cell::calculateNext);
        return true;
    }

}
