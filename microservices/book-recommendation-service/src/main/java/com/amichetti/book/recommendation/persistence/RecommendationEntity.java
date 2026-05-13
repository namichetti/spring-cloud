package com.amichetti.book.recommendation.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "recommendations")
@CompoundIndex(name = "book-rec-id", unique = true, def = "{'bookId': 1, 'recommendationId' : 1}")
public class RecommendationEntity {

  @Id
  private String id;

  @Version
  private Integer version;

  private Long bookId;
  private Long recommendationId;
  private String author;
  private int rating;
  private String content;

}
