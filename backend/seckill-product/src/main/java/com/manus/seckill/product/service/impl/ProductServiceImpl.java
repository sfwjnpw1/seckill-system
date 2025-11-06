package com.manus.seckill.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manus.seckill.product.dto.ProductDTO;
import com.manus.seckill.product.entity.Product;
import com.manus.seckill.product.mapper.ProductMapper;
import com.manus.seckill.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_CACHE_KEY = "product:";
    private static final String PRODUCT_LIST_CACHE_KEY = "product:list";
    private static final long CACHE_EXPIRATION = 3600; // 1 hour

    @Override
    public ProductDTO getProductById(Long id) {
        // Try to get from cache first
        String cacheKey = PRODUCT_CACHE_KEY + id;
        ProductDTO cached = (ProductDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Get from database
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        // Convert to DTO and cache
        ProductDTO dto = convertToDTO(product);
        redisTemplate.opsForValue().set(cacheKey, dto, CACHE_EXPIRATION, TimeUnit.SECONDS);

        return dto;
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        // Try to get from cache first
        List<ProductDTO> cached = (List<ProductDTO>) redisTemplate.opsForValue().get(PRODUCT_LIST_CACHE_KEY);
        if (cached != null) {
            return cached;
        }

        // Get from database
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Cache the list
        redisTemplate.opsForValue().set(PRODUCT_LIST_CACHE_KEY, dtos, CACHE_EXPIRATION, TimeUnit.SECONDS);

        return dtos;
    }

    @Override
    public void createProduct(Product product) {
        productMapper.insert(product);
        // Invalidate cache
        redisTemplate.delete(PRODUCT_LIST_CACHE_KEY);
        log.info("Product created: {}", product.getId());
    }

    @Override
    public void updateProduct(Product product) {
        productMapper.updateById(product);
        // Invalidate cache
        String cacheKey = PRODUCT_CACHE_KEY + product.getId();
        redisTemplate.delete(cacheKey);
        redisTemplate.delete(PRODUCT_LIST_CACHE_KEY);
        log.info("Product updated: {}", product.getId());
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
        // Invalidate cache
        String cacheKey = PRODUCT_CACHE_KEY + id;
        redisTemplate.delete(cacheKey);
        redisTemplate.delete(PRODUCT_LIST_CACHE_KEY);
        log.info("Product deleted: {}", id);
    }

    @Override
    public void warmUpCache() {
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
        for (Product product : products) {
            String cacheKey = PRODUCT_CACHE_KEY + product.getId();
            ProductDTO dto = convertToDTO(product);
            redisTemplate.opsForValue().set(cacheKey, dto, CACHE_EXPIRATION, TimeUnit.SECONDS);
        }
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        redisTemplate.opsForValue().set(PRODUCT_LIST_CACHE_KEY, dtos, CACHE_EXPIRATION, TimeUnit.SECONDS);
        log.info("Product cache warmed up");
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setDescription(product.getDescription());
        dto.setStatus(product.getStatus());
        return dto;
    }

}
