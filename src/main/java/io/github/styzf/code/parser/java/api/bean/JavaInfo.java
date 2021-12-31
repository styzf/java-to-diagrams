package io.github.styzf.code.parser.java.api.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JavaInfo extends ModifiersInfo {
    /** 包名 */
    public List<String> packNames = new ArrayList<>();
    /** 包注释 */
    public List<String> packComments = new ArrayList<>();


    /** 全称 */
    public String sign;
    /** 简称 */
    public String name;
    /** 首字母小写简称 */
    public String lowFirstName;

    /** 非静态调用时返回小写开头的类名 */
    public String className() {
        return isStatic ? classInfo.name : classInfo.lowFirstName;
    }


    /** 首句注释 */
    public String commentFirst;
    /** 全部注释 */
    public String comment;


    /** 参数类名 */
    public List<String> paramTypes = new ArrayList<>();
    /** 参数名 */
    public List<String> paramNames = new ArrayList<>();
    /** 参数注释 */
    public List<String> paramComments = new ArrayList<>();
    /** 有参数注释 */
    public boolean haveParamComments;

    /** 是否选择的文件 */
    public boolean isSelect;

    /** 类 */
    public JavaInfo classInfo;
    /** 成员 */
    public LinkedHashMap<String, JavaInfo> memberInfo = new LinkedHashMap<>();
    /** 被调 */
    public LinkedHashMap<String, JavaInfo> usageInfo = new LinkedHashMap<>();
    /** 调用 */
    public LinkedHashMap<String, JavaInfo> callInfo = new LinkedHashMap<>();
    /** 接口 */
    public LinkedHashMap<String, JavaInfo> faceInfo = new LinkedHashMap<>();
    /** 实现 */
    public LinkedHashMap<String, JavaInfo> implInfo = new LinkedHashMap<>();
    /** 继承父 */
    public LinkedHashMap<String, JavaInfo> parentInfo = new LinkedHashMap<>();
    /** 继承子 */
    public LinkedHashMap<String, JavaInfo> childInfo = new LinkedHashMap<>();


    public String paramTypesStr() {
        return paramTypes.stream().collect(Collectors.joining(", ", "(", ")"));
    }

    public String paramNamesStr() {
        return paramNames.stream().collect(Collectors.joining(", ", "(", ")"));
    }

    public String paramCommentsStr() {
        return paramComments.stream().collect(Collectors.joining(", ", "(", ")"));
    }

    /** 返回类名 */
    public String returnType;
    /** 返回注释 */
    public String returnComment;


    /**
     * 生成首字母小写简称
     */
    public void genLowFirstName() {
        lowFirstName = name.substring(0, 1).toLowerCase() + name.substring(1);
    }


    private static final Logger LOG = LoggerFactory.getLogger(JavaInfo.class);
    private static final Pattern COMMENT_FIRST_PATTERN = Pattern.compile(
            "^[^\\r\\n]*(?:[.;]\\s|[\\r\\n]|$|.)");

    /**
     * 截取注释第一句
     */
    public void genCommentFirst() {
        if (comment == null) {
            commentFirst = "";
            return;
        }
        Matcher m = COMMENT_FIRST_PATTERN.matcher(comment);
        if (m.find()) {
            commentFirst = m.group();
        } else {
            if (classInfo != null) {
                // 方法
                LOG.warn("commentGenFirst fail:\n{}.{}({}.java:1) \n{}", classInfo.sign, name, classInfo.name, comment);
            } else {
                // 类
                LOG.warn("commentGenFirst fail:\n{}({}.java:1) \n{}", sign, name, comment);
            }
            commentFirst = comment;
        }
    }

}
