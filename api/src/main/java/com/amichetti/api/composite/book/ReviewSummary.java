package com.amichetti.api.composite.book;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ReviewSummary{

    private final Long reviewId;
    private final String author;
    private final String subject;
    private final String content;

    public ReviewSummary() {
        this.reviewId = 0L;
        this.author = null;
        this.subject = null;
        this.content = null;
    }

    public ReviewSummary(Long reviewId, String author, String subject, String content) {
        this.reviewId = reviewId;
        this.author = author;
        this.subject = subject;
        this.content = content;
    }

}
