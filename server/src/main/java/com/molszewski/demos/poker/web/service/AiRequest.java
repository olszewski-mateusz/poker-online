package com.molszewski.demos.poker.web.service;

import com.molszewski.demos.poker.engine.ai.AiConfiguration;
import com.molszewski.demos.poker.web.model.response.GameResponse;

public record AiRequest(AiConfiguration aiConfiguration, GameResponse gameResponse) {
}
