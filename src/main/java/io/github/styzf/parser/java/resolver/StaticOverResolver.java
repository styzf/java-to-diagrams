package io.github.styzf.parser.java.resolver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import io.github.styzf.constant.GlobalConstant;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.parser.java.util.InfoUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 静态解析接口，抽象，实现
 *
 * @author styzf
 */
@Slf4j
public class StaticOverResolver {
    public static final Set<TypeInfo> HAS_REL_CLASS = new HashSet<>();
    
    /**
     * 静态解析重写方法
     */
    public static void parseOver(JavaContext javaContext, TypeInfo classInfo,
                                 ResolvedReferenceTypeDeclaration rt, TypeDeclaration<?> type) {
        if (!type.isClassOrInterfaceDeclaration()) {
            return;
        }
        
        String fullName = "";
        if (rt.isClass()) {
            fullName = type.toString().substring(type.toString().indexOf("class "), type.toString().indexOf("{"));
        }
        if (rt.isInterface()) {
            fullName = type.toString().substring(type.toString().indexOf(" interface "), type.toString().indexOf("{"));
        }
        // 去除泛型
        while (GlobalConstant.GENERICS_PATTERN.matcher(fullName).find()) {
            fullName = GlobalConstant.GENERICS_PATTERN.matcher(fullName).replaceFirst("");
        }
        
        if (!fullName.contains(GlobalConstant.EXTENDS) && !fullName.contains(GlobalConstant.IMPLEMENTS)) {
            return;
        }
    
        fullName = fullName.replaceAll(GlobalConstant.EXTENDS, GlobalConstant.EMPTY_STR);
        fullName = fullName.replaceAll(GlobalConstant.IMPLEMENTS, GlobalConstant.EMPTY_STR);
    
        String[] parentArr = fullName.split(GlobalConstant.SPACE);
        if (parentArr.length <= 3) {
            return;
        }
    
        List<String> parentList = Arrays.stream(parentArr)
                .filter(StrUtil::isNotBlank).collect(Collectors.toList());
        parentList = parentList.subList(2, parentList.size());
    
        parentList.forEach(parentName -> {
            Set<String> importSet = classInfo.getImportSet(parentName);
            if (CollUtil.isEmpty(importSet)) {
                String packName = classInfo.getPackName();
                try {
                    String sign = packName + "." + parentName;
                    Class.forName(sign);
                    addRel(javaContext, classInfo, parentName, sign);
                } catch (ClassNotFoundException e) {
                    log.error("无法解析到对应的继承关系：" + classInfo.sign + " parentName:" + parentName);
                }
            } else {
                importSet.forEach(sign -> {
                    addRel(javaContext, classInfo, parentName, sign);
                });
            }
        });
    }
    
    /**
     * 添加关联关系
     */
    private static void addRel(JavaContext javaContext, TypeInfo classInfo, String parentName, String sign) {
        TypeInfo relInfo = javaContext.getType(sign);
        boolean needAddContext = false;
        if (ObjectUtil.isNull(relInfo)) {
            relInfo = new TypeInfo();
            needAddContext = true;
        }
        if (needAddContext) {
            javaContext.add(relInfo);
        }
        
        relInfo.name = parentName;
        relInfo.sign = sign;
        
        classInfo.relInfo.put(relInfo.sign, relInfo);
        classInfo.relInfo.put(classInfo.sign, classInfo);
        for (Map.Entry<String, TypeInfo> entry: classInfo.relInfo.entrySet()) {
            entry.getValue().relInfo.put(relInfo.sign, relInfo);
        }
        
        relInfo.relInfo.put(relInfo.sign, relInfo);
        relInfo.relInfo.put(classInfo.sign, classInfo);
        for (Map.Entry<String, TypeInfo> entry: relInfo.relInfo.entrySet()) {
            entry.getValue().relInfo.put(classInfo.sign, classInfo);
        }
        HAS_REL_CLASS.add(classInfo);
    }
    
    /**
     * 接口与抽象、实现，调用关系关联
     */
    public static void parseOver(JavaContext javaContext) {
        List<MemberInfo> notCallInfoList = javaContext.getMemberContext().values().stream()
                .filter(memberInfo -> CollUtil.isEmpty(memberInfo.callInfo))
                .filter(memberInfo -> CollUtil.isNotEmpty(memberInfo.classInfo.relInfo)).collect(Collectors.toList());
        for (MemberInfo memberInfo : notCallInfoList) {
            MemberInfo relMemInfo = memberInfo.classInfo.relInfo.values().stream()
                    .flatMap(relType -> relType.memberInfo.values().stream())
                    .filter(relMem -> relMem.getMethodName().equals(memberInfo.getMethodName())
                            && StrUtil.isNotBlank(relMem.getMethodName()))
                    .filter(relMem -> CollUtil.isNotEmpty(relMem.callInfo))
                    .max((o1, o2) -> o2.callInfo.size() - o1.callInfo.size()).orElse(null);
            if (ObjectUtil.isNull(relMemInfo)) {
                continue;
            }
            relMemInfo.usageInfo.put(memberInfo.sign, memberInfo);
            memberInfo.callInfo.put(relMemInfo.sign, relMemInfo);
        }
    }
    
    public static void parseRel() {
        HAS_REL_CLASS.forEach(StaticOverResolver::parseRel);
    }
    
    private static void parseRel(TypeInfo classInfo) {
        // 实现方法挂载，形成链路
        LinkedHashMap<String, MemberInfo> memberInfoList = classInfo.memberInfo;
        memberInfoList.values().stream()
                .filter(iMemberInfo -> MemberEnum.isMethod(iMemberInfo.memberType))
                .forEach(cMemberInfo -> {
                    String classMethodName = cMemberInfo.sign;
                    
                    classInfo.relInfo.forEach((key, value) -> value.memberInfo.values().stream()
                            .filter(iMemberInfo -> MemberEnum.isMethod(iMemberInfo.memberType))
                            .forEach(iMemberInfo -> {
                                if (iMemberInfo.sign.equals(classMethodName)) {
                                    iMemberInfo.callInfo.put(cMemberInfo.sign, cMemberInfo);
                                    cMemberInfo.usageInfo.put(iMemberInfo.sign, iMemberInfo);
                                }
                            }));
                });
    }
}
