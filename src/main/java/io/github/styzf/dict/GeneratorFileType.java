package io.github.styzf.dict;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 要生成的文件类型
 * @author styzf
 * @date 2021/12/15 21:07
 */
public enum GeneratorFileType {
    /** 生成文档类型说明 */
    @SuppressWarnings("SpellCheckingInspection")
    XMIND(1, "xmind", "xmind类型生成") ,
    ;
    
    GeneratorFileType(int key, String value, String desc) {
        this.key = key;
        this.value = value;
        this.desc = desc;
    }
    
    private final int key;
    private final String value;
    private final String desc;
    
    /**
     * 根据配置获取对应的类型参数
     * @param value
     * @return
     */
    public static List<GeneratorFileType> getGeneratorTypeList(String value) {
        List<GeneratorFileType> list = new ArrayList<>();
        if (StrUtil.isBlank(value)) {
            return list;
        }
        for (String typeValue: value.split(",")) {
            for (GeneratorFileType type: values()) {
                if (type.value.equals(typeValue)) {
                    list.add(type);
                    break;
                }
            }
        }
        return list;
    }
}
