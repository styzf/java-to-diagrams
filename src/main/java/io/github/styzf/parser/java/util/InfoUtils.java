package io.github.styzf.parser.java.util;

import cn.hutool.core.util.ObjectUtil;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.resolution.declarations.HasAccessSpecifier;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserAnnotationDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import io.github.styzf.code.parser.java.api.dict.AccessEnum;
import io.github.styzf.code.parser.java.api.dict.TypeEnum;
import io.github.styzf.context.JavaContext;
import io.github.styzf.context.javainfo.JavaInfo;
import io.github.styzf.context.javainfo.MemberInfo;
import io.github.styzf.context.javainfo.TypeInfo;
import io.github.styzf.parser.java.dict.MemberEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * JavaParseInfo 信息补充工具类
 */
public class InfoUtils {

    private static final Logger LOG = LoggerFactory.getLogger(InfoUtils.class);

    private InfoUtils() {}
    
    public static TypeInfo getTypeInfo(JavaContext javaContext, ResolvedReferenceTypeDeclaration rt) {
        String sign = rt.getQualifiedName();
        String name = rt.getName();
    
        TypeInfo classInfo = javaContext.getType(sign);
        if (ObjectUtil.isNull(classInfo)) {
            classInfo = new TypeInfo();
        } else {
            return classInfo;
        }
        
        if (rt.isEnum()) {
            classInfo.type = TypeEnum.ENUM;
        } else if (rt.isClass()) {
            classInfo.type = TypeEnum.CLAZZ;
        } else if (rt.isInterface()) {
            classInfo.type = TypeEnum.INTERFACE;
        } else if (rt instanceof JavaParserAnnotationDeclaration) {
            classInfo.type = TypeEnum.ANNOTATION;
        } else {
            classInfo.type = TypeEnum.UNKNOWN;
        }
        classInfo.sign = sign;
        classInfo.name = name;
        classInfo.genLowFirstName();
        if (rt instanceof HasAccessSpecifier) {
            classInfo.access = AccessEnumUtils.toEnum(((HasAccessSpecifier) rt).accessSpecifier());
        }
        
        javaContext.add(classInfo);
        return classInfo;
    }
    
    /**
     * 补充类信息
     * todo 被调用方包解析
     * @param type nullable
     */
    public static TypeInfo getTypeInfo(JavaContext javaContext, ResolvedReferenceTypeDeclaration rt, TypeDeclaration<?> type) {
        TypeInfo classInfo = getTypeInfo(javaContext, rt);
        addTypeInfo(classInfo, type);
        return classInfo;
    }
    
    /**
     * 补充类信息
     *
     * @param type nullable
     */
    private static void addTypeInfo(JavaInfo classInfo, TypeDeclaration<?> type) {
        if (type.isAnnotationDeclaration()) {
            classInfo.type = TypeEnum.ANNOTATION;
        }
        classInfo.isStatic = type.isStatic();
        type.ifClassOrInterfaceDeclaration(t -> classInfo.isAbstract = t.isAbstract());
        // TODO 测试是否相同
        AccessEnum accessEnum = AccessEnumUtils.toEnum(type.getAccessSpecifier());
        if (classInfo.access != accessEnum) {
            LOG.warn("access {} != {} in {}", classInfo.access, accessEnum, classInfo.sign);
            classInfo.access = accessEnum;
        }

        type.getJavadoc().ifPresent(v -> classInfo.comment = v.getDescription().toText());
        classInfo.genCommentFirst();
    }

    /**
     * 补充字段信息
     */
    public static void addFieldInfo(JavaInfo info, ResolvedFieldDeclaration r, FieldDeclaration d) {
        info.name = r.getName();
        info.genLowFirstName();
        info.isStatic = d.isStatic();
        info.isFinal = d.isFinal();
        info.access = AccessEnumUtils.toEnum(r.accessSpecifier());
        d.getJavadoc().ifPresent(v -> info.comment = v.getDescription().toText());
        info.genCommentFirst();
    }

