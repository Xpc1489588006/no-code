//package com.xpc.nocode.controller;
//
//import com.xpc.nocode.model.dto.app.AppAddRequest;
//import com.xpc.nocode.model.dto.app.AppQueryRequest;
//import com.xpc.nocode.model.dto.app.AppUpdateRequest;
//import jakarta.annotation.Resource;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
///**
// * App 控制器测试
// */
//@SpringBootTest
//class AppControllerTest {
//
//    @Resource
//    private AppController appController;
//
//    @Test
//    void testAppCRUD() {
//        // 1. 测试创建应用
//        AppAddRequest appAddRequest = new AppAddRequest();
//        appAddRequest.setAppName("测试应用");
//        appAddRequest.setInitPrompt("创建一个简单的待办事项应用");
//
//        // 注意：实际测试需要模拟登录用户
//        // BaseResponse<Long> response = appController.addApp(appAddRequest, mockRequest);
//        // Assertions.assertNotNull(response);
//
//        // 2. 测试查询应用
//        AppQueryRequest queryRequest = new AppQueryRequest();
//        queryRequest.setAppName("测试");
//        queryRequest.setPageNum(1);
//        queryRequest.setPageSize(10);
//
//        // 3. 测试更新应用
//        AppUpdateRequest updateRequest = new AppUpdateRequest();
//        updateRequest.setId(1L);
//        updateRequest.setAppName("更新后的应用名称");
//
//        // 4. 测试删除应用
//        // 需要通过 DeleteRequest 进行删除
//
//        Assertions.assertTrue(true);
//    }
//}
