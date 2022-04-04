package io.github.styzf.parser.java.resolver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.parser.java.dict.TypeEnum;
import io.github.styzf.parser.java.util.InfoUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OverResolver {

    /**
     * 解析重写方法
     */
    public static void parseOver(JavaContext javaContext, TypeInfo classInfo,
                                 TypeDeclaration<?> type,
                                 List<ResolvedReferenceType> parents, boolean isClass) {
        parents.stream()
                .filter(parent -> ! StrUtil.equals(parent.getQualifiedName(),"java.lang.Object"))
                .forEach(parent -> parseOver(javaContext, classInfo, type, parent, isClass));
    }
    
    /**
     * 解析重写方法
     */
    private static void parseOver(JavaContext javaContext, TypeInfo classInfo,
                                  TypeDeclaration<?> type, ResolvedReferenceType parent, boolean isClass) {
        TypeInfo typeInfo = InfoUtils.getTypeInfo(javaContext, parent.getTypeDeclaration().get());
        classInfo.relInfo.put(typeInfo.sign, typeInfo);
        classInfo.relInfo.put(classInfo.sign, classInfo);
        typeInfo.relInfo.put(typeInfo.sign, typeInfo);
        typeInfo.relInfo.put(classInfo.sign, classInfo);
        
        // 实现方法挂载，形成链路
        LinkedHashMap<String, MemberInfo> memberInfoList = classInfo.memberInfo;
        if (isClass) {
            memberInfoList.values().stream()
                    .filter(iMemberInfo -> MemberEnum.isMethod(iMemberInfo.memberType))
                    .forEach(cMemberInfo -> {
                        String classMethodName = cMemberInfo.name;
                        typeInfo.memberInfo.values().forEach(iMemberInfo -> {
                            if (iMemberInfo.name.equals(classMethodName)) {
                                iMemberInfo.callInfo.put(cMemberInfo.sign, cMemberInfo);
                                cMemberInfo.usageInfo.put(iMemberInfo.sign, iMemberInfo);
                            }
                        });
                    });
        } else {
            memberInfoList.values().forEach(iMemberInfo -> {
                String interfaceMethodName = iMemberInfo.getMethodName();
                typeInfo.memberInfo.values().forEach(cMemberInfo -> {
                    if (cMemberInfo.getMethodName().equals(interfaceMethodName)) {
                        cMemberInfo.usageInfo.put(iMemberInfo.sign, iMemberInfo);
                        iMemberInfo.callInfo.put(cMemberInfo.sign, cMemberInfo);
                    }
                });
            });
        }
    }
    
    /**
     * 接口与抽象、实现，调用关系关联
     */
    public static void parseOver(JavaContext javaContext) {
        List<MemberInfo> notCallInfoList = javaContext.getMemberContext().values().stream()
                .filter(memberInfo -> CollUtil.isEmpty(memberInfo.callInfo))
                .filter(memberInfo -> CollUtil.isNotEmpty(memberInfo.classInfo.relInfo)).collect(Collectors.toList());
        for (MemberInfo memberInfo:notCallInfoList) {
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
}
