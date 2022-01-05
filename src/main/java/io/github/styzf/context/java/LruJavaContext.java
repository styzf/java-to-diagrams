package io.github.styzf.context.java;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import io.github.styzf.context.java.javainfo.JavaInfo;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.util.common.FileUtils;

import java.io.*;
import java.security.spec.ECField;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author styzf
 * @date 2022/1/5 19:48
 */
public class LruJavaContext implements JavaContext {
    
    private final JavaContext delegate;
    private LinkedHashMap<String, String> keyMap;
    private String eldestKey;
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]|\\?");
    
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
        try {
            cycleKeyList(javaInfo.sign);
        } catch (Exception ignored) {
            String s = FILE_NAME_PATTERN.matcher(javaInfo.sign).replaceAll("");
            System.out.println("移除失败：" + s);
        }
    }
    
    private void cycleKeyList(String key) {
        keyMap.put(key, key);
        if (eldestKey != null) {
            try {
                key = FILE_NAME_PATTERN.matcher(key).replaceAll("");
                JavaInfo removeInfo = delegate.get(key);
                File file = FileUtil.file(key);
                FileUtil.touch(file);
                IoUtil.writeObjects(new FileOutputStream(file), true, removeInfo);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("文件未找到");
            } finally {
                eldestKey = null;
            }
            delegate.remove(eldestKey);
        }
    }
    
    @Override
    public JavaInfo get(String key) {
        keyMap.get(key);
        JavaInfo javaInfo = delegate.get(key);
        if (ObjectUtil.isNotNull(javaInfo)) {
            return javaInfo;
        }
        try(InputStream in = new FileInputStream(FileUtil.file(key))) {
            javaInfo = IoUtil.readObj(in, JavaInfo.class);
        } catch (Exception e) {
            return null;
        }
        return javaInfo;
    }
    
    @Override
    public TypeInfo getType(String key) {
        keyMap.get(key);
        TypeInfo type = delegate.getType(key);
        if (ObjectUtil.isNotNull(type)) {
            return type;
        }
        try(InputStream in = new FileInputStream(FileUtils.CLASS_PATH + key)) {
            type = IoUtil.readObj(in, TypeInfo.class);
        } catch (Exception e) {
            return null;
        }
        return type;
    }
    
    @Override
    public MemberInfo getMember(String key) {
        keyMap.get(key);
        MemberInfo member = delegate.getMember(key);
        if (ObjectUtil.isNotNull(member)) {
            return member;
        }
        try(InputStream in = new FileInputStream(FileUtils.CLASS_PATH + key)) {
            member = IoUtil.readObj(in, MemberInfo.class);
        } catch (Exception e) {
            return null;
        }
        return member;
    }
    
    @Override
    public Map<String, MemberInfo> getMemberContext() {
        // todo 怎么持续性的获取持久化的数据
        return delegate.getMemberContext();
    }
    
    @Override
    public JavaInfo remove(String key) {
        return delegate.remove(key);
    }
}
