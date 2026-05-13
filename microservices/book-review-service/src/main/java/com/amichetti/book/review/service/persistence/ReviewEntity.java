package com.amichetti.book.review.service.persistence;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews",
        indexes = {
        @Index(name = "reviews_unique_idx",
                unique = true,
                columnList = "bookId,reviewId") })
public class ReviewEntity {

  @Id
  @GeneratedValue
  private Long id;

  @Version
  private int version;

  private Long bookId;
  private Long reviewId;
  private String author;
  private String subject;
  private String content;
}
