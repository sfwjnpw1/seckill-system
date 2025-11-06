package com.manus.seckill.product.controller;

import com.manus.seckill.product.common.Result;
import com.manus.seckill.product.dto.ProductDTO;
import com.manus.seckill.product.entity.Product;
import com.manus.seckill.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public Result<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductById(id);
            return Result.success(product);
        } catch (Exception e) {
            log.error("Failed to get product", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<ProductDTO>> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            return Result.success(products);
        } catch (Exception e) {
            log.error("Failed to get products", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<String> createProduct(@RequestBody Product product) {
        try {
            productService.createProduct(product);
            return Result.success("Product created successfully");
        } catch (Exception e) {
            log.error("Failed to create product", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<String> updateProduct(@RequestBody Product product) {
        try {
            productService.updateProduct(product);
            return Result.success("Product updated successfully");
        } catch (Exception e) {
            log.error("Failed to update product", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return Result.success("Product deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete product", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/warmup")
    public Result<String> warmUpCache() {
        try {
            productService.warmUpCache();
            return Result.success("Cache warmed up successfully");
        } catch (Exception e) {
            log.error("Failed to warm up cache", e);
            return Result.error(e.getMessage());
        }
    }

}
