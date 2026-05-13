package com.amichetti.book.review.service.services;

import java.util.function.Consumer;

import com.amichetti.api.core.review.Review;
import com.amichetti.api.core.review.ReviewService;
import com.amichetti.api.event.Event;
import com.amichetti.api.exception.EventProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final ReviewService reviewService;

  /*
  Para asegurar que podamos propagar las excepciones lanzadas por el bean bookService
  de vuelta al sistema de mensajería, llamamos a block() sobre las respuestas que obtenemos
  del bean bookService.

  Esto garantiza que el procesador de mensajes espere a que el bean bookService complete
  la creación o eliminación en la base de datos subyacente.

  Sin llamar al block(), no podríamos propagar las excepciones y el sistema de mensajería
  no podría reencolar un intento fallido o, posiblemente, mover el mensaje a una cola de
  mensajes fallidos (dead-letter queue); en cambio, el mensaje sería descartado silenciosamente.

  Llamar a un método block() es, en general, considerado una mala práctica desde el punto de vista
  del rendimiento y la escalabilidad. Pero en este caso, solo manejaremos unos pocos mensajes entrantes
  en paralelo, uno por partición.
  Esto significa que solo tendremos unos pocos hilos bloqueados simultáneamente,
  lo cual no impactará negativamente en el rendimiento ni en la escalabilidad.
   */
  @Bean
  public Consumer<Event<Long, Review>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      switch (event.getEventType()) {

        case CREATE:
          Review review = event.getData();
          LOG.info("Create review with ID: {}/{}", review.getBookId(), review.getReviewId());
          reviewService.createReview(review).block();
          break;

        case DELETE:
          Long bookId = event.getKey();
          LOG.info("Delete reviews with BookId: {}", bookId);
          reviewService.deleteReviews(bookId).block();
          break;

        default:
          String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
          LOG.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("Message processing done!");
    };
  }
}
