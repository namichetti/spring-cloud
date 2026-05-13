package com.amichetti.api.core.review;


import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReviewService {

    /*
      La lectura se desarrollarán como APIs síncronas no bloqueantes,
    ya que hay un usuario final esperando sus respuestas.

    Crear y eliminar proporcionados pse desarrollarán
    como servicios asincrónicos orientados a eventos.
     */

    @PostMapping("/review")
    Mono<Review> createReview(@RequestBody Review body);

    @GetMapping("/review/{bookId}")
    Flux<Review> getReviews(@PathVariable Long bookId);


    @DeleteMapping("/review/{bookId}")
    Mono<Void> deleteReviews(@PathVariable  Long bookId);
}
