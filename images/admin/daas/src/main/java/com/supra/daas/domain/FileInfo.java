package com.supra.daas.domain;

import java.io.File;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FileInfo {
    private String id;

    private String route;

    private String name;

    private String path;

    private String ownerId;

    private String ownerName;

    private Boolean isDirectory;

    private Long createTime;

    private Long length;

    public FileInfo(File file, String path) {
        this.createTime = file.lastModified();
        this.length = file.length();
        this.isDirectory = file.isDirectory();
        this.name = file.getName();
        this.path = path;
    }
}
