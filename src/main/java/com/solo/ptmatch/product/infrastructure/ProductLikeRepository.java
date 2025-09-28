package com.solo.ptmatch.product.infrastructure;

import com.solo.ptmatch.product.domain.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
}
