package com.shanewow.life.core;

import com.shanewow.life.config.LifeContext;
import com.shanewow.life.config.LifeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

        final StopWatch stopWatch = new StopWatch();

        stopWatch.start("Building cells");
        final Map<String, Cell> map = createCells();
        stopWatch.stop();

        stopWatch.start("Sorting Cells Into Rows");
        final List<List<Cell>> rows = initRows(map);
        stopWatch.stop();

        stopWatch.start("Flattening Cells");
        final List<Cell> cells = rows.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        stopWatch.stop();

        LOGGER.info("{} Cells initialized in: {}", cells.size(), stopWatch.toString());

        return LifeContext.builder()
                .cellMap(map)
                .rows(rows)
                .cells(cells)
                .build();
    }

    private Map<String, Cell> createCells(){

        //generate the cells and their initial state
        final Map<String, Cell> cellMap = IntStream
                .range(0, lifeProperties.getYMax())
                .boxed()
                .flatMap(this::createRow)
                .collect(Collectors.toMap(Cell::getId, Function.identity()));

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

        return cellMap;
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

    private Stream<Cell> createRow(int y){
        return IntStream
                .range(0, lifeProperties.getXMax())
                .mapToObj(createCellFunction(y));
    }

    private IntFunction<Cell> createCellFunction(int y){
        return x -> Cell.builder()
                .x(x)
                .y(y)
//                .startingValue(random.nextBoolean())
                .startingValue(booleanSupplier.get())
                .build();
    }

}
