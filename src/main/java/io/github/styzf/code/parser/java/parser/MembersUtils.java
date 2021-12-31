package io.github.styzf.code.parser.java.parser;

import com.github.javaparser.ast.body.*;
import com.github.javaparser.resolution.declarations.*;
import io.github.styzf.code.parser.java.api.JavaParse;
import io.github.styzf.code.parser.java.api.bean.JavaInfo;
import io.github.styzf.code.parser.java.api.dict.AccessEnum;
import io.github.styzf.code.parser.java.api.dict.MemberEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MembersUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MembersUtils.class);

    /**
     * 解析成员
     */
    public static void parseMembers(List<JavaParse> javaParses, JavaInfo classInfo, TypeDeclaration<?> type,
                                    ResolvedReferenceTypeDeclaration rt) {
        for (BodyDeclaration<?> m : type.getMembers()) {
            JavaInfo info = new JavaInfo();
            info.classInfo = classInfo;
            if (m.isMethodDeclaration()) {
                info.memberType = MemberEnum.METHOD;
                MethodDeclaration d = m.asMethodDeclaration();
                ResolvedMethodDeclaration r = d.resolve();
                InfoUtils.addMethodInfo(info, r, d);
                InfoUtils.forGetSet(info, type, rt);
                javaParses.forEach(v -> v.member(info));
            } else if (m.isConstructorDeclaration()) {
                info.memberType = MemberEnum.CONSTRUCTOR;
                ConstructorDeclaration d = m.asConstructorDeclaration();
                ResolvedConstructorDeclaration r = d.resolve();
                InfoUtils.addMethodInfo(info, r, d);
                javaParses.forEach(v -> v.member(info));
            } else if (m.isFieldDeclaration()) {
                info.memberType = MemberEnum.FIELD;
                FieldDeclaration d = m.asFieldDeclaration();
                ResolvedFieldDeclaration r = d.resolve();
                info.sign = classInfo.sign + r.getName();
                InfoUtils.addFieldInfo(info, r, d);
                javaParses.forEach(v -> v.member(info));
            } else if (m.isInitializerDeclaration()) {
                info.memberType = MemberEnum.STATIC;
                InitializerDeclaration d = m.asInitializerDeclaration();
                info.sign = classInfo.sign + "_static";
                info.name = classInfo.name + "_static";
                info.isStatic = d.isStatic();
                info.access = AccessEnum.NONE;
                javaParses.forEach(v -> v.member(info));
            } else {
                LOG.warn("skip: {}", m);
                continue;
            }
            MethodCallUtils.parseMethodCall(javaParses, m, classInfo, info);
        }
    }
}
