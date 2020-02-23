package com.shanewow.life.core;

import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
public class Cell {
    private int x;
    private int y;
    private String id;
    private Boolean currentState;
    private Boolean nextState;
    private List<Cell> neighbors;

    @Builder
    public Cell(int x, int y, boolean startingValue){
        this.x = x;
        this.y = y;
        this.id = formatId(x, y);
        this.currentState = startingValue;
        this.nextState = startingValue;
    }

    public void setNeighbors(List<Cell> neighbors) {
        this.neighbors = neighbors;
    }

    public void applyNext(){
        currentState = nextState;
    }

    public Boolean shouldTurnOn(){
        nextState = neighbors.stream().filter(Cell::isOn).count() == 3;
        return nextState;
    }

    public boolean shouldTurnOff(){
        final long count = neighbors.stream().filter(Cell::isOn).count();
        nextState = (count > 1 && count < 4);
        return nextState;
    }






    //UTILS

    public static Boolean isOn(Cell cell){
        return cell.getCurrentState();
    }

    public static Boolean isOff(Cell cell){
        return !cell.getCurrentState();
    }

    public static CompletableFuture<Boolean> calculateNextAsync(Cell cell){
        if(cell.getCurrentState()){
            return shouldTurnOffAsync(cell);
        }else{
            return shouldTurnOnAsync(cell);
        }
    }

    public static CompletableFuture<Boolean> shouldTurnOnAsync(Cell cell){
        return CompletableFuture.supplyAsync(cell::shouldTurnOn);
    }

    public static CompletableFuture<Boolean> shouldTurnOffAsync(Cell cell){
        return CompletableFuture.supplyAsync(cell::shouldTurnOff);
    }

    public static List<Cell> calculateNeighbors(int x, int y,  int maxX, int maxY, Map<String, Cell> cellMap){
        return Arrays.asList(
                cellMap.get(formatId(increment(x, maxX), y)),
                cellMap.get(formatId(decrement(x, maxX), y)),

                cellMap.get(formatId(increment(x, maxX), increment(y, maxY))),
                cellMap.get(formatId(decrement(x, maxX), decrement(y, maxY))),

                cellMap.get(formatId(increment(x, maxX), decrement(y, maxY))),
                cellMap.get(formatId(decrement(x, maxX), increment(y, maxY))),

                cellMap.get(formatId(x, decrement(y, maxY))),
                cellMap.get(formatId(x, increment(y, maxY)))
        );
    }

    public static int increment(int val, int max){
        if(val == max - 1){
            return 0;
        }
        return val + 1;
    }

    public static int decrement(int val, int max){
        if(val == 0){
            return max - 1;
        }
        return val - 1;
    }

    public static String formatId(int x, int y){
        return String.format("%s,%s", x, y);
    }
}
