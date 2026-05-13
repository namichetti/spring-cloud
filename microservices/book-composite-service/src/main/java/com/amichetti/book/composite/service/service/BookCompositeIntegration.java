package com.amichetti.book.composite.service.service;

import com.amichetti.api.core.book.Book;
import com.amichetti.api.core.book.BookService;
import com.amichetti.api.core.recommendation.Recommendation;
import com.amichetti.api.core.recommendation.RecommendationService;
import com.amichetti.api.core.review.Review;
import com.amichetti.api.core.review.ReviewService;
import com.amichetti.api.event.Event;
import com.amichetti.api.exception.InvalidInputException;
import com.amichetti.api.exception.NotFoundException;
import com.amichetti.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.List;

import static com.amichetti.api.event.Event.Type.CREATE;
import static com.amichetti.api.event.Event.Type.DELETE;
import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Mono.empty;

@Component
public class BookCompositeIntegration implements BookService,
        RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(BookCompositeIntegration.class);

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String bookServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    //Permite trabajar eventos sin depender directamente de Kafka o Rabbit
    private final StreamBridge streamBridge;

    private final Scheduler publishEventScheduler;

    public BookCompositeIntegration(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,

            WebClient.Builder webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge,

            @Value("${app.book-service.host}") String bookServiceHost,
            @Value("${app.book-service.port}") int  bookServicePort,

            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int  recommendationServicePort,

            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int  reviewServicePort
    ) {

        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClient.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;

        bookServiceUrl        = "http://" + bookServiceHost + ":" + bookServicePort;
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
        reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort;
    }

    @Override
    public Mono<Book> createBook(Book body) {

        return Mono.fromCallable(() -> {
            sendMessage("books-out-0", new Event<>(CREATE, body.getBookId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Book> getBook(Long bookId) {
        String url = bookServiceUrl + "/book/" + bookId;
        LOG.debug("Will call the getBook API on URL: {}", url);

        return webClient.get().uri(url).retrieve().bodyToMono(Book.class).log(LOG.getName(), FINE).onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Void> deleteBook(Long bookId) {

        return Mono.fromRunnable(() -> sendMessage("books-out-0", new Event<>(DELETE, bookId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0", new Event<>(CREATE, body.getRecommendationId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<Recommendation> getRecommendations(Long bookId) {

        String url = recommendationServiceUrl + "/recommendation?bookId=" + bookId;

        LOG.debug("Will call the getRecommendations API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get().uri(url).retrieve()
                .bodyToFlux(Recommendation.class)
                .log(LOG.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteRecommendations(Long bookId) {

        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event<>(DELETE, bookId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<Review> createReview(Review body) {

        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event<>(CREATE, body.getReviewId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<Review> getReviews(Long bookId) {

        String url = reviewServiceUrl + "/review?bookId=" + bookId;

        LOG.debug("Will call the getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteReviews(Long bookId) {

        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event<>(DELETE, bookId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    public Mono<Health> getBookHealth() {
        return getHealth(bookServiceUrl);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(reviewServiceUrl);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call the Health API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(LOG.getName(), FINE);
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
