package com.collegestore.unistore.repository;

import com.collegestore.unistore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}