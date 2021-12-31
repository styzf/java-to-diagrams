package io.github.styzf.context;

import io.github.styzf.context.javainfo.JavaInfo;
import io.github.styzf.context.javainfo.MemberInfo;
import io.github.styzf.context.javainfo.TypeInfo;

import java.util.Map;

/**
 * 后续支持存储数据库
 * 为以后装饰者模式预留
 * @author styzf
 * @date 2021/12/15 21:28
 */
public interface JavaContext extends ParserContext{
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
     * 获取所有数据
     * @return
     */
    Map<String, JavaInfo> getAll();
    
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
     * @return
     */
    Map<String, MemberInfo> getMemberContext();
}
