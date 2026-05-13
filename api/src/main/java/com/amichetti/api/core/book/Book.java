package com.amichetti.api.core.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
@AllArgsConstructor
public class Book{

    private Long bookId;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private String serviceAddress;

    public Book() {
        bookId = 0L;
        author = null;
        description = null;
        title = null;
        isbn = null;
        serviceAddress = null;
    }
}