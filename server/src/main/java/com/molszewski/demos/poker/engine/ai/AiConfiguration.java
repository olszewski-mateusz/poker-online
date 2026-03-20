package com.molszewski.demos.poker.engine.ai;

public record AiConfiguration(
        Aggressivity aggressivity
) {

    public enum Aggressivity {
        LOW,
        MEDIUM,
        HIGH
    }
}
