package com.supra.daas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.FileInfo;
import com.supra.daas.service.FileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<FileInfo>> list(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "dir", required = false) String dir) {
        return fileService.listFile(id, dir);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<FileInfo>> upload(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "dir", required = false) String dir,
            @RequestParam("name") String fileName,
            @RequestPart("files.path") String path) {
        return fileService.upload(id, dir, fileName, path);
    }

    @RequestMapping(value = "/mkdirs", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<FileInfo> mkdirs(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "dir", required = false) String dir) {
        return fileService.mkdirs(id, dir);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<FileInfo> delete(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "path") String path) {
        return fileService.delete(id, path);
    }

    @RequestMapping(value = "/move", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<FileInfo> move(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "oldPath") String oldPath,
            @RequestParam(name = "newPath") String newPath) {
        return fileService.move(id, oldPath, newPath);
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "path") String path,
            HttpServletRequest request, HttpServletResponse response) {
        fileService.download(id, path, request, response);
    }

}
