package io.github.styzf.parser.java.resolver;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.parser.java.util.InfoUtils;

/**
 * @author styzf
 * @date 2022/4/3 11:40
 */
public class TypeResolver {
    public static void parserType(TypeDeclaration<?> type, JavaContext javaContext ) {
        //            classInfo.packNames = packNames;
        ResolvedReferenceTypeDeclaration rt = type.resolve();
        TypeInfo classInfo = InfoUtils.getTypeInfo(javaContext, rt, type);
    
        MembersResolver.parseMembers(javaContext, classInfo, type, rt);
        if (!type.isClassOrInterfaceDeclaration()) {
            return;
        }
        if (rt.isClass()) {
            ResolvedClassDeclaration rcd = rt.asClass();
            OverResolver.parseOver(javaContext, classInfo, type, rcd.getAllInterfaces(), true);
            OverResolver.parseOver(javaContext, classInfo, type, rcd.getAllSuperClasses(), true);
        }
        if (rt.isInterface()) {
            ResolvedInterfaceDeclaration rid = rt.asInterface();
            OverResolver.parseOver(javaContext, classInfo, type, rid.getAllInterfacesExtended(), false);
        }
    }
}
