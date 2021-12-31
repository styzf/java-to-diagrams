package io.github.styzf.parser.java.util;

import cn.hutool.core.util.ObjectUtil;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import io.github.styzf.code.parser.java.api.JavaParse;
import io.github.styzf.context.JavaContext;
import io.github.styzf.context.javainfo.JavaInfo;
import io.github.styzf.context.javainfo.MemberInfo;
import io.github.styzf.context.javainfo.TypeInfo;
import io.github.styzf.parser.java.dict.MemberEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 解析方法调用工具类
 * <br/>独立出来便于阅读和单独控制解析失败日志
 */
class MethodCallUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MethodCallUtils.class);

    private MethodCallUtils() {}

    /**
     * 解析成员中的方法调用
     */
    static void parseMethodCall(JavaContext javaContext, BodyDeclaration<?> m, TypeInfo classInfo, MemberInfo info) {
        for (MethodCallExpr expr : m.findAll(MethodCallExpr.class)) {
            ResolvedMethodDeclaration r;
            try {
                r = expr.resolve();
            } catch (Exception e) {
                // FIXME 目前已知解析失败：静态引用，::调用
                LOG.warn("resolve fail:\n  {}.{}({}.java:1) -> {}\n  {}",
                        classInfo.sign, info.name, classInfo.name, expr.getNameAsString(),
                        e.getLocalizedMessage());
                continue;
            }
            MemberInfo callInfo = new MemberInfo();
            // todo 该如何识别
            callInfo.memberType = MemberEnum.METHOD;
    
            ResolvedReferenceTypeDeclaration rt = r.declaringType();
            callInfo.classInfo = InfoUtils.getTypeInfo(javaContext, rt);
            InfoUtils.addMethodInfo(callInfo, r, null);
            
            MemberInfo member = javaContext.getMember(callInfo.sign);
            if (ObjectUtil.isNotNull(member)) {
                callInfo = member;
            } else {
                javaContext.add(callInfo);
            }
            
            info.callInfo.put(callInfo.sign, callInfo);
            callInfo.usageInfo.put(info.sign, info);
//            InfoUtils.forGetSet(classInfo, null, rt);
//            javaParses.forEach(v -> v.call(info, callInfo));
        }
    }
}
