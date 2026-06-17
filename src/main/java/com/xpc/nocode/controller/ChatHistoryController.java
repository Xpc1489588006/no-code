package com.xpc.nocode.controller;

import com.mybatisflex.core.paginate.Page;
import com.xpc.nocode.annotation.AuthCheck;
import com.xpc.nocode.common.BaseResponse;
import com.xpc.nocode.common.ResultUtils;
import com.xpc.nocode.constant.UserConstant;
import com.xpc.nocode.exception.BusinessException;
import com.xpc.nocode.exception.ErrorCode;
import com.xpc.nocode.exception.ThrowUtils;
import com.xpc.nocode.model.dto.chathistory.ChatHistoryAddRequest;
import com.xpc.nocode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xpc.nocode.model.entity.ChatHistory;
import com.xpc.nocode.model.entity.User;
import com.xpc.nocode.model.enums.ChatHistoryMessageTypeEnum;
import com.xpc.nocode.model.vo.ChatHistoryVO;
import com.xpc.nocode.service.ChatHistoryService;
import com.xpc.nocode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 对话历史 控制层。
 *
 * @author xpc
 */
@Slf4j
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 保存对话历史消息
     *
     * @param chatHistoryAddRequest 添加请求
     * @param request               HTTP 请求
     * @return 保存的对话历史 ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChatHistory(@RequestBody ChatHistoryAddRequest chatHistoryAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(chatHistoryAddRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        Long appId = chatHistoryAddRequest.getAppId();
        String messageType = chatHistoryAddRequest.getMessageType();
        String content = chatHistoryAddRequest.getContent();
        String errorMessage = chatHistoryAddRequest.getErrorMessage();

        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        ThrowUtils.throwIf(messageType == null || messageType.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "消息类型不能为空");

        // 验证消息类型
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        if (messageTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的消息类型");
        }

        // 根据消息类型保存
        ChatHistory chatHistory;
        if (ChatHistoryMessageTypeEnum.USER.equals(messageTypeEnum)) {
            ThrowUtils.throwIf(content == null || content.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "用户消息内容不能为空");
            chatHistory = chatHistoryService.saveUserMessage(appId, loginUser.getId(), content);
        } else if (ChatHistoryMessageTypeEnum.AI.equals(messageTypeEnum)) {
            // AI 消息可以是正常内容或错误信息
            if (content != null && !content.trim().isEmpty()) {
                chatHistory = chatHistoryService.saveAiMessage(appId, loginUser.getId(), content);
            } else if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                chatHistory = chatHistoryService.saveAiErrorMessage(appId, loginUser.getId(), errorMessage);
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI 消息内容或错误信息至少需要一个");
            }
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的消息类型");
        }

        return ResultUtils.success(chatHistory.getId());
    }

    /**
     * 分页查询应用的对话历史（游标查询，向前加载更多）
     * 仅应用创建者和管理员可见
     *
     * @param chatHistoryQueryRequest 查询请求
     * @param request                 HTTP 请求
     * @return 对话历史列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ChatHistoryVO>> listChatHistoryVOByPage(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest,
                                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        Page<ChatHistoryVO> chatHistoryVOPage = chatHistoryService.listChatHistoryVOByPageWithCursor(chatHistoryQueryRequest, loginUser);
        return ResultUtils.success(chatHistoryVOPage);
    }

    /**
     * 根据 id 查看对话历史详情
     *
     * @param id      对话历史 ID
     * @param request HTTP 请求
     * @return 对话历史详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<ChatHistoryVO> getChatHistoryVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        ChatHistory chatHistory = chatHistoryService.getById(id);
        ThrowUtils.throwIf(chatHistory == null, ErrorCode.NOT_FOUND_ERROR, "对话历史不存在");

        // 权限校验：仅本人或管理员可查看
        if (!chatHistory.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该对话历史");
        }

        return ResultUtils.success(chatHistoryService.getChatHistoryVO(chatHistory));
    }

    // region 管理员管理

    /**
     * 分页查询所有对话历史列表（管理员）
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryVO>> listChatHistoryVOByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Page<ChatHistoryVO> chatHistoryVOPage = chatHistoryService.listChatHistoryVOByPageForAdmin(chatHistoryQueryRequest);
        return ResultUtils.success(chatHistoryVOPage);
    }

    /**
     * 根据 id 删除对话历史（管理员）
     *
     * @param id 对话历史 ID
     * @return 删除结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteChatHistoryByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = chatHistoryService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 根据应用 id 删除所有对话历史（管理员）
     *
     * @param appId 应用 ID
     * @return 删除结果
     */
    @PostMapping("/admin/delete/byApp")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteChatHistoryByAppIdForAdmin(long appId) {
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = chatHistoryService.removeByAppId(appId);
        return ResultUtils.success(result);
    }

    // endregion
}
