package com.amichetti.book.service.services;

import com.amichetti.api.core.book.Book;
import com.amichetti.api.core.book.BookService;
import com.amichetti.api.event.Event;
import com.amichetti.api.exception.EventProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final BookService bookService;

  @Bean
  public Consumer<Event<Long, Book>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      switch (event.getEventType()) {

        case CREATE:
          Book book = event.getData();
          LOG.info("Create book with ID: {}", book.getBookId());
          bookService.createBook(book).block();
          break;

        case DELETE:
          Long bookId = event.getKey();
          LOG.info("Delete book with BookId: {}", bookId);
          bookService.deleteBook(bookId).block();
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
