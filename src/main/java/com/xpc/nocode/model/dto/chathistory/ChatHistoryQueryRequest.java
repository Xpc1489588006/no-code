package com.xpc.nocode.model.dto.chathistory;

import com.xpc.nocode.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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
     * 创建用户 id
     */
    private Long userId;

    /**
     * 游标（用于向前加载更多历史记录，使用 createTime）
     */
    private LocalDateTime cursor;

    private static final long serialVersionUID = 1L;
}
