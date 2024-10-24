package com.supra.daas.component;

import com.supra.daas.api.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public CommonResult base(IllegalStateException exception) {
//        exception.printStackTrace();
        log.error(exception.getMessage());
        return CommonResult.failed(exception.getMessage());
    }

    @ExceptionHandler(NumberFormatException.class)
    public CommonResult base(NumberFormatException exception) {
//        exception.printStackTrace();
        log.error(exception.getMessage());
        return CommonResult.failed(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult base(IllegalArgumentException exception) {
//        exception.printStackTrace();
        log.error(exception.getMessage());
        return CommonResult.failed(exception.getMessage());
    }
}
