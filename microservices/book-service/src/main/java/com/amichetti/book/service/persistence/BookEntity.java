package com.amichetti.book.service.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Document(collection = "books")
public class BookEntity {

  @Id private String id;
  @Version private Integer version;

  @Indexed(unique = true)
  private Long bookId;
  private String title;
  private String author;
  private String description;
  private String isbn;

}
