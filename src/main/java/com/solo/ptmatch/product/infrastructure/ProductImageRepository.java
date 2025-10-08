package com.solo.ptmatch.product.infrastructure;

import com.solo.ptmatch.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // displayOrder 오름차순으로 특정 상품의 이미지 목록 조회
    List<ProductImage> findAllByProductIdOrderByDisplayOrderAsc(Long productId);

}
