package com.solo.ptmatch.product.infrastructure;

import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.domain.ProductLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

    // 다대일 or 일대일 관계라서 Fetch Join 사용 가능
    @Query(value = """
            select pl
            from ProductLike pl
            join fetch pl.product p
            join fetch p.trainerProfile tp
            join fetch tp.user
            where pl.user.id = :userId
            """)
    Page<ProductLike> findAllByUserIdWithProduct(@Param("userId") Long userId, Pageable pageable);

    long countByProduct(Product product);
}
