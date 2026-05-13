package com.amichetti.api.composite.book;


import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
public class BookAggregate {

    private final Long bookId;
    private final String title;
    private final String author;
    private final String description;
    private final String isbn;
    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;

    public BookAggregate() {
        bookId = 0L;
        title = null;
        author = null;
        description = null;
        isbn = null;
        recommendations = null;
        reviews = null;
        serviceAddresses = null;
    }


    public BookAggregate(
            Long bookId,
            String title,
            String description,
            List<RecommendationSummary> recommendationSummaries,
            List<ReviewSummary> reviewSummaries,
            ServiceAddresses serviceAddresses,
            String author,
            String isbn
    ) {
        this.bookId = bookId;
        this.reviews=reviewSummaries;
        this.title = title;
        this.description=description;
        this.recommendations = recommendationSummaries;
        this.serviceAddresses = serviceAddresses;
        this.author=author;
        this.isbn=isbn;
    }

}



