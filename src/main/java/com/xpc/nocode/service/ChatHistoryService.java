package com.xpc.nocode.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xpc.nocode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xpc.nocode.model.entity.ChatHistory;
import com.xpc.nocode.model.entity.User;
import com.xpc.nocode.model.vo.ChatHistoryVO;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author xpc
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 获取对话历史 VO
     *
     * @param chatHistory 对话历史实体
     * @return 对话历史 VO
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 获取对话历史 VO 列表
     *
     * @param chatHistoryList 对话历史实体列表
     * @return 对话历史 VO 列表
     */
    List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList);

    /**
     * 获取查询包装器
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页获取应用的对话历史 VO 列表（游标查询，向前加载更多）
     *
     * @param chatHistoryQueryRequest 查询请求
     * @param loginUser               当前登录用户
     * @return 对话历史 VO 分页列表
     */
    Page<ChatHistoryVO> listChatHistoryVOByPageWithCursor(ChatHistoryQueryRequest chatHistoryQueryRequest, User loginUser);

    /**
     * 管理员分页获取所有对话历史 VO 列表
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史 VO 分页列表
     */
    Page<ChatHistoryVO> listChatHistoryVOByPageForAdmin(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 保存用户消息
     *
     * @param appId       应用 id
     * @param userId      用户 id
     * @param message     消息内容
     * @return 保存的对话历史
     */
    ChatHistory saveUserMessage(Long appId, Long userId, String message);

    /**
     * 保存 AI 消息
     *
     * @param appId       应用 id
     * @param userId      用户 id
     * @param message     消息内容
     * @return 保存的对话历史
     */
    ChatHistory saveAiMessage(Long appId, Long userId, String message);

    /**
     * 保存 AI 错误消息
     *
     * @param appId        应用 id
     * @param userId       用户 id
     * @param errorMessage 错误信息
     * @return 保存的对话历史
     */
    ChatHistory saveAiErrorMessage(Long appId, Long userId, String errorMessage);

    /**
     * 根据应用 id 删除对话历史
     *
     * @param appId 应用 id
     * @return 删除结果
     */
    boolean removeByAppId(Long appId);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
