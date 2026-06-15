package com.xpc.nocode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xpc.nocode.core.AiCodeGeneratorFacade;
import com.xpc.nocode.exception.BusinessException;
import com.xpc.nocode.exception.ErrorCode;
import com.xpc.nocode.exception.ThrowUtils;
import com.xpc.nocode.mapper.AppMapper;
import com.xpc.nocode.model.dto.app.AppQueryRequest;
import com.xpc.nocode.model.entity.App;
import com.xpc.nocode.model.entity.User;
import com.xpc.nocode.model.enums.CodeGenTypeEnum;
import com.xpc.nocode.model.vo.AppVO;
import com.xpc.nocode.service.AppService;
import com.xpc.nocode.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询创建用户信息
        Long userId = app.getUserId();
        if (userId != null && userId > 0) {
            appVO.setUser(userService.getUserVO(userService.getById(userId)));
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return appList.stream().map(this::getAppVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create();
        // 根据各个字段进行筛选
        if (id != null) {
            queryWrapper.eq("id", id);
        }
        if (appName != null) {
            queryWrapper.like("appName", appName);
        }
        if (cover != null) {
            queryWrapper.eq("cover", cover);
        }
        if (initPrompt != null) {
            queryWrapper.like("initPrompt", initPrompt);
        }
        if (codeGenType != null) {
            queryWrapper.eq("codeGenType", codeGenType);
        }
        if (deployKey != null) {
            queryWrapper.eq("deployKey", deployKey);
        }
        if (priority != null) {
            queryWrapper.eq("priority", priority);
        }
        if (userId != null) {
            queryWrapper.eq("userId", userId);
        }
        // 排序
        if (sortField != null) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return queryWrapper;
    }

    @Override
    public Page<AppVO> listAppVOByPageForAdmin(AppQueryRequest appQueryRequest) {
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        // 管理员分页查询，不限制每页数量
        Page<App> appPage = this.page(Page.of(pageNum, pageSize),
                this.getQueryWrapper(appQueryRequest));
        // 转换为 VO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    @Override
    public Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, long userId) {
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户 ID 无效");
        }
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = Math.min(appQueryRequest.getPageSize(), 20); // 最多 20 个
        // 只查询当前用户的应用
        appQueryRequest.setUserId(userId);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize),
                this.getQueryWrapper(appQueryRequest));
        // 转换为 VO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    @Override
    public Page<AppVO> listFeaturedAppVOByPage(AppQueryRequest appQueryRequest) {
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = Math.min(appQueryRequest.getPageSize(), 20); // 最多 20 个
        // 精选应用：按优先级降序排列，优先显示高优先级的应用
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        // 如果没有指定排序字段，默认按优先级降序
        if (appQueryRequest.getSortField() == null) {
            queryWrapper.orderBy("priority", false);
        }
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 转换为 VO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

}
