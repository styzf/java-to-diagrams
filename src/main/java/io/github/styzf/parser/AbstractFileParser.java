package io.github.styzf.parser;

import io.github.styzf.api.FileGenerate;
import io.github.styzf.api.FileParser;
import io.github.styzf.context.ParserContext;

import java.util.List;

/**
 * @author styzf
 * @date 2021/12/15 23:21
 */
public abstract class AbstractFileParser implements FileParser {
    
    protected List<FileGenerate> generateList;
    protected ParserContext context;
    
    @Override
    public FileParser setFileGenerate(List<FileGenerate> generateList) {
        this.generateList = generateList;
        return this;
    }
    
    /**
     * 统一结束后逻辑
     */
    @Override
    public void end() {
        generateList.forEach(generate -> generate.generate(context));
    }
}
