package com.solo.ptmatch.product.infrastructure;

import com.solo.ptmatch.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
