package com.xpc.nocode.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xpc.nocode.model.dto.app.AppQueryRequest;
import com.xpc.nocode.model.entity.App;
import com.xpc.nocode.model.entity.User;
import com.xpc.nocode.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用 VO
     *
     * @param app 应用实体
     * @return 应用 VO
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用 VO 列表
     *
     * @param appList 应用实体列表
     * @return 应用 VO 列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询包装器
     *
     * @param appQueryRequest 查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 分页获取应用 VO 列表（仅管理员）
     *
     * @param appQueryRequest 查询请求
     * @return 应用 VO 分页列表
     */
    Page<AppVO> listAppVOByPageForAdmin(AppQueryRequest appQueryRequest);

    /**
     * 分页获取用户自己的应用 VO 列表
     *
     * @param appQueryRequest 查询请求
     * @param userId          用户 ID
     * @return 应用 VO 分页列表
     */
    Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, long userId);

    /**
     * 分页获取精选应用 VO 列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用 VO 分页列表
     */
    Page<AppVO> listFeaturedAppVOByPage(AppQueryRequest appQueryRequest);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);
}
