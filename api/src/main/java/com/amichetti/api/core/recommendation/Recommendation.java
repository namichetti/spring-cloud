package com.amichetti.api.core.recommendation;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Recommendation {

    private Long bookId;
    private Long recommendationId;
    private String author;
    private int rate;
    private String content;
    private String serviceAddress;
}
