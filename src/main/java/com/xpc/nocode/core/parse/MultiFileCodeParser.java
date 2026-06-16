package com.xpc.nocode.core.parse;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xpc.nocode.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多文件代码解析器（HTML + CSS + JS）
 *
 * @author yupi
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        
        // 尝试从 JSON 格式提取
        boolean jsonExtracted = extractFromJson(codeContent, result);
        
        // 如果 JSON 提取失败，尝试从 Markdown 代码块提取
        if (!jsonExtracted) {
            String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
            String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
            String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);
            
            if (htmlCode != null && !htmlCode.trim().isEmpty()) {
                result.setHtmlCode(htmlCode.trim());
            }
            if (cssCode != null && !cssCode.trim().isEmpty()) {
                result.setCssCode(cssCode.trim());
            }
            if (jsCode != null && !jsCode.trim().isEmpty()) {
                result.setJsCode(jsCode.trim());
            }
        }
        
        return result;
    }

    /**
     * 从 JSON 格式中提取代码
     *
     * @param content 原始内容
     * @param result 结果对象
     * @return 是否成功提取
     */
    private boolean extractFromJson(String content, MultiFileCodeResult result) {
        try {
            JSONObject jsonObject = JSONUtil.parseObj(content);
            boolean extracted = false;
            
            if (jsonObject.containsKey("html")) {
                result.setHtmlCode(jsonObject.getStr("html"));
                extracted = true;
            }
            if (jsonObject.containsKey("css")) {
                result.setCssCode(jsonObject.getStr("css"));
                extracted = true;
            }
            if (jsonObject.containsKey("js") || jsonObject.containsKey("javascript")) {
                result.setJsCode(jsonObject.getStr("js", jsonObject.getStr("javascript")));
                extracted = true;
            }
            
            return extracted;
        } catch (Exception e) {
            // 不是 JSON 格式
            return false;
        }
    }

    /**
     * 根据正则模式提取代码
     *
     * @param content 原始内容
     * @param pattern 正则模式
     * @return 提取的代码
     */
    private String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
