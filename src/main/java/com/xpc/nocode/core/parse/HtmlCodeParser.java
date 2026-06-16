package com.xpc.nocode.core.parse;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xpc.nocode.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 单文件代码解析器
 *
 * @author yupi
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        String htmlCode = null;
        
        // 尝试从 JSON 格式提取
        htmlCode = extractFromJson(codeContent);
        
        // 如果 JSON 提取失败，尝试从 Markdown 代码块提取
        if (htmlCode == null || htmlCode.trim().isEmpty()) {
            htmlCode = extractHtmlCode(codeContent);
        }
        
        // 如果都没找到，将整个内容作为HTML
        if (htmlCode == null || htmlCode.trim().isEmpty()) {
            htmlCode = codeContent.trim();
        }
        
        result.setHtmlCode(htmlCode.trim());
        return result;
    }

    /**
     * 从 JSON 格式中提取 HTML 代码
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractFromJson(String content) {
        try {
            // 尝试解析为 JSON
            JSONObject jsonObject = JSONUtil.parseObj(content);
            // 获取 html 字段
            if (jsonObject.containsKey("html")) {
                return jsonObject.getStr("html");
            }
        } catch (Exception e) {
            // 不是 JSON 格式，返回 null
            return null;
        }
        return null;
    }

    /**
     * 提取HTML代码内容
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
