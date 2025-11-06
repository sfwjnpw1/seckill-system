package com.manus.seckill.auth.exception;

import com.manus.seckill.auth.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("An error occurred", e);
        return Result.error(500, "Internal server error: " + e.getMessage());
    }

}
