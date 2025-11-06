package com.manus.seckill.product.service;

import com.manus.seckill.product.dto.ProductDTO;
import com.manus.seckill.product.entity.Product;

import java.util.List;

public interface ProductService {

    /**
     * Get product by ID
     */
    ProductDTO getProductById(Long id);

    /**
     * Get all products
     */
    List<ProductDTO> getAllProducts();

    /**
     * Create product
     */
    void createProduct(Product product);

    /**
     * Update product
     */
    void updateProduct(Product product);

    /**
     * Delete product
     */
    void deleteProduct(Long id);

    /**
     * Warm up cache for products
     */
    void warmUpCache();

}