    /**
     * 补充方法信息
     *
     * @param d nullable
     */
    public static void addMethodInfo(MemberInfo info, ResolvedMethodLikeDeclaration r, CallableDeclaration<?> d) {
        // https://github.com/nidi3/graphviz-java/issues/172
        info.sign = r.getQualifiedSignature().replace(" extends java.lang.Object", "");
        info.name = r.getName();
        info.genLowFirstName();
        if (r instanceof JavaParserMethodDeclaration) {
            JavaParserMethodDeclaration md = (JavaParserMethodDeclaration) r;
            info.isStatic = md.isStatic();
            info.isAbstract = md.isAbstract();
        }
        // 修正接口方法默认共有
        if (TypeEnum.INTERFACE.equals(info.classInfo.type)) {
            info.access = AccessEnum.PUBLIC;
        } else {
            info.access = AccessEnumUtils.toEnum(r.accessSpecifier());
        }
        if (d == null) {
            return;
        }
        info.isFinal = d.isFinal();
        // TODO 测试是否相同
        AccessEnum accessEnum = AccessEnumUtils.toEnum(d.getAccessSpecifier());
        if (info.access != accessEnum) {
            // 修正接口方法默认共有
            if (TypeEnum.INTERFACE.equals(info.classInfo.type)) {
                info.access = AccessEnum.PUBLIC;
            } else {
                LOG.warn("access {} != {} in {}", info.access, accessEnum, info.sign);
                info.access = accessEnum;
            }
        }
        if (info.isStatic != d.isStatic()) {
            LOG.warn("isStatic {} != {} in {}", info.isStatic, d.isStatic(), info.sign);
            info.isStatic = d.isStatic();
        }
        if (info.isAbstract != d.isAbstract()) {
            // 修正接口方法默认抽象
            if (TypeEnum.INTERFACE.equals(info.classInfo.type)) {
                info.isAbstract = true;
            } else {
                LOG.warn("isAbstract {} != {} in {}", info.isAbstract, d.isAbstract(), info.sign);
                info.isAbstract = d.isAbstract();
            }
        }

        Optional<Javadoc> javadoc = d.getJavadoc();
        javadoc.ifPresent(v -> info.comment = v.getDescription().toText());
        info.genCommentFirst();

        for (Parameter param : d.getParameters()) {
            info.paramTypes.add(param.getType().toString());
            String name = param.getName().toString();
            info.paramNames.add(name);
            if (javadoc.isPresent()) {
                String paramComment = TagComment.from(javadoc.get(), JavadocBlockTag.Type.PARAM, name);
                if (!"".equals(paramComment)) {
                    info.haveParamComments = true;
                }
                info.paramComments.add(paramComment);
            } else {
                info.paramComments.add("");
            }
        }
        if (d.isMethodDeclaration()) {
            MethodDeclaration rmd = d.asMethodDeclaration();
            info.returnType = rmd.getType().asString();
            javadoc.ifPresent(v -> info.returnComment = TagComment.from(v, JavadocBlockTag.Type.RETURN, null));
        }
    }

    /**
     * GetSet方法判断
     */
    public static void forGetSet(MemberInfo info, TypeDeclaration<?> type, ResolvedReferenceTypeDeclaration rt) {
        String name = null;
        if (info.name.length() > 3) {
            if (info.name.startsWith("get") || info.name.startsWith("set")) {
                name = info.name.substring(3);
            }
        } else if (info.name.length() > 2 && info.name.startsWith("is")) {
            name = info.name.substring(2);
        }
        if (name != null) {
            String lowFirstName = name.substring(0, 1).toLowerCase() + name.substring(1);
            if (type != null) {
                type.getFieldByName(lowFirstName).ifPresent(e -> info.memberType = MemberEnum.GET_SET);
            } else if (rt != null) {
                ResolvedFieldDeclaration field = rt.getField(lowFirstName);
                if (field != null) {
                    info.memberType = MemberEnum.GET_SET;
                }
            }
        }
    }
    
}
