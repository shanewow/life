package com.shanewow.life.config;

import com.shanewow.life.core.Cell;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class LifeContext {
    private Map<String, Cell> cellMap;
    private List<List<Cell>> rows;
    private List<Cell> cells;
}
