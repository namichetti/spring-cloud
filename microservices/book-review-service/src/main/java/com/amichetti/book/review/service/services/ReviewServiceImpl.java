package com.amichetti.book.review.service.services;

import java.util.List;

import com.amichetti.api.core.review.Review;
import com.amichetti.api.core.review.ReviewService;
import com.amichetti.api.exception.InvalidInputException;
import com.amichetti.book.review.service.persistence.ReviewEntity;
import com.amichetti.book.review.service.persistence.ReviewRepository;
import com.amichetti.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static java.util.logging.Level.FINE;

@RestController
public class ReviewServiceImpl implements ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ReviewRepository repository;
  private final ReviewMapper mapper;
  private final ServiceUtil serviceUtil;
  private final Scheduler jdbcScheduler;

  public ReviewServiceImpl(
          ReviewRepository repository,
          ReviewMapper mapper,
          ServiceUtil serviceUtil,
          @Qualifier("jdbcScheduler") Scheduler jdbcScheduler) {

      this.repository = repository;
      this.mapper = mapper;
      this.serviceUtil = serviceUtil;
      this.jdbcScheduler = jdbcScheduler;
  }

  @Override
  public Mono<Review> createReview(Review body) {

      if (body.getBookId() < 1) {
          throw new InvalidInputException("Invalid bookId: " + body.getBookId());
      }
      return Mono.fromCallable(() -> internalCreateReview(body))
                .subscribeOn(jdbcScheduler);
  }

  private Review internalCreateReview(Review body) {
      try {
          ReviewEntity entity = mapper.apiToEntity(body);
          ReviewEntity newEntity = repository.save(entity);

          LOG.debug("createReview: created a review entity: {}/{}", body.getBookId(), body.getReviewId());
          return mapper.entityToApi(newEntity);

      }catch (DataIntegrityViolationException dive) {
          throw new InvalidInputException("Duplicate key, Book Id: " + body.getBookId() + ", Review Id:" + body.getReviewId());
      }
  }

  @Override
  public Flux<Review> getReviews(Long bookId) {
      if (bookId < 1) {
          throw new InvalidInputException("Invalid bookId: " + bookId);
      }

      LOG.info("Will get reviews for book with id={}", bookId);

      return Mono.fromCallable(() -> internalGetReviews(bookId))
              .flatMapMany(Flux::fromIterable)
              .log(LOG.getName(), FINE)
              .subscribeOn(jdbcScheduler);
    }

    private List<Review> internalGetReviews(Long bookId) {

      List<ReviewEntity> entityList = repository.findByBookId(bookId);
      List<Review> list = mapper.entityListToApiList(entityList);
      list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

      LOG.debug("Response size: {}", list.size());

      return list;
  }

  @Override
  public Mono<Void> deleteReviews(Long bookId) {
      if (bookId < 1) {
          throw new InvalidInputException("Invalid bookId: " + bookId);
       }

      return Mono.fromRunnable(() -> internalDeleteReviews(bookId))
              .subscribeOn(jdbcScheduler)
              .then();
   }

   private void internalDeleteReviews(Long bookId) {
      LOG.debug("deleteReviews: tries to delete reviews for the book with bookId: {}", bookId);

      repository.deleteAll(repository.findByBookId(bookId));
  }
}