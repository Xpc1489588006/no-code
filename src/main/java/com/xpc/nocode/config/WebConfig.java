package com.xpc.nocode.config;

import com.xpc.nocode.constant.AppConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置代码生成的静态资源访问（预览）
        // 访问路径: /static/{codeGenType}_{appId}/** 
        // 实际路径: tmp/code_output/{codeGenType}_{appId}/**
        registry.addResourceHandler("/static/**").addResourceLocations("file:" + AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator);
        
        // 配置部署应用的静态资源访问
        // 访问路径: /deploy/{deployKey}/** 
        // 实际路径: tmp/code_deploy/{deployKey}/**
        registry.addResourceHandler("/deploy/**").addResourceLocations("file:" + AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator);
    }
}
