package io.github.styzf.util.common;

import cn.hutool.core.util.StrUtil;
import io.github.styzf.util.common.EnvUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置
 * <br/>前缀 parser_ 不设置成变量拼接是为了 IDE 能智能识别
 * @author styzf
 * @date 2021/12/15 20:59
 */
public enum Conf {

    // region 解析路径
    /** pom路径 */
    POM_PATH("pom_path", null),
    
    /** 解析路径 ;: 分隔 */
    PARSER_PATH("parser_path", null),

    /** 包含文件路径正则 */
    PARSER_PATH_INCLUDE("parser_path_include",
            // language="regexp"
            "\\.java$"),

    /** 排除文件路径正则 */
    PARSER_PATH_EXCLUDE("parser_path_exclude", null),

    // endregion 解析路径

    /** 生成文件后缀 */
    PARSER_OUT_SUFFIX("parser_out_suffix", "_code_parser"),

    // region 思维导图

    /** 思维导图显示类名 */
    @SuppressWarnings("SpellCheckingInspection")
    PARSER_XMIND_CLASS("parser_xmind_class", "true"),

    /** 思维导图显示标识符 */
    @SuppressWarnings("SpellCheckingInspection")
    PARSER_SHOW_SYMBOL("parser_xmind_symbol", "true"),

    /** 思维导图 指定开始的方法 包含正则 */
    @SuppressWarnings("SpellCheckingInspection")
    PARSER_XMIND_METHOD_INCLUDE("parser_xmind_method_include", ""),

    /** 思维导图 指定开始的方法 排除正则 */
    @SuppressWarnings("SpellCheckingInspection")
    PARSER_XMIND_METHOD_EXCLUDE("parser_xmind_method_exclude", "^java"),

    /** 思维导图 方法 排除正则 */
    @SuppressWarnings("SpellCheckingInspection")
    PARSER_XMIND_SHOW_OTHER_CALL("parser_xmind_show_other_call", "false"),
    
    /** 需要生成的文件类型列表 */
    GENERATOR_LIST("generator_list", "xmind"),

    /** 要解析的文件类型列表 */
    PARSER_LIST("parser_list", "java"),
    // endregion 思维导图
    ;

    private final String key;
    private final String defaultValue;

    private static final Map<String, String> CONFIG_MAP = new HashMap<>();
    
    Conf(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public static String confPath = "conf.properties";
    public static Properties prop;

    public String get() {
        String value = CONFIG_MAP.get(key);
        if (StrUtil.isBlank(value)) {
            value = EnvUtils.get(key, prop, defaultValue);
        }
        return value;
    }
    
    public static void set(String key, String value) {
        CONFIG_MAP.put(key, value);
    }
}
