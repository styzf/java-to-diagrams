package io.github.styzf.parser.java.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import io.github.styzf.code.parser.java.api.JavaParse;
import io.github.styzf.context.JavaContext;
import io.github.styzf.context.javainfo.JavaInfo;
import io.github.styzf.context.javainfo.MemberInfo;
import io.github.styzf.context.javainfo.TypeInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class OverUtils {

    /**
     * 解析重写方法
     */
    public static void parseOver(JavaContext javaContext, TypeInfo classInfo,
                                 TypeDeclaration<?> type,
                                 List<ResolvedReferenceType> parents) {
        parents.stream()
                .filter(parent -> ! StrUtil.equals(parent.getQualifiedName(),"java.lang.Object"))
                .forEach(parent -> parseOver(javaContext, classInfo, type, parent));
    }
    
    private static void parseOver(JavaContext javaContext, TypeInfo classInfo,
                                  TypeDeclaration<?> type, ResolvedReferenceType parent) {
        TypeInfo typeInfo = InfoUtils.getTypeInfo(javaContext, parent.getTypeDeclaration());
        classInfo.relInfo.put(typeInfo.sign, typeInfo);
        classInfo.relInfo.put(classInfo.sign, classInfo);
        typeInfo.relInfo = classInfo.relInfo;
    }
    
    /**
     * 最终处理调用链路问题
     * @param javaContext
     */
    public static void parseOver(JavaContext javaContext) {
        List<MemberInfo> notCallInfoList = javaContext.getMemberContext().values().stream()
                .filter(memberInfo -> CollUtil.isEmpty(memberInfo.callInfo))
                .filter(memberInfo -> CollUtil.isEmpty(memberInfo.classInfo.relInfo)).collect(Collectors.toList());
        for (MemberInfo memberInfo:notCallInfoList) {
            MemberInfo relMemInfo = memberInfo.classInfo.relInfo.values().stream()
                    .flatMap(relType -> relType.memberInfo.values().stream())
                    .filter(relMem -> relMem.methodName.equals(memberInfo.methodName))
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
