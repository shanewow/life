package com.shanewow.life.core;

import com.shanewow.life.config.LifeContext;
import com.shanewow.life.config.LifeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class CellFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellFactory.class);

    private final Supplier<Boolean> booleanSupplier;
    private final LifeProperties lifeProperties;

    public CellFactory(LifeProperties lifeProperties, Supplier<Boolean> booleanSupplier) {
        this.lifeProperties = lifeProperties;
        this.booleanSupplier = booleanSupplier;
    }

    public LifeContext createContext() {

        final long startTime = System.currentTimeMillis();

        //create cells
        final Map<String, Cell> map = createCells();
        //associate neighbors
        final CompletableFuture<Boolean> neighborsPromise = CompletableFuture.supplyAsync(() -> {
            initNeighbors(map);
            return true;
        });
        //sorted into rows
        final CompletableFuture<List<List<Cell>>> rowsPromise = CompletableFuture.supplyAsync(() -> initRows(map));
        //sorted into cells list
        final CompletableFuture<List<Cell>> cellsPromise = CompletableFuture.supplyAsync(() -> initCells(map));

        try {
            neighborsPromise.get();

            LOGGER.info("{} Cells initialized in: {} seconds", map.size(), (System.currentTimeMillis() - startTime)/1000 );

            return LifeContext.builder()
                    .cellMap(map)
                    .rows(rowsPromise.get())
                    .cells(cellsPromise.get())
                    .build();

        } catch (InterruptedException e) {
            throw new RuntimeException("Cell creation was interrupted.", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Unexpected exception while creating cells.", e);
        }
    }

    private Map<String, Cell> createCells(){

        //generate the cells and their initial state
        final Map<String, Cell> cellMap = IntStream
                .range(0, lifeProperties.getYMax())
                .parallel()
                .boxed()
                .flatMap(this::createRow)
                .collect(Collectors.toMap(Cell::getId, Function.identity()));

        return cellMap;
    }

    private Stream<Cell> createRow(int y){
        return IntStream
                .range(0, lifeProperties.getXMax())
                .mapToObj(createCellFunction(y));
    }

    private IntFunction<Cell> createCellFunction(int y){
        return x -> Cell.builder()
                .x(x)
                .y(y)
                .startingValue(booleanSupplier.get())
                .build();
    }


    private void initNeighbors(Map<String, Cell> cellMap){
        //set neighbors now that all have been created
        cellMap
                .values()
                .parallelStream()
                .forEach(cell -> cell.setNeighbors(
                        Cell.calculateNeighbors(
                                cell.getX(),
                                cell.getY(),
                                lifeProperties.getXMax(),
                                lifeProperties.getYMax(),
                                cellMap))
                );
    }

    private static List<List<Cell>> initRows(Map<String, Cell> cellMap){
        return cellMap
                .values()
                .stream()
                .sorted(Comparator.comparing(Cell::getY).thenComparing(Cell::getX))
                .collect(Collectors.groupingBy(Cell::getY))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private static List<Cell> initCells(Map<String, Cell> cellMap){
        return cellMap
                .values()
                .stream()
                .sorted(Comparator.comparing(Cell::getY).thenComparing(Cell::getX))
                .collect(Collectors.toList());
    }



}
