package com.shanewow.life.core;

import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public class Cell {

    private int x;
    private int y;
    private String id;
    private boolean currentState;
    private boolean nextState;
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

    public void setNext(boolean next){ nextState = next; }

    public void applyNext(){
        currentState = nextState;
    }

    public boolean shouldTurnOn(){
        final long count = neighbors.stream().filter(Cell::isOn).count();
        nextState = count == 3L;
        return nextState;
    }

    public boolean shouldTurnOff(){
        final long count = neighbors.stream().filter(Cell::isOn).count();
        nextState = (count > 1L && count < 4L);
        return !nextState;
    }






    //UTILS

    public static boolean isOn(Cell cell){
        return cell.isCurrentState();
    }

    public static boolean calculateNext(Cell cell){
        if(cell.isCurrentState()){
            return cell.shouldTurnOff();
        }else{
            return cell.shouldTurnOn();
        }
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

    private static int increment(int val, int max){
        if(val == max - 1){
            return 0;
        }
        return val + 1;
    }

    private static int decrement(int val, int max){
        if(val == 0){
            return max - 1;
        }
        return val - 1;
    }

    private static String formatId(int x, int y){
        return String.format("%s,%s", x, y);
    }
}
