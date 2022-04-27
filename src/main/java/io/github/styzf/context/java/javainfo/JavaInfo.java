package io.github.styzf.context.java.javainfo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java解析容器
 * @author styzf
 * @date 2021/12/15 20:15
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class JavaInfo extends ModifiersInfo {
    private static final Pattern COMMENT_FIRST_PATTERN = Pattern.compile(
            "^[^\\r\\n]*(?:[.;]\\s|[\\r\\n]|$|.)");
    
    /**
     * 包名
     */
    private String packName;
    /**
     * 引入的包
     */
    private final Map<String, Set<String>> importMap = new HashMap<>();
    /** 包注释 */
    public List<String> packComments = new ArrayList<>();

    /** 全称 */
    public String sign;
    /** 简称 */
    public String name;
    /** 首字母小写简称 */
    public String lowFirstName;
    
    /** 首句注释 */
    private String commentFirst = "";
    /** 全部注释 */
    public String comment;

    /** 是否选择的文件 */
    public boolean isSelect;

    /** 类 */
    public TypeInfo classInfo;
    
    /**
     * 获取导入包
     */
    public Set<String> getImportSet(String key) {
        return importMap.get(key);
    }
    
    /**
     * 批量添加导入包
     */
    public void putImportSet(Set<String> set) {
        set.forEach(this::putImportMap);
    }
    
    /**
     * 添加导入包
     */
    public void putImportMap(String importStr) {
        String importName = importStr.substring(importStr.lastIndexOf(".") + 1);
        Set<String> set = importMap.get(importName);
        boolean notSameName = false;
        if (CollUtil.isEmpty(set)) {
            set = new HashSet<>();
            notSameName = true;
        }
        set.add(importStr);
        if (notSameName) {
            importMap.put(importName, set);
        }
    }
    
    /**
     * 生成首字母小写简称
     */
    public void genLowFirstName() {
        this.lowFirstName = name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    /**
     * 截取注释第一句
     * TODO 如果当前类没有注释，尝试使用实现或者接口的注释
     */
    public String getCommentFirst() {
        if (StrUtil.isNotBlank(commentFirst) || StrUtil.isBlank(comment)) {
            return commentFirst;
        }
        
        Matcher matcher = COMMENT_FIRST_PATTERN.matcher(comment);
        if (matcher.find()) {
            commentFirst = matcher.group();
        } else {
            if (classInfo != null) {
                // 方法
                log.warn("commentGenFirst fail:\n{}.{}({}.java:1) \n{}", classInfo.sign, name, classInfo.name, comment);
            } else {
                // 类
                log.warn("commentGenFirst fail:\n{}({}.java:1) \n{}", sign, name, comment);
            }
            commentFirst = comment;
        }
        return commentFirst;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JavaInfo javaInfo = (JavaInfo) o;
        return Objects.equals(sign, javaInfo.sign);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sign);
    }
}
