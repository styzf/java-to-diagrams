package io.github.styzf.parser.java.resolver;

import cn.hutool.core.util.ObjectUtil;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.parser.java.util.InfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 解析方法调用工具类
 * <br/>独立出来便于阅读和单独控制解析失败日志
 */
class MethodCallResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MethodCallResolver.class);

    private MethodCallResolver() {}

    /**
     * 解析成员中的方法调用
     */
    static void parseMethodCall(JavaContext javaContext, BodyDeclaration<?> m, TypeInfo classInfo, MemberInfo info) {
        List<Expression> exprNodeList = m.findAll(Expression.class)
                .stream().filter(expr -> !expr.isFieldAccessExpr()).collect(Collectors.toList());
        for (Expression expr : exprNodeList) {
            // TODO 这里需要做下处理，先解析入参的方法调用，然后链式调用，应该从第一个方法开始解析，就是顺序不一样
            if (expr.isMethodCallExpr()) {
                if (exprMethodCall(javaContext, classInfo, info, expr)) {
                    continue;
                }
            }
            if (expr.isLambdaExpr() && expr.asLambdaExpr().getExpressionBody().isPresent()) {
                Expression expression = expr.asLambdaExpr().getExpressionBody().get();
                if (expression.isMethodCallExpr()) {
                    parseMethodCall(javaContext, classInfo, info, expression.asMethodCallExpr().resolve());
                }
            }
            if (expr.isMethodReferenceExpr()) {
                try {
                    parseMethodCall(javaContext, classInfo, info, expr.asMethodReferenceExpr().resolve());
                } catch (Exception e) {
                    LOG.warn("resolve fail:\n  {}.{}({}.java:1) -> {}\n  {}",
                            classInfo.sign, info.name, classInfo.name, expr.toString(),
                            e.getLocalizedMessage());
                }
            }
        }
    }
    
    private static boolean exprMethodCall(JavaContext javaContext, TypeInfo classInfo, MemberInfo info, Expression expr) {
        try {
            MethodCallExpr methodCallExpr = expr.asMethodCallExpr();
            for (Expression argExpr : methodCallExpr.getArguments()) {
                if (argExpr.isMethodCallExpr()) {
                    exprMethodCall(javaContext, classInfo, info, argExpr);
                }
            }
            parseMethodCall(javaContext, classInfo, info, methodCallExpr.resolve());
            return true;
        } catch (Exception e) {
            LOG.warn("resolve fail:\n  {}.{}({}.java:1) -> {}\n  {}",
                    classInfo.sign, info.name, classInfo.name, expr.asMethodCallExpr().getNameAsString(),
                    e.getLocalizedMessage());
        }
        return false;
    }
    
    private static void parseMethodCall(JavaContext javaContext, TypeInfo classInfo, MemberInfo info, ResolvedMethodDeclaration r) {
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
