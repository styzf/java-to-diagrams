package io.github.styzf.code.parser.java.parser;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import io.github.styzf.code.parser.java.api.JavaParse;
import io.github.styzf.code.parser.java.api.bean.JavaInfo;

import java.util.List;

public class OverUtils {

    /**
     * 解析重写方法
     */
    static void parseOver(List<JavaParse> javaParses, JavaInfo classInfo,
                          TypeDeclaration<?> type,
                          List<ResolvedReferenceType> parents) {
        for (ResolvedReferenceType parent : parents) {
            for (MethodUsage dm : parent.getDeclaredMethods()) {
                // 私有方法不会被重写
                if (AccessSpecifier.PRIVATE == dm.getDeclaration().accessSpecifier()) {
                    continue;
                }
                // 查找当前类是否有同签名的重写方法
                String[] paramTypes = dm.getParamTypes().stream()
                        .map(ResolvedType::describe)
                        .map(s -> s.substring(s.lastIndexOf(".") + 1))
                        .toArray(String[]::new);
                List<MethodDeclaration> methods = type.getMethodsBySignature(dm.getName(), paramTypes);
                if (methods.isEmpty()) {
                    continue;
                }
                // 重写的方法
                JavaInfo overInfo = new JavaInfo();
                overInfo.classInfo = classInfo;
                overInfo.sign = methods.get(0).resolve().getQualifiedSignature();
                // 父类或实现类方法
                JavaInfo parentInfo = new JavaInfo();
                parentInfo.classInfo = new JavaInfo();
                parentInfo.classInfo.sign = parent.getQualifiedName();
                parentInfo.sign = dm.getQualifiedSignature();
                javaParses.forEach(v -> v.over(overInfo, parentInfo));
            }
        }
    }
}
