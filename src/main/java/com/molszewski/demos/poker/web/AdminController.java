package com.molszewski.demos.poker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final ReactiveStringRedisTemplate redisTemplate;

    @PostMapping("/clear")
    public Mono<ResponseEntity<Void>> clear() {
        return redisTemplate.scan()
                .flatMap(redisTemplate::delete)
                .collectList()
                .thenReturn(ResponseEntity.noContent().<Void>build())
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
