package com.ecommerce.productservice.domain.repository;

import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.repository.custom.ProductRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
}
