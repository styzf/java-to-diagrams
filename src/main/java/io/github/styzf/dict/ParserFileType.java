package io.github.styzf.dict;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析的文件类型
 * @author styzf
 * @date 2021/12/15 21:07
 */
public enum ParserFileType {
    /** 解析类型说明 */
    @SuppressWarnings("SpellCheckingInspection")
    JAVA(1, "java", "java类型解析") ,
    ;
    
    ParserFileType(int key, String value, String desc) {
        this.value = value;
    }
    
    private final String value;
    
    /**
     * 根据配置获取对应的类型参数
     * @param value
     * @return
     */
    public static List<ParserFileType> getParserTypeList(String value) {
        List<ParserFileType> list = new ArrayList<>();
        if (StrUtil.isBlank(value)) {
            return list;
        }
        for (String typeValue: value.split(",")) {
            for (ParserFileType type: values()) {
                if (type.value.equals(typeValue)) {
                    list.add(type);
                    break;
                }
            }
        }
        return list;
    }
}
