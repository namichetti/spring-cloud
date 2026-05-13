package com.amichetti.book.service.services;

import com.amichetti.api.core.book.Book;
import com.amichetti.book.service.persistence.BookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BookMapper {

  @Mappings({
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Book entityToApi(BookEntity entity);

  @Mappings({
    @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true)
  })
  BookEntity apiToEntity(Book api);
}
