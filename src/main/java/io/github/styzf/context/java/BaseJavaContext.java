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
     * 成员容器
     */
    private static final Map<String, MemberInfo> MEMBER_CONTEXT = new HashMap<>();
    
    @Override
    public void add(JavaInfo javaInfo) {
        CONTEXT.put(javaInfo.sign, javaInfo);
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
        JavaInfo javaInfo = CONTEXT.get(key);
        if (javaInfo instanceof TypeInfo) {
            return (TypeInfo) javaInfo;
        }
        return null;
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
        MEMBER_CONTEXT.remove(key);
        return CONTEXT.remove(key);
    }
}
