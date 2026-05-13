package com.amichetti.api.composite.book;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class RecommendationSummary {

    private final Long recommendationId;
    private final String author;
    private final int rate;
    private final String content;

    public RecommendationSummary() {
        this.recommendationId = 0L;
        this.author = null;
        this.rate = 0;
        this.content = null;
    }

    public RecommendationSummary(Long recommendationId, String author, int rate, String content) {
        this.recommendationId = recommendationId;
        this.author = author;
        this.rate = rate;
        this.content = content;
    }

}
