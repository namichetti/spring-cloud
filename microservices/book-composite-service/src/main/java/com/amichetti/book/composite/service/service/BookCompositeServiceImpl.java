package com.amichetti.book.composite.service.service;

import com.amichetti.api.composite.book.*;
import com.amichetti.api.core.book.Book;
import com.amichetti.api.core.book.BookService;
import com.amichetti.api.core.recommendation.Recommendation;
import com.amichetti.api.core.review.Review;
import com.amichetti.api.event.Event;
import com.amichetti.api.exception.InvalidInputException;
import com.amichetti.api.exception.NotFoundException;
import com.amichetti.util.http.HttpErrorInfo;
import com.amichetti.util.http.ServiceUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.amichetti.api.event.Event.Type.CREATE;
import static com.amichetti.api.event.Event.Type.DELETE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.util.logging.Level.FINE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestController
@RequiredArgsConstructor
public class BookCompositeServiceImpl implements BookCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(BookCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final BookCompositeIntegration integration;

    @Override
    public Mono<Void> createBook(BookAggregate body) {

        try {

            List<Mono> monoList = new ArrayList<>();

            LOG.info("Will create a new composite entity for book.id: {}", body.getBookId());

            Book book = new Book(
                    body.getBookId(),
                    body.getTitle(),
                    body.getDescription(),
                    body.getAuthor(),
                    body.getIsbn(),
                    null);
            monoList.add(integration.createBook(book));

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getBookId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    monoList.add(integration.createRecommendation(recommendation));
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getBookId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    monoList.add(integration.createReview(review));
                });
            }

            LOG.debug("createCompositeBook: composite entities created for bookId: {}", body.getBookId());

            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                    .doOnError(ex -> LOG.warn("createCompositeBook failed: {}", ex.toString()))
                    .then();

        } catch (RuntimeException re) {
            LOG.warn("createCompositeBook failed: {}", re.toString());
            throw re;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<BookAggregate> getBook(Long bookId) {

        LOG.info("Will get composite book info for book.id={}", bookId);
        return Mono.zip(
                        values -> createBookAggregate((Book) values[0], (List<Recommendation>) values[1], (List<Review>) values[2], serviceUtil.getServiceAddress()),
                        integration.getBook(bookId),
                        integration.getRecommendations(bookId).collectList(),
                        integration.getReviews(bookId).collectList())
                .doOnError(ex -> LOG.warn("getCompositeBook failed: {}", ex.toString()))
                .log(LOG.getName(), FINE);
    }

    @Override
    public Mono<Void> deleteBook(Long bookId) {

        try {

            LOG.info("Will delete a book aggregate for book.id: {}", bookId);

            return Mono.zip(
                            r -> "",
                            integration.deleteBook(bookId),
                            integration.deleteRecommendations(bookId),
                            integration.deleteReviews(bookId))
                    .doOnError(ex -> LOG.warn("delete failed: {}", ex.toString()))
                    .log(LOG.getName(), FINE).then();

        } catch (RuntimeException re) {
            LOG.warn("deleteCompositeBook failed: {}", re.toString());
            throw re;
        }
    }

    private BookAggregate createBookAggregate(Book book, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        // 1. Setup book info
        Long bookId = book.getBookId();
        String title = book.getTitle();
        String description = book.getDescription();
        String isbn = book.getIsbn();
        String author =book.getAuthor();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :
                reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String bookAddress = book.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, bookAddress, reviewAddress, recommendationAddress);

        return new BookAggregate(bookId, title, description,recommendationSummaries, reviewSummaries,serviceAddresses,author,isbn);
    }
}
