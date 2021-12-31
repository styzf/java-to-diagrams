package io.github.styzf.api;

import io.github.styzf.context.ParserContext;

/**
 * 文件生成器
 * @author styzf
 * @date 2021/12/15 20:42
 */
public interface FileGenerate<T extends ParserContext> {
    /**
     * 文件生成
     * @param t 要生成的解析结果数据
     */
    void generate(T t);
}
