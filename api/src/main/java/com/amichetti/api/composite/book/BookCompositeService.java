package com.amichetti.api.composite.book;


import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface BookCompositeService {

    /*
    Crear, leer y eliminar expuestos estarán basados en APIs síncronas no bloqueantes.

    Se asume que el microservicio tiene clientes tanto en plataformas web como móviles,
    así como clientes provenientes de otras organizaciones distintas de las que operan
    el ecosistema del sistema.
    Por lo tanto, las APIs síncronas parecen ser una elección natural.
     */

    /*
    Las APIs síncronas proporcionadas para crear y eliminar información agregada de productos
    publicarán eventos de creación y eliminación en estos topics.
    Si la operación de publicación tiene éxito, devolverá una respuesta 202 (Accepted);
    de lo contrario, se devolverá una respuesta de error.

    Habrá un topic por tipo de entidad: books, recommendations y reviews.

    La respuesta 202 difiere de una respuesta normal 200 (OK): indica que la solicitud ha sido aceptada
    pero no completamente procesada.
    En cambio, el procesamiento se completará de manera asincrónica e independiente de la respuesta 202.
     */
  @PostMapping("/book-composite")
  Mono<Void>  createBook(@RequestBody BookAggregate body);

  @GetMapping("/book-composite/{bookId}")
  Mono<BookAggregate> getBook(@PathVariable Long bookId);

  @DeleteMapping("/book-composite/{bookId}")
  Mono<Void> deleteBook(@PathVariable Long bookId);
}
