package com.xpc.nocode.model.dto.chathistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话历史创建请求
 */
@Data
public class ChatHistoryAddRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 错误信息（AI 回复失败时记录）
     */
    private String errorMessage;

    private static final long serialVersionUID = 1L;
}
