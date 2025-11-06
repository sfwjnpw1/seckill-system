package com.manus.seckill.product.controller;

import com.manus.seckill.product.common.Result;
import com.manus.seckill.product.dto.SeckillActivityDTO;
import com.manus.seckill.product.entity.SeckillActivity;
import com.manus.seckill.product.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product/seckill")
public class SeckillActivityController {

    @Autowired
    private SeckillActivityService seckillActivityService;

    @GetMapping("/{id}")
    public Result<SeckillActivityDTO> getActivityById(@PathVariable Long id) {
        try {
            SeckillActivityDTO activity = seckillActivityService.getActivityById(id);
            return Result.success(activity);
        } catch (Exception e) {
            log.error("Failed to get seckill activity", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<SeckillActivityDTO>> getActiveActivities() {
        try {
            List<SeckillActivityDTO> activities = seckillActivityService.getActiveActivities();
            return Result.success(activities);
        } catch (Exception e) {
            log.error("Failed to get active activities", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<String> createActivity(@RequestBody SeckillActivity activity) {
        try {
            seckillActivityService.createActivity(activity);
            return Result.success("Seckill activity created successfully");
        } catch (Exception e) {
            log.error("Failed to create seckill activity", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<String> updateActivity(@RequestBody SeckillActivity activity) {
        try {
            seckillActivityService.updateActivity(activity);
            return Result.success("Seckill activity updated successfully");
        } catch (Exception e) {
            log.error("Failed to update seckill activity", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteActivity(@PathVariable Long id) {
        try {
            seckillActivityService.deleteActivity(id);
            return Result.success("Seckill activity deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete seckill activity", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/warmup")
    public Result<String> warmUpCache() {
        try {
            seckillActivityService.warmUpCache();
            return Result.success("Cache warmed up successfully");
        } catch (Exception e) {
            log.error("Failed to warm up cache", e);
            return Result.error(e.getMessage());
        }
    }

}
