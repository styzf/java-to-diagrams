package io.github.styzf.code.parser.java.api;

import io.github.styzf.code.parser.java.api.bean.JavaInfo;

/**
 * Java 解析接口
 * <br>没有r是为了类名不重复
 */
public interface JavaParse {
    /** 类处理 */
    void type(JavaInfo classInfo);

    /** 方法处理 */
    void member(JavaInfo info);

    /** 方法调用关系处理 */
    void call(JavaInfo usageInfo, JavaInfo callInfo);

    /** 实现接口关系处理 */
    void over(JavaInfo overInfo, JavaInfo parentInfo);

    /** 结束处理 */
    void end();
}
