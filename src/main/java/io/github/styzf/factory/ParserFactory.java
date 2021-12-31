package io.github.styzf.factory;

import cn.hutool.core.collection.CollUtil;
import io.github.styzf.api.FileGenerate;
import io.github.styzf.api.FileParser;
import io.github.styzf.dict.GeneratorFileType;
import io.github.styzf.dict.ParserFileType;
import io.github.styzf.generate.xmind.BaseXmindGenerator;
import io.github.styzf.parser.java.JavaParser;
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
public class ParserFactory {

    private static final Map<ParserFileType, FileParser> PARSER_FACTORY = new HashMap<>();

    static {
        PARSER_FACTORY.put(ParserFileType.JAVA, new JavaParser());
    }
    
    private static final List<FileParser> PARSER_LIST = new ArrayList<>();
    
    public static List<FileParser> get() {
        if (CollUtil.isNotEmpty(PARSER_LIST)) {
            return PARSER_LIST;
        }
        
        String value = Conf.PARSER_LIST.get();
        List<ParserFileType> typeList = ParserFileType.getParserTypeList(value);
        for (ParserFileType type: typeList) {
            PARSER_LIST.add(PARSER_FACTORY.get(type));
        }
        return PARSER_LIST;
    }
    
}
