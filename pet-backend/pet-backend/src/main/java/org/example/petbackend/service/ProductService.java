package org.example.petbackend.service;

import org.example.petbackend.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String mainCategory);
}
