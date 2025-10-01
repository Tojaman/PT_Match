package com.solo.ptmatch.product.infrastructure;

import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.domain.ProductCategory;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 조건: 제목 or 카테고리 or 가격범위로 상품 검색
    // 정렬: 가격순/등록일순 + 오름차순/내림차순(Pageable 객체 내부에 Sort 객체 포함) -> order by 쿼리 JPA
    @Query("""
            select p
            from Product p
            where (:titleKeyword is null or p.title like concat('%', :titleKeyword, '%'))
              and (:category is null or p.category = :category)
              and (:minPrice is null or p.pricePerSession >= :minPrice)
              and (:maxPrice is null or p.pricePerSession <= :maxPrice)
            """)
    Page<Product>  searchByTitleAndCategoryAndPrice(
            @Param("titleKeyword") String titleKeyword,
            @Param("category") ProductCategory category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
