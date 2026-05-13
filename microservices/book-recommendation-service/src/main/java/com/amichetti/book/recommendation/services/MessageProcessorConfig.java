package com.amichetti.book.recommendation.services;

import java.util.function.Consumer;

import com.amichetti.api.core.recommendation.Recommendation;
import com.amichetti.api.core.recommendation.RecommendationService;
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

  private final RecommendationService recommendationService;


  @Bean
  public Consumer<Event<Long, Recommendation>> messageProcessor() {
    return event -> {

      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      switch (event.getEventType()) {

        case CREATE:
          Recommendation recommendation = event.getData();
          LOG.info("Create recommendation with ID: {}/{}", recommendation.getBookId(), recommendation.getRecommendationId());
          recommendationService.createRecommendation(recommendation).block();
          break;

        case DELETE:
          Long bookId = event.getKey();
          LOG.info("Delete recommendations with BookId: {}", bookId);
          recommendationService.deleteRecommendations(bookId).block();
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
