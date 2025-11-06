package com.manus.seckill.seckill.controller;

import com.manus.seckill.seckill.common.Result;
import com.manus.seckill.seckill.dto.SeckillRequest;
import com.manus.seckill.seckill.dto.SeckillResult;
import com.manus.seckill.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/path/{activityId}")
    public Result<String> getSeckillPath(@PathVariable Long activityId,
                                         @RequestHeader("Authorization") String token) {
        try {
            // Extract userId from token (simplified, in real scenario use JWT parser)
            Long userId = extractUserIdFromToken(token);
            String path = seckillService.getSeckillPath(activityId, userId);
            return Result.success(path);
        } catch (Exception e) {
            log.error("Failed to get seckill path", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/doSeckill/{path}")
    public Result<SeckillResult> doSeckill(@PathVariable String path,
                                           @RequestBody SeckillRequest request,
                                           @RequestHeader("Authorization") String token) {
        try {
            Long userId = extractUserIdFromToken(token);
            SeckillResult result = seckillService.doSeckill(request.getActivityId(), userId, path);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to execute seckill", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/result/{activityId}")
    public Result<SeckillResult> getSeckillResult(@PathVariable Long activityId,
                                                  @RequestHeader("Authorization") String token) {
        try {
            Long userId = extractUserIdFromToken(token);
            SeckillResult result = seckillService.getSeckillResult(activityId, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to get seckill result", e);
            return Result.error(e.getMessage());
        }
    }

    private Long extractUserIdFromToken(String token) {
        // Simplified extraction - in real scenario, use JWT parser
        // This is a placeholder that should be replaced with actual JWT parsing
        try {
            String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            // For now, return a default user ID
            // In production, parse the JWT token properly
            return 1L;
        } catch (Exception e) {
            log.error("Failed to extract user ID from token", e);
            throw new RuntimeException("Invalid token");
        }
    }

}
