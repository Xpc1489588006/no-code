package com.xpc.nocode.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 部署应用控制器
 * 处理部署应用的访问路由
 */
@Controller
public class DeployController {

    /**
     * 处理部署应用目录访问，重定向到 index.html
     * 例如: /api/deploy/abc123/ → /api/deploy/abc123/index.html
     *
     * @param deployKey 部署标识
     * @return 重定向到 index.html
     */
    @GetMapping("/api/deploy/{deployKey}/")
    public String deployAppRoot(@PathVariable String deployKey) {
        return "forward:/api/deploy/" + deployKey + "/index.html";
    }
}
