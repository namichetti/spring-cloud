package com.amichetti.api.core.book;

import com.amichetti.api.composite.book.BookAggregate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequestMapping("/book")
public interface BookService {

    /*
    La lectura se desarrollarán como APIs síncronas no bloqueantes,
    ya que hay un usuario final esperando sus respuestas.

    Crear y eliminar proporcionados pse desarrollarán
    como servicios asincrónicos orientados a eventos.
     */

    @PostMapping("/book")
    Mono<Book> createBook(@RequestBody Book body);

    @GetMapping("/book/{bookId}")
    Mono<Book> getBook(@PathVariable Long bookId);

    @DeleteMapping("/book/{bookId}")
    Mono<Void> deleteBook(@PathVariable Long bookId);
}
