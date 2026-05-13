package com.amichetti.api.core.review;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    private Long bookId;
    private Long reviewId;
    private String author;
    private String subject;
    private String content;
    private String serviceAddress;
}
