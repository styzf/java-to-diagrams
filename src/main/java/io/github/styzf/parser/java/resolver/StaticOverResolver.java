package io.github.styzf.parser.java.resolver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.context.java.javainfo.TypeInfo;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.parser.java.util.InfoUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 静态解析接口，抽象，实现
 *
 * @author styzf
 */
@Slf4j
public class StaticOverResolver {
    private final static Pattern GENERICS_PATTERN = Pattern.compile("<[^<]*?>");
    private final static String EXTENDS = "extends";
    private final static String IMPLEMENTS = "implements";
    private final static String EMPTY_STR = "";
    private final static String SPACE = " ";
    
    /**
     * 静态解析重写方法
     */
    public static void parseOver(JavaContext javaContext, TypeInfo classInfo,
                                 ResolvedReferenceTypeDeclaration rt, TypeDeclaration<?> type) {
        if (!type.isClassOrInterfaceDeclaration()) {
            return;
        }
        
        String fullName = "";
        if (rt.isClass()) {
            fullName = type.toString().substring(type.toString().indexOf("class "), type.toString().indexOf("{"));
        }
        if (rt.isInterface()) {
            fullName = type.toString().substring(type.toString().indexOf(" interface "), type.toString().indexOf("{"));
        }
        // 去除泛型
        while (GENERICS_PATTERN.matcher(fullName).find()) {
            fullName = GENERICS_PATTERN.matcher(fullName).replaceFirst("");
        }
        
        if (!fullName.contains(EXTENDS) && !fullName.contains(IMPLEMENTS)) {
            return;
        }
    
        fullName = fullName.replaceAll(EXTENDS, EMPTY_STR);
        fullName = fullName.replaceAll(IMPLEMENTS, EMPTY_STR);
    
        String[] parentArr = fullName.split(SPACE);
        if (parentArr.length <= 3) {
            return;
        }
    
        List<String> parentList = Arrays.stream(parentArr)
                .filter(StrUtil::isNotBlank).collect(Collectors.toList());
        parentList = parentList.subList(2, parentList.size());
    
        parentList.forEach(parentName -> {
            Set<String> importSet = classInfo.getImportSet(parentName);
            if (CollUtil.isEmpty(importSet)) {
                String packName = classInfo.getPackName();
                try {
                    String sign = packName + "." + parentName;
                    Class.forName(sign);
                    addRel(javaContext, classInfo, parentName, sign);
                } catch (ClassNotFoundException e) {
                    log.error("无法解析到对应的继承关系：{}  parentName:{}" ,classInfo.sign , parentName);
                }
            } else {
                importSet.forEach(sign -> {
                    addRel(javaContext, classInfo, parentName, sign);
                });
            }
        });
    }
    
    /**
     * 添加关联关系
     */
    private static void addRel(JavaContext javaContext, TypeInfo classInfo, String parentName, String sign) {
        TypeInfo relInfo = javaContext.getType(sign);
        boolean needAddContext = false;
        if (ObjectUtil.isNull(relInfo)) {
            relInfo = new TypeInfo();
            needAddContext = true;
        }
        if (needAddContext) {
            javaContext.add(relInfo);
        }
        
        relInfo.name = parentName;
        relInfo.sign = sign;
        
        classInfo.relInfo.put(relInfo.sign, relInfo);
        classInfo.relInfo.put(classInfo.sign, classInfo);
        relInfo.relInfo.put(relInfo.sign, relInfo);
        relInfo.relInfo.put(classInfo.sign, classInfo);
    }
}
