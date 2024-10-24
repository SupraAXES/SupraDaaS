package com.supra.daas.api;

import lombok.Data;

@Data
public class CommonResult<T> {
    private long code;
    private String message;
    private T data;

    protected CommonResult() {
    }

    protected CommonResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(200, "success", data);
    }

    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<T>(200, message, data);
    }

    public static <T> CommonResult<T> failed(long code, String message) {
        return new CommonResult<T>(code, message, null);
    }

    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(500, message, null);
    }

    public static <T> CommonResult<T> failed() {
        return failed("failed");
    }

    public Boolean isSuccess() {return code == 200; }

}