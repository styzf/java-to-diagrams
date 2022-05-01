package io.github.styzf.parser.java.resolver;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.parser.java.dict.AccessEnum;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.parser.java.util.InfoUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MembersResolver {
    /**
     * 解析成员
     */
    public static void parseMembers(JavaContext javaContext, TypeInfo classInfo, TypeDeclaration<?> type,
                                    ResolvedReferenceTypeDeclaration rt) {
        List<FieldDeclaration> fieldList = type.findAll(FieldDeclaration.class);
        for (FieldDeclaration f : fieldList) {
            MemberInfo info = initInfo(classInfo);
            info.memberType = MemberEnum.FIELD;
            ResolvedFieldDeclaration r = f.resolve();
            info.sign = classInfo.sign + r.getName();
            InfoUtils.addFieldInfo(info, r, f);
    
            parserCall(javaContext, classInfo, f, info);
        }
        List<ConstructorDeclaration> constructorList = type.findAll(ConstructorDeclaration.class);
        for (ConstructorDeclaration c :constructorList) {
            MemberInfo info = initInfo(classInfo);
            info.memberType = MemberEnum.CONSTRUCTOR;
            ResolvedConstructorDeclaration r = c.resolve();
            InfoUtils.addMethodInfo(info, r, c);
    
            parserCall(javaContext, classInfo, c, info);
        }
        List<InitializerDeclaration> initializerList = type.findAll(InitializerDeclaration.class);
        for (InitializerDeclaration i :initializerList) {
            MemberInfo info = initInfo(classInfo);
            info.memberType = MemberEnum.STATIC;
            info.sign = classInfo.sign + "_static";
            info.name = classInfo.name + "_static";
            info.isStatic = i.isStatic();
            info.access = AccessEnum.NONE;
            
            parserCall(javaContext, classInfo, i, info);
        }
        List<MethodDeclaration> methodList = type.findAll(MethodDeclaration.class);
        for (MethodDeclaration m: methodList) {
            MemberInfo info = initInfo(classInfo);
            info.memberType = MemberEnum.METHOD;
            ResolvedMethodDeclaration r = m.resolve();
            InfoUtils.addMethodInfo(info, r, m);
            InfoUtils.forGetSet(info, type, rt);
    
            parserCall(javaContext, classInfo, m, info);
        }
    }
    
    /**
     * 初始化
     */
    private static MemberInfo initInfo(TypeInfo classInfo) {
        MemberInfo info = BeanUtil.copyProperties(classInfo, MemberInfo.class);
        info.classInfo = classInfo;
        return info;
    }
    
    /**
     * 解析调用方
     */
    private static void parserCall(JavaContext javaContext, TypeInfo classInfo,
                                   BodyDeclaration<?> m, MemberInfo info) {
        MemberInfo member = javaContext.getMember(info.sign);
        if (ObjectUtil.isNotNull(member)) {
            info.usageInfo = member.usageInfo;
            member.usageInfo.values().forEach(usage -> usage.callInfo.put(info.sign, info));
        }
    
        javaContext.add(info);
        classInfo.memberInfo.put(info.sign, info);
        
        if (MemberEnum.isMethod(info.memberType)) {
            MethodCallResolver.parseMethodCall(javaContext, m, classInfo, info);
        }
    }
}
