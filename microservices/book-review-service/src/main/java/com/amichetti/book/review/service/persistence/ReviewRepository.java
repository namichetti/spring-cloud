package com.amichetti.book.review.service.persistence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ReviewRepository extends CrudRepository<ReviewEntity, Long> {

  @Transactional(readOnly = true)
  List<ReviewEntity> findByBookId(Long bookId);
}
