package io.github.styzf.context.java;

import io.github.styzf.context.java.javainfo.JavaInfo;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author styzf
 * @date 2021/12/15 21:37
 */
public class BaseJavaContext implements JavaContext {
    private static final Map<String, JavaInfo> CONTEXT = new HashMap<>();
    
    /**
     * 类、接口、抽象类、枚举容器
     */
    private static final Map<String, TypeInfo> CLASS_CONTEXT = new HashMap<>();
    
    /**
     * 成员容器
     */
    private static final Map<String, MemberInfo> MEMBER_CONTEXT = new HashMap<>();
    
    @Override
    public void add(JavaInfo javaInfo) {
        CONTEXT.put(javaInfo.sign, javaInfo);
        if (javaInfo instanceof TypeInfo) {
            CLASS_CONTEXT.put(javaInfo.sign, (TypeInfo) javaInfo);
            return;
        }
        if (javaInfo instanceof MemberInfo) {
            MEMBER_CONTEXT.put(javaInfo.sign, (MemberInfo) javaInfo);
        }
    }
    
    @Override
    public JavaInfo get(String key) {
        return CONTEXT.get(key);
    }
    
    @Override
    public TypeInfo getType(String key) {
        return CLASS_CONTEXT.get(key);
    }
    
    @Override
    public MemberInfo getMember(String key) {
        return MEMBER_CONTEXT.get(key);
    }
    
    @Override
    public Map<String, MemberInfo> getMemberContext() {
        return MEMBER_CONTEXT;
    }
    
    @Override
    public JavaInfo remove(String key) {
        CLASS_CONTEXT.remove(key);
        MEMBER_CONTEXT.remove(key);
        return CONTEXT.remove(key);
    }
}
