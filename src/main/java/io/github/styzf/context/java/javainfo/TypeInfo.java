package io.github.styzf.context.java.javainfo;

import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;

/**
 * 类信息
 * @author styzf
 * @date 2021/12/16 19:39
 */
public class TypeInfo extends JavaInfo {
    
    /** 非静态调用时返回小写开头的类名 */
    public String className() {
        return isStatic ? name : lowFirstName;
    }
    
    /** 成员 */
    public LinkedHashMap<String, MemberInfo> memberInfo = new LinkedHashMap<>();
    
    /** 接口 */
//    public LinkedHashMap<String, TypeInfo> faceInfo = new LinkedHashMap<>();
    /** 实现 */
//    public LinkedHashMap<String, TypeInfo> implInfo = new LinkedHashMap<>();
    /** 继承父 */
//    public TypeInfo parentInfo;
    /** 继承子 */
//    public LinkedHashMap<String, TypeInfo> childInfo = new LinkedHashMap<>();
    /**
     * 调用关系
     */
    public LinkedHashMap<String, TypeInfo> relInfo = new LinkedHashMap<>();
}
