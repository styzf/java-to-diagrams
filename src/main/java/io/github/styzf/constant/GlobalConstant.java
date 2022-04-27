package io.github.styzf.constant;

import java.util.regex.Pattern;

/**
 * @author styzf
 * @date 2022/4/27 21:38
 */
public interface GlobalConstant {
    /**
     * 泛型正则
     */
    Pattern GENERICS_PATTERN = Pattern.compile("<[^<]*?>");
    /**
     * 继承关键字
     */
    String EXTENDS = "extends";
    /**
     * 实现关键字
     */
    String IMPLEMENTS = "implements";
    /**
     * 空字符串
     */
    String EMPTY_STR = "";
    /**
     * 空格
     */
    String SPACE = " ";
}
