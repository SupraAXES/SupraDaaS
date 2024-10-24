package com.supra.daas.service;

import java.io.File;
import java.util.List;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.FileInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FileService {
    
    CommonResult<List<FileInfo>> listFile(String uuid, String dir);

    CommonResult<List<FileInfo>> upload(String uuid, String dir, String fileName, String path);

    CommonResult<FileInfo> mkdirs(String uuid, String dir);

    CommonResult<FileInfo> delete(String uuid, String path);

    CommonResult<FileInfo> move(String uuid, String oldPath, String newPath);

    CommonResult<String> prepareDownload(String uuid, String path);

    void download(File file, HttpServletRequest request, HttpServletResponse response);

    void download(String uuid, String path, HttpServletRequest request, HttpServletResponse response);


}
