package io.github.styzf.parser.java.resolver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.parser.java.util.InfoUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 解析接口，抽象，实现
 *
 * @author styzf
 */
@Slf4j
public class OverResolver {
    public static final Set<TypeInfo> HAS_REL_CLASS = new HashSet<>();
    
    /**
     * 解析接口、抽象、实现
     */
    public static void parseOver(TypeDeclaration<?> type, ResolvedReferenceTypeDeclaration rt,
                                 TypeInfo classInfo, JavaContext javaContext) {
//        try {
//        if (type.isClassOrInterfaceDeclaration()) {
//            if (rt.isClass()) {
//                ResolvedClassDeclaration rcd = rt.asClass();
//                parseOver(javaContext, classInfo, type, rcd.getAllInterfaces());
//                parseOver(javaContext, classInfo, type, rcd.getAllSuperClasses());
//            }
//            if (rt.isInterface()) {
//                ResolvedInterfaceDeclaration rid = rt.asInterface();
//                parseOver(javaContext, classInfo, type, rid.getAllInterfacesExtended());
//            }
//        }
//        } catch (Exception e) {
//            log.error("over解析失败：{},{}", classInfo.sign, e.getMessage());
        StaticOverResolver.parseOver(javaContext, classInfo, rt,type);
//        }
    }
    
    /**
     * 解析重写方法
     */
    public static void parseOver(JavaContext javaContext, TypeInfo classInfo,
                                 TypeDeclaration<?> type,
                                 List<ResolvedReferenceType> parents) {
        parents.stream()
                .filter(parent -> !StrUtil.equals(parent.getQualifiedName(), "java.lang.Object"))
                .forEach(parent -> parseOver(javaContext, classInfo, type, parent));
    }
    
    /**
     * 解析重写方法
     */
    private static void parseOver(JavaContext javaContext, TypeInfo classInfo,
                                  TypeDeclaration<?> type, ResolvedReferenceType parent) {
        TypeInfo typeInfo = InfoUtils.getTypeInfo(javaContext, parent.getTypeDeclaration().get());
        classInfo.relInfo.put(typeInfo.sign, typeInfo);
        classInfo.relInfo.put(classInfo.sign, classInfo);
        typeInfo.relInfo.put(typeInfo.sign, typeInfo);
        typeInfo.relInfo.put(classInfo.sign, classInfo);
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
        HAS_REL_CLASS.forEach(OverResolver::parseRel);
    }
    
    private static void parseRel(TypeInfo classInfo) {
        // 实现方法挂载，形成链路
        LinkedHashMap<String, MemberInfo> memberInfoList = classInfo.memberInfo;
        memberInfoList.values().stream()
                .filter(iMemberInfo -> MemberEnum.isMethod(iMemberInfo.memberType))
                .forEach(cMemberInfo -> {
                    String classMethodName = cMemberInfo.name;
    
                    classInfo.relInfo.entrySet().forEach(typeInfoEntry -> {
                        typeInfoEntry.getValue().memberInfo.values().forEach(iMemberInfo -> {
                            if (iMemberInfo.name.equals(classMethodName)) {
                                iMemberInfo.callInfo.put(cMemberInfo.sign, cMemberInfo);
                                cMemberInfo.usageInfo.put(iMemberInfo.sign, iMemberInfo);
                            }
                        });
                    });
                });
    }
}
