package io.github.styzf.context.java;

import io.github.styzf.context.java.javainfo.JavaInfo;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author styzf
 * @date 2022/1/5 19:48
 */
public class LruJavaContext implements JavaContext {
    
    private final JavaContext delegate;
    private Map<String, String> keyMap;
    private String eldestKey;
    
    public LruJavaContext(JavaContext delegate) {
        this.delegate = delegate;
        setSize(1024);// todo 有空改成配置的
    }
    
    public void setSize(final int size) {
        keyMap = new LinkedHashMap<String, String>(size, .75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                boolean tooBig = size() > size;
                if (tooBig) {
                    eldestKey = eldest.getKey();
                }
                return tooBig;
            }
        };
    }
    
    @Override
    public void add(JavaInfo javaInfo) {
        delegate.add(javaInfo);
        cycleKeyList(javaInfo.sign);
    }
    
    private void cycleKeyList(String key) {
        keyMap.put(key, key);
        if (eldestKey != null) {
            // todo 写库的操作，或者持久化文件保存
            delegate.remove(eldestKey);
            eldestKey = null;
        }
    }
    
    @Override
    public JavaInfo get(String key) {
        keyMap.get(key);
        return delegate.get(key);
    }
    
    @Override
    public TypeInfo getType(String key) {
        keyMap.get(key);
        return delegate.getType(key);
    }
    
    @Override
    public MemberInfo getMember(String key) {
        keyMap.get(key);
        return delegate.getMember(key);
    }
    
    @Override
    public Map<String, MemberInfo> getMemberContext() {
        return delegate.getMemberContext();
    }
    
    @Override
    public JavaInfo remove(String key) {
        return delegate.remove(key);
    }
}
