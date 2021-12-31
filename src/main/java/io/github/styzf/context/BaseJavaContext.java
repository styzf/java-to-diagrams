package io.github.styzf.context;

import io.github.styzf.context.javainfo.JavaInfo;
import io.github.styzf.context.javainfo.MemberInfo;
import io.github.styzf.context.javainfo.TypeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    /**
     * 继承或者实现链路容器
     */
    private static final Map<String, List<JavaInfo>> EXTENDS_CONTEXT = new HashMap<>();
    
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
        throw new RuntimeException("暂不支持");
    }
    
    @Override
    public Map<String, JavaInfo> getAll() {
        return CONTEXT;
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
}
