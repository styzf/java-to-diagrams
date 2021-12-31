package io.github.styzf.code.parser.java.parser;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import io.github.styzf.code.parser.java.api.JavaParse;
import io.github.styzf.code.parser.java.api.bean.JavaInfo;
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
    static void parseMethodCall(List<JavaParse> javaParses, BodyDeclaration<?> m,
                                JavaInfo classInfo, JavaInfo info) {
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
            JavaInfo callInfo = new JavaInfo();
            callInfo.classInfo = new JavaInfo();
            ResolvedReferenceTypeDeclaration rt = r.declaringType();
            InfoUtils.addTypeInfo(callInfo.classInfo, rt, null);
            InfoUtils.addMethodInfo(callInfo, r, null);
            InfoUtils.forGetSet(classInfo, null, rt);
            javaParses.forEach(v -> v.call(info, callInfo));
        }
    }
}
