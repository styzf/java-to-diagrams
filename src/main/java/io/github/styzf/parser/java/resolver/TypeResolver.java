package io.github.styzf.parser.java.resolver;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.parser.java.JavaParserImpl;
import io.github.styzf.parser.java.util.InfoUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author styzf
 * @date 2022/4/3 11:40
 */
public class TypeResolver {
    public static void parserType(CompilationUnit cu, JavaContext javaContext) {
        String packName = "";
        Optional<PackageDeclaration> packageDeclaration = cu.getPackageDeclaration();
        if (packageDeclaration.isPresent()) {
            packName = packageDeclaration.get().getNameAsString();
        }
        Set<String> importNameSet = cu.getImports().stream()
                .map(ImportDeclaration::getNameAsString).collect(Collectors.toSet());
    
        // todo 实现解析包注释
//         StaticJavaParser.parseJavadoc(cu.getComment().get().getContent()).getDescription().toText();
        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type.isAnnotationDeclaration()) {
                continue;
            }
            ResolvedReferenceTypeDeclaration rt = type.resolve();
            TypeInfo classInfo = InfoUtils.getTypeInfo(javaContext, rt, type);
            classInfo.putImportSet(importNameSet);
            // todo 这个包可能会没有
            classInfo.setPackName(packName);
    
            StaticOverResolver.parseOver(javaContext, classInfo, rt,type);
            MembersResolver.parseMembers(javaContext, classInfo, type, rt);
        }
    }
}
