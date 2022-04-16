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

@Slf4j
public class MembersResolver {
    /**
     * 解析成员
     */
    public static void parseMembers(JavaContext javaContext, TypeInfo classInfo, TypeDeclaration<?> type,
                                    ResolvedReferenceTypeDeclaration rt) {
        for (BodyDeclaration<?> m : type.getMembers()) {
            MemberInfo info = BeanUtil.copyProperties(classInfo, MemberInfo.class);
            info.classInfo = classInfo;
            
            if (m.isMethodDeclaration()) {
                info.memberType = MemberEnum.METHOD;
                MethodDeclaration d = m.asMethodDeclaration();
                ResolvedMethodDeclaration r = d.resolve();
                InfoUtils.addMethodInfo(info, r, d);
                InfoUtils.forGetSet(info, type, rt);
//                javaParses.forEach(v -> v.member(info));
            } else if (m.isConstructorDeclaration()) {
                info.memberType = MemberEnum.CONSTRUCTOR;
                ConstructorDeclaration d = m.asConstructorDeclaration();
                ResolvedConstructorDeclaration r = d.resolve();
                InfoUtils.addMethodInfo(info, r, d);
//                javaParses.forEach(v -> v.member(info));
            } else if (m.isFieldDeclaration()) {
                info.memberType = MemberEnum.FIELD;
                FieldDeclaration d = m.asFieldDeclaration();
                ResolvedFieldDeclaration r = d.resolve();
                info.sign = classInfo.sign + r.getName();
                InfoUtils.addFieldInfo(info, r, d);
//                javaParses.forEach(v -> v.member(info));
            } else if (m.isInitializerDeclaration()) {
                info.memberType = MemberEnum.STATIC;
                InitializerDeclaration d = m.asInitializerDeclaration();
                info.sign = classInfo.sign + "_static";
                info.name = classInfo.name + "_static";
                info.isStatic = d.isStatic();
                info.access = AccessEnum.NONE;
//                javaParses.forEach(v -> v.member(info));
//            } else if (m.isTypeDeclaration()) {
//                TypeResolver.parserType(m.asTypeDeclaration(), javaContext);
//                continue;
            } else {
                // TODO 这个会导致成员没有添加到类里面去
                log.warn("skip: {}", m);
                continue;
            }
            // TODO 这里的解析是否可以先解析出名字，然后再进行处理
             MemberInfo member = javaContext.getMember(info.sign);
            if (ObjectUtil.isNotNull(member)) {
                info.usageInfo = member.usageInfo;
                member.usageInfo.values().stream().forEach(usage -> usage.callInfo.put(info.sign, info));
            }
            
            javaContext.add(info);
            classInfo.memberInfo.put(info.sign, info);
            parserCall(javaContext, classInfo, m, info);
        }
    }
    
    /**
     * 解析调用方
     */
    private static void parserCall(JavaContext javaContext, TypeInfo classInfo, BodyDeclaration<?> m, MemberInfo info) {
        if (MemberEnum.isMethod(info.memberType)) {
            MethodCallResolver.parseMethodCall(javaContext, m, classInfo, info);
        }
    }
}
