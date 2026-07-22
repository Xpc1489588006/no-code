package com.xpc.nocode.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户上传素材视图。
 */
@Data
public class UserAssetVO implements Serializable {

    /**
     * 原始文件名
     */
    private String name;

    /**
     * 可访问地址
     */
    private String url;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件类型
     */
    private String contentType;

    private static final long serialVersionUID = 1L;
}
