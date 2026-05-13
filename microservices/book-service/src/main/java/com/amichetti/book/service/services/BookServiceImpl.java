package com.amichetti.book.service.services;

import com.amichetti.api.core.book.Book;
import com.amichetti.api.core.book.BookService;
import com.amichetti.api.exception.InvalidInputException;
import com.amichetti.api.exception.NotFoundException;
import com.amichetti.book.service.persistence.BookEntity;
import com.amichetti.book.service.persistence.BookRepository;
import com.amichetti.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

import static java.util.logging.Level.FINE;

@RequiredArgsConstructor
@RestController
public class BookServiceImpl implements BookService {

  private static final Logger LOG = LoggerFactory.getLogger(BookServiceImpl.class);
  private final ServiceUtil serviceUtil;
  private final BookRepository repository;
  private final BookMapper mapper;


  @Override
  public Mono<Book> createBook(Book body) {

      if (body.getBookId() < 1) {
          throw new InvalidInputException("Invalid bookId: " + body.getBookId());
      }
      BookEntity entity = mapper.apiToEntity(body);
      return repository.save(entity)
              .log(LOG.getName(), FINE)
              .onErrorMap(
                      DuplicateKeyException.class,
                      ex -> new InvalidInputException("Duplicate key, Book Id: "
                              + body.getBookId()))
              .map(mapper::entityToApi);
  }

  @Override
  public Mono<Book> getBook(Long bookId) {

    if (bookId < 1) {
      throw new InvalidInputException("Invalid bookId: " + bookId);
    }

      return repository.findByBookId(bookId)
              .switchIfEmpty(Mono.error(new NotFoundException("No book found for bookId: " + bookId)))
              .log(LOG.getName(), FINE)
              .map(mapper::entityToApi)
              .map(this::setServiceAddress);

  }

  @Override
  public Mono<Void> deleteBook(Long bookId) {
      if (bookId < 1) {
          throw new InvalidInputException("Invalid bookId: " + bookId);
      }

      LOG.debug("deleteBook: tries to delete an entity with bookId: {}", bookId);
      return repository.findByBookId(bookId)
              .log(LOG.getName(), FINE)
              .map(repository::delete)
              .flatMap(e -> e);
  }

    private Book setServiceAddress(Book e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
