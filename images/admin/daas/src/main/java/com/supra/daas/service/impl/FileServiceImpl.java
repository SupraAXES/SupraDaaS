package com.supra.daas.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.FileInfo;
import com.supra.daas.domain.ResourceRule;
import com.supra.daas.domain.SessionInfo;
import com.supra.daas.service.FileService;
import com.supra.daas.service.SessionService;
import com.supra.daas.util.EmptyUtils;
import com.supra.daas.util.FileUtil;
import com.supra.daas.util.MD5Util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
 
    @Autowired
    private SessionService sessionService;

    @Override
    public CommonResult<List<FileInfo>> listFile(
            String uuid, String dir) {
        SessionInfo session = sessionService.getSession(uuid);
        if (session == null) {
            return CommonResult.failed("Session Disconnect");
        }

        File filesDir = new File(session.getPath(), EmptyUtils.getNotEmpty(dir));

        List<FileInfo> fileInfos = new ArrayList<>();

        String[] files = filesDir.list((dir1, name) -> ! name.startsWith(".")
                && ! name.matches("^core\\.\\d+$")
                && ! name.startsWith("~$"));

        if (files != null) {
            for (String item : files) {
                File file = new File(filesDir, item);
                fileInfos.add(new FileInfo(file, dir + "/" + item));
            }
        }

        return CommonResult.success(fileInfos);
    }

    @Override
    public CommonResult<List<FileInfo>> upload(
            String uuid, String dir, String fileName, String path) {
        SessionInfo session = sessionService.getSession(uuid);
        if (session == null) {
            return CommonResult.failed("Session Disconnect");
        }

        ResourceRule rule = session.getRule();
        if (rule == null || EmptyUtils.isEmpty(rule.getUpload())) {
            return CommonResult.failed("Permission Error");
        }

        File filesDir = new File(session.getPath(), EmptyUtils.getNotEmpty(dir));
        if (! filesDir.exists()) {
            filesDir.mkdirs();
        }

        log.info("upload file: " + fileName + " to " + filesDir.getPath());

        List<FileInfo> fileInfos = null;
        try {
            Path tmpPath = Paths.get(new File(path).toURI());
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(tmpPath);
            permissions.add(PosixFilePermission.OTHERS_WRITE);
            permissions.add(PosixFilePermission.GROUP_WRITE);
            Files.setPosixFilePermissions(tmpPath, permissions);

            fileInfos = new ArrayList<>();
            final String name = fileName.startsWith(".") ? 
                "i" + fileName : fileName;

            final File item = new File(filesDir, name);
            final Path itemPath = Paths.get(item.toURI());
            Files.copy(tmpPath, itemPath, 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.delete(tmpPath);

            log.info("upload file path: " + path + " to " + item.getPath());

            fileInfos.add(new FileInfo(item, name));

            chmodSupra(item);
        } catch (Exception e) {
            log.warn("upload file failed: ", e);
            return CommonResult.failed();
        }

        return CommonResult.success(fileInfos);
    }

    @Override
    public CommonResult<FileInfo> mkdirs(String uuid, String dir) {
        SessionInfo session = sessionService.getSession(uuid);
        if (session == null) {
            return CommonResult.failed("Session Disconnect");
        }

        ResourceRule rule = session.getRule();
        if (rule == null || EmptyUtils.isEmpty(rule.getUpload())) {
            return CommonResult.failed("Permission Error");
        }

        File filesDir = new File(session.getPath());

        File file = new File( filesDir.getPath() + "/" + dir);
        if (! file.exists()) {
            file.mkdirs();

            chmodSupra(file);
        }

        return CommonResult.success(new FileInfo(file,
                dir.substring(0, dir.length() - file.getName().length())));
    }

    @Override
    public CommonResult<FileInfo> delete(String uuid, String path) {
        SessionInfo session = sessionService.getSession(uuid);
        if (session == null) {
            return CommonResult.failed("Session Disconnect");
        }

        ResourceRule rule = session.getRule();
        if (rule == null || EmptyUtils.isEmpty(rule.getUpload())) {
            return CommonResult.failed("Permission Error");
        }

        File filesDir = new File(session.getPath());

        File file = new File(filesDir.getPath() + "/" + path);
        FileUtil.deleteFile(file);

        return CommonResult.success(null);
    }

    @Override
    public CommonResult<FileInfo> move(String uuid, String oldPath, String newPath) {
        SessionInfo session = sessionService.getSession(uuid);
        if (session == null) {
            return CommonResult.failed("Session Disconnect");
        }

        ResourceRule rule = session.getRule();
        if (rule == null || EmptyUtils.isEmpty(rule.getUpload())) {
            return CommonResult.failed("Permission Error");
        }

        File filesDir = new File(session.getPath());

        File oldFile = new File(filesDir.getPath() + "/" + oldPath);
        File newFile = new File(filesDir.getPath() + "/" + newPath);

        if (! oldFile.exists()) {
            return CommonResult.failed("Old file not exits");
        }

        if (oldFile.renameTo(newFile)) {
            return CommonResult.success(new FileInfo(newFile, newPath));
        } else {
            return CommonResult.failed("File rename failed");
        }
    }

    @Override
    public CommonResult<String> prepareDownload(String uuid, String path) {
        SessionInfo session = sessionService.getSession(uuid);
        if (session == null) {
            return CommonResult.failed("Session Disconnect");
        }

        ResourceRule rule = session.getRule();
        if (rule == null || EmptyUtils.isEmpty(rule.getDownload())) {
            return CommonResult.failed("Permission Error");
        }

        File filesDir = new File(session.getPath());

        File file = new File(filesDir.getPath() + "/" + path);
        if (file.isDirectory()) {
            return CommonResult.failed();
        }

        try {
            Path tmpPath = Paths.get(file.toURI());
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(tmpPath);
            if (! permissions.contains(PosixFilePermission.OTHERS_READ)
                    || ! permissions.contains(PosixFilePermission.GROUP_READ)) {
                permissions.add(PosixFilePermission.OTHERS_READ);
                permissions.add(PosixFilePermission.GROUP_READ);
                Files.setPosixFilePermissions(tmpPath, permissions);
            }

            do {
                File parentFile = file.getParentFile();
                if (parentFile == null || "/".equals(parentFile.getName())) {
                    break;
                }

                tmpPath = Paths.get(parentFile.toURI());
                permissions = Files.getPosixFilePermissions(tmpPath);

                if (! permissions.contains(PosixFilePermission.OTHERS_READ)
                        || ! permissions.contains(PosixFilePermission.OTHERS_EXECUTE)
                        || ! permissions.contains(PosixFilePermission.GROUP_READ)
                        || ! permissions.contains(PosixFilePermission.GROUP_EXECUTE)) {
                    permissions.add(PosixFilePermission.OTHERS_READ);
                    permissions.add(PosixFilePermission.OTHERS_EXECUTE);
                    permissions.add(PosixFilePermission.GROUP_READ);
                    permissions.add(PosixFilePermission.GROUP_EXECUTE);
                    Files.setPosixFilePermissions(tmpPath, permissions);
                }

                if ("archive".equals(parentFile.getName())) {
                    break;
                }

                file = parentFile;
            } while(true);
        } catch (Exception e) {
        }

        return CommonResult.success("");
    }

    @Override
    public void download(File file, HttpServletRequest request, HttpServletResponse response) {
        if (file.exists()) {
            downloadFile(file, request, response);
        } else {
            response.setStatus(404);
        }
    }

    @Override
    public void download(String uuid, String path, HttpServletRequest request, HttpServletResponse response) {
        SessionInfo session = sessionService.getSession(uuid);
        if (session == null) {
            return;
        }

        File filesDir = new File(session.getPath());

        File file = new File(filesDir.getPath() + "/" + path);

        if (file.isDirectory()) {
            return;
        }

        downloadFile(file, request, response);
    }

    private void downloadFile(File file, HttpServletRequest request, HttpServletResponse response) {
        ServletContext context = request.getServletContext();
        // get MIME type of the file
        String mimeType = context.getMimeType(file.getPath());
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }

        response.setContentType(mimeType);

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
        response.setHeader(headerKey, headerValue);

        response.setHeader("Accept-Ranges", "bytes");

        long downloadSize = file.length();
        long fromPos = 0, toPos = 0;

        String etag = MD5Util.md5(file.getPath()).substring(0, 8)
                + "-" + file.lastModified();
        response.setHeader("ETag", etag);

        String ifNoneMatch = request.getHeader("If-None-Match");
        if (request.getHeader("Range") == null
                || ifNoneMatch == null
                || ! ifNoneMatch.equals(etag)) {
            response.setHeader("Content-Length", downloadSize + "");
        } else {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            String range = request.getHeader("Range");
            String bytes = range.replaceAll("bytes=", "");
            String[] ary = bytes.split("-");
            fromPos = Long.parseLong(ary[0]);
            if (ary.length == 2) {
                toPos = Long.parseLong(ary[1]);
            }
            int size;
            if (toPos > fromPos) {
                size = (int) (toPos - fromPos);
            } else {
                size = (int) (downloadSize - fromPos);
            }
            response.setHeader("Content-Length", size + "");
            downloadSize = size;
        }

        // Copy the stream to the response's output stream.
        RandomAccessFile in = null;
        OutputStream out = null;
        try {
            in = new RandomAccessFile(file, "rw");
            if (fromPos > 0) {
                in.seek(fromPos);
            }
            int bufLen = (int) (downloadSize < 2048 ? downloadSize : 2048);
            byte[] buffer = new byte[bufLen];
            int num;
            int count = 0;
            int blockCount = 0;

            response.flushBuffer();

            out = response.getOutputStream();
            while ((num = in.read(buffer)) != -1) {
                out.write(buffer, 0, num);
                count += num;
                if (downloadSize - count < bufLen) {
                    bufLen = (int) (downloadSize-count);
                    if(bufLen==0){
                        break;
                    }
                    buffer = new byte[bufLen];
                }

                if ((count / (1 << 15)) != blockCount) {
                    blockCount = count / (1 << 15);
                    out.flush();
                    response.flushBuffer();
                }
            }
            response.flushBuffer();
        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }
    }
    
    protected void chmodSupra(File file) {
        try {
            FileSystem fs = FileSystems.getDefault();

            PosixFileAttributeView view = Files.getFileAttributeView(
                file.toPath(), PosixFileAttributeView.class);
            UserPrincipal owner = fs.getUserPrincipalLookupService()
                .lookupPrincipalByName("supra");
            view.setOwner(owner);

            GroupPrincipal group = fs.getUserPrincipalLookupService()
                .lookupPrincipalByGroupName("supra");
            view.setGroup(group);
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }
}
