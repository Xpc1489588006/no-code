package com.xpc.nocode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xpc.nocode.exception.BusinessException;
import com.xpc.nocode.exception.ErrorCode;
import com.xpc.nocode.exception.ThrowUtils;
import com.xpc.nocode.mapper.AppMapper;
import com.xpc.nocode.mapper.ChatHistoryMapper;
import com.xpc.nocode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xpc.nocode.model.entity.App;
import com.xpc.nocode.model.entity.ChatHistory;
import com.xpc.nocode.model.entity.User;
import com.xpc.nocode.model.enums.ChatHistoryMessageTypeEnum;
import com.xpc.nocode.model.vo.ChatHistoryVO;
import com.xpc.nocode.model.vo.UserVO;
import com.xpc.nocode.service.ChatHistoryService;
import com.xpc.nocode.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话历史 服务实现类。
 *
 * @author xpc
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends com.mybatisflex.spring.service.impl.ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private UserService userService;

    @Resource
    private AppMapper appMapper;

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);
        // 关联查询创建用户信息
        Long userId = chatHistory.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                chatHistoryVO.setUser(userVO);
            }
        }
        return chatHistoryVO;
    }

    @Override
    public List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
        return chatHistoryList.stream().map(this::getChatHistoryVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = chatHistoryQueryRequest.getId();
        Long appId = chatHistoryQueryRequest.getAppId();
        String messageType = chatHistoryQueryRequest.getMessageType();
        String content = chatHistoryQueryRequest.getContent();
        Long userId = chatHistoryQueryRequest.getUserId();
        java.time.LocalDateTime cursor = chatHistoryQueryRequest.getCursor();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create();
        // 根据各个字段进行筛选
        if (id != null) {
            queryWrapper.eq("id", id);
        }
        if (appId != null) {
            queryWrapper.eq("appId", appId);
        }
        if (messageType != null) {
            queryWrapper.eq("messageType", messageType);
        }
        if (content != null) {
            queryWrapper.like("content", content);
        }
        if (userId != null) {
            queryWrapper.eq("userId", userId);
        }
        // 游标查询：向前加载更多历史记录（查询比游标 createTime 更早的记录）
        if (cursor != null) {
            queryWrapper.lt("createTime", cursor);
        }
        // 排序：默认按 createTime 降序（最新的在前）
        if (sortField != null) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistoryVO> listChatHistoryVOByPageWithCursor(ChatHistoryQueryRequest chatHistoryQueryRequest, User loginUser) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        Long appId = chatHistoryQueryRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");

        // 检查应用是否存在
        App app = appMapper.selectOneById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 权限校验：仅应用创建者和管理员可查看对话历史
        if (!app.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }

        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = Math.min(chatHistoryQueryRequest.getPageSize(), 20); // 最多 20 条

        Page<ChatHistory> chatHistoryPage = this.page(Page.of(pageNum, pageSize),
                this.getQueryWrapper(chatHistoryQueryRequest));
        
        // 转换为 VO
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(pageNum, pageSize, chatHistoryPage.getTotalRow());
        List<ChatHistoryVO> chatHistoryVOList = this.getChatHistoryVOList(chatHistoryPage.getRecords());
        chatHistoryVOPage.setRecords(chatHistoryVOList);
        return chatHistoryVOPage;
    }

    @Override
    public Page<ChatHistoryVO> listChatHistoryVOByPageForAdmin(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        
        Page<ChatHistory> chatHistoryPage = this.page(Page.of(pageNum, pageSize),
                this.getQueryWrapper(chatHistoryQueryRequest));
        
        // 转换为 VO
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(pageNum, pageSize, chatHistoryPage.getTotalRow());
        List<ChatHistoryVO> chatHistoryVOList = this.getChatHistoryVOList(chatHistoryPage.getRecords());
        chatHistoryVOPage.setRecords(chatHistoryVOList);
        return chatHistoryVOPage;
    }

    @Override
    public ChatHistory saveUserMessage(Long appId, Long userId, String message) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 无效");
        ThrowUtils.throwIf(message == null || message.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "消息内容不能为空");

        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .messageType(ChatHistoryMessageTypeEnum.USER.getValue())
                .content(message)
                .build();
        
        boolean result = this.save(chatHistory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存用户消息失败");
        return chatHistory;
    }

    @Override
    public ChatHistory saveAiMessage(Long appId, Long userId, String message) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 无效");
        ThrowUtils.throwIf(message == null || message.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "消息内容不能为空");

        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .messageType(ChatHistoryMessageTypeEnum.AI.getValue())
                .content(message)
                .build();
        
        boolean result = this.save(chatHistory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存 AI 消息失败");
        return chatHistory;
    }

    @Override
    public ChatHistory saveAiErrorMessage(Long appId, Long userId, String errorMessage) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 无效");
        ThrowUtils.throwIf(errorMessage == null || errorMessage.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "错误信息不能为空");

        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .messageType(ChatHistoryMessageTypeEnum.AI.getValue())
                .errorMessage(errorMessage)
                .build();
        
        boolean result = this.save(chatHistory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存 AI 错误消息失败");
        return chatHistory;
    }

    @Override
    public boolean removeByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        
        return this.remove(queryWrapper);
    }
}
