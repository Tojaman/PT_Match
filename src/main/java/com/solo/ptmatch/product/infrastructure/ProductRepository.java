package com.solo.ptmatch.product.infrastructure;

import com.solo.ptmatch.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
