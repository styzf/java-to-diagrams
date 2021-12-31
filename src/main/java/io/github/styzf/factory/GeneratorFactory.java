package io.github.styzf.factory;

import cn.hutool.core.collection.CollUtil;
import io.github.styzf.api.FileGenerate;
import io.github.styzf.dict.GeneratorFileType;
import io.github.styzf.generate.xmind.BaseXmindGenerator;
import io.github.styzf.util.common.Conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成容器工厂
 * @author styzf
 * @date 2021/12/15 21:43
 */
public class GeneratorFactory {

    private static final Map<GeneratorFileType, FileGenerate> GENERATOR_FACTORY = new HashMap<>();

    static {
        GENERATOR_FACTORY.put(GeneratorFileType.XMIND, new BaseXmindGenerator());
    }
    
    private static final List<FileGenerate> GENERATE_LIST = new ArrayList<>();
    
    public static List<FileGenerate> get() {
        if (CollUtil.isNotEmpty(GENERATE_LIST)) {
            return GENERATE_LIST;
        }
        
        String value = Conf.GENERATOR_LIST.get();
        List<GeneratorFileType> typeList = GeneratorFileType.getGeneratorTypeList(value);
        for (GeneratorFileType type: typeList) {
            GENERATE_LIST.add(GENERATOR_FACTORY.get(type));
        }
        return GENERATE_LIST;
    }
    
}
