package com.amichetti.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {

    /*
      La lectura se desarrollarán como APIs síncronas no bloqueantes,
    ya que hay un usuario final esperando sus respuestas.

    Crear y eliminar proporcionados pse desarrollarán
    como servicios asincrónicos orientados a eventos.
     */

    @PostMapping("/recommendation")
    Mono<Recommendation> createRecommendation(@RequestBody Recommendation body);

    @GetMapping("/recommendation/{bookId}")
    Flux<Recommendation> getRecommendations(@PathVariable Long bookId);

    @DeleteMapping(value = "/recommendation/{bookId}")
    Mono<Void> deleteRecommendations(@PathVariable Long bookId);
}
