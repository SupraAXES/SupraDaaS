package com.supra.daas.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class FileUtil {

    // Create a global ExecutorService
    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);

    public static String getSuffix(String name) {
        if (EmptyUtils.isEmpty(name)) {
            return "";
        }

        int index = name.lastIndexOf('.');
        if (index > 0) {
            return name.substring(index + 1);
        }

        return "";
    }

    public static boolean saveFile(String path, String content) {
        try {
            return saveFile(path, content.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveFile(String path, byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        return saveFile(path, in, 6);
    }

    public static boolean saveFile(String path, InputStream in, long timeout) {
        StringBuffer tmp = new StringBuffer();
        tmp.append(path).append(".").append(System.currentTimeMillis()).append(".tmp");

        File file = new File(tmp.toString());

        log.info("save file: " + file.getAbsolutePath());

        boolean result = saveFile(file, in, timeout);
        if (result) {
            File oldFile = new File(path);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            file.renameTo(oldFile);
        }

        return result;
    }

    public static boolean saveFile(File tmpFile, String content) {
        try {
            return saveFile(tmpFile, new ByteArrayInputStream(content.getBytes("utf-8")), 6);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean saveFile(File tmpFile, InputStream in, long timeout) {
        FileWriteTask task = new FileWriteTask(tmpFile, in);
        Future<Boolean> future = executorService.submit(task);

        try {
            // Set a timeout (e.g., 10 seconds)
            TimeUnit timeUnit = TimeUnit.SECONDS;

            // Wait for the task to complete within the timeout
            return future.get(timeout, timeUnit);
        } catch (TimeoutException e) {
            // Handle timeout - cancel the task
            future.cancel(true);
            log.debug("WriteFile TimeoutException: " + tmpFile.getAbsolutePath());
        } catch (Exception e) {
            log.debug("WriteFile xception: " + tmpFile.getAbsolutePath());
        }

        return false;
    }

    public static String loadFileContent(String path) {
        try {
            byte[] data = loadFileData(path);
            if (data == null) {
                return null;
            }
            return new String(data, "utf-8");
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }

    public static String loadResourceContent(String name) {
        try {
            Resource resource = new ClassPathResource(name);
            InputStream is = resource.getInputStream();

            byte[] data = readContent(is);

            return new String(data, "utf-8");
        } catch (IOException e) {
//            e.printStackTrace();
        }

        return null;
    }

    public static byte[] loadFileData(String path) {
        File file = new File(path);

        try {
            if (! file.exists()) {
                return null;
            }

            return readContent(new FileInputStream(file));
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return null;
    }

    private static byte[] readContent(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] data = new byte[1024];
        int count = 0;
        while ((count = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, count);
        }

        is.close();
        bos.close();

        return bos.toByteArray();
    }

    public static void deleteFile(File file) {
        // if (file.isDirectory()) {
        //     String[] files = file.list();
        //     for (String item : files) {
        //         deleteFile(new File(file, item));
        //     }
        //     file.delete();
        // }
        // else {
        //     file.delete();
        // }
        if (! file.exists()) {
            return;
        }

        log.info("delete file: " + file.getAbsolutePath());

        FileDeleteTask task = new FileDeleteTask(file);
        Future<Boolean> future = executorService.submit(task);

        try {
            // Set a timeout (e.g., 10 seconds)
            TimeUnit timeUnit = TimeUnit.SECONDS;

            // Wait for the task to complete within the timeout
            future.get(file.isDirectory() ? 60 : 6, timeUnit);
        } catch (TimeoutException e) {
            // Handle timeout - cancel the task
            future.cancel(true);
            log.debug("DeleteFile TimeoutException: " + file.getAbsolutePath());
        } catch (Exception e) {
            log.debug("DeleteFile Exception: " + file.getAbsolutePath());
        }
    }

    public static class FileDeleteTask implements Callable<Boolean>  {

        private File file;

        public FileDeleteTask(File file) {
            this.file = file;
        }

        private void delFile(File file) {
            file.delete();
        }

        private void delDir(File file) {
            String[] files = file.list();
            for (String item : files) {
                File tmp = new File(file, item);
                if (tmp.isDirectory()) {
                    delDir(tmp);
                } else {
                    delFile(tmp);
                }
            }
        }

        @Override
        public Boolean call() throws Exception {
            try {
                if (file.isDirectory()) {
                    delDir(file);
                } else {
                    delFile(file);
                }
                return true;
            } catch (Exception e) {
//                e.printStackTrace();
                return false;
            }
        }
    }

    public static class FileWriteTask implements Callable<Boolean>  {

        private File tmpFile;
        private InputStream in;

        public FileWriteTask(File tmpFile, InputStream in) {
            this.tmpFile = tmpFile;
            this.in = in;
        }

        @Override
        public Boolean call() throws Exception {
            File dir = tmpFile.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }

            try {
                FileOutputStream fos = new FileOutputStream(tmpFile);

                byte[] data = new byte[4096];
                int length = 0;

                while ((length = in.read(data)) >= 0) {
                    fos.write(data, 0, length);
                }
                fos.flush();
                fos.close();

                return true;
            } catch (Exception e) {
    //            e.printStackTrace();
                return false;
            } finally {
                in.close();
            }
        }
    
        
    } 
}
