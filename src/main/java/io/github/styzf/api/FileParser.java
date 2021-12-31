package io.github.styzf.api;

import io.github.styzf.context.ParserContext;

import java.io.File;
import java.util.List;

/**
 * 解析文件接口
 *
 * @author styzf
 * @date 2021/12/15 20:15
 */
public interface FileParser {
    
    /**
     * 设置生成器
     * @param parserContext
     * @return
     */
    FileParser setParserContext(ParserContext parserContext);
    
    /**
     * 设置生成器
     * @param generateList
     * @return
     */
    FileParser setFileGenerate(List<FileGenerate> generateList);
    
    /**
     * 解析
     */
    FileParser parser(File... files);
    
    /**
     * 结束时候调用，处理解析后逻辑
     */
    void end();
}
