package com.amichetti.book.recommendation.services;

import com.amichetti.api.core.recommendation.Recommendation;
import com.amichetti.api.core.recommendation.RecommendationService;
import com.amichetti.api.exception.InvalidInputException;
import com.amichetti.book.recommendation.persistence.RecommendationEntity;
import com.amichetti.book.recommendation.persistence.RecommendationRepository;
import com.amichetti.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static java.util.logging.Level.FINE;


@RequiredArgsConstructor
@RestController
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final RecommendationRepository repository;
  private final RecommendationMapper mapper;
  private final ServiceUtil serviceUtil;


    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        if (body.getBookId() < 1) {
            throw new InvalidInputException("Invalid bookId: " + body.getBookId());
        }

        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<Recommendation> newEntity = repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Book Id: " + body.getBookId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(mapper::entityToApi);

        return newEntity;
    }

    @Override
    public Flux<Recommendation> getRecommendations(Long bookId) {

        if (bookId < 1) {
            throw new InvalidInputException("Invalid bookId: " + bookId);
        }

        LOG.info("Will get recommendations for book with id={}", bookId);

        return repository.findByBookId(bookId)
                .log(LOG.getName(), FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteRecommendations(Long bookId) {

        if (bookId < 1) {
            throw new InvalidInputException("Invalid bookId: " + bookId);
        }

        LOG.debug("deleteRecommendations: tries to delete recommendations for the book with bookId: {}", bookId);
        return repository.deleteAll(repository.findByBookId(bookId));
    }

    private Recommendation setServiceAddress(Recommendation e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
