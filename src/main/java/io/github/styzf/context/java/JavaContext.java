package io.github.styzf.context.java;

import io.github.styzf.context.ParserContext;
import io.github.styzf.context.java.javainfo.JavaInfo;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;

import java.util.Map;

/**
 * 后续支持存储数据库
 * 为以后装饰者模式预留
 * @author styzf
 * @date 2021/12/15 21:28
 */
public interface JavaContext extends ParserContext {
    /**
     * 添加
     * @param javaInfo
     */
    void add(JavaInfo javaInfo);
    
    /**
     * 获取
     * @param key
     * @return
     */
    JavaInfo get(String key);
    
    /**
     * 获取类
     * @param key
     * @return
     */
    TypeInfo getType(String key);
    
    /**
     * 获取成员
     * @param key
     * @return
     */
    MemberInfo getMember(String key);
    
    /**
     * 获取所有成员
     * todo 想办法优化掉
     * todo 要实现的话，增加一个判断的方法，多次加载
     */
    Map<String, MemberInfo> getMemberContext();
    
    /**
     * 删除
     */
    JavaInfo remove(String key);
}
