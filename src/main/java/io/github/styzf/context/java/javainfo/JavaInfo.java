package io.github.styzf.context.java.javainfo;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java解析容器
 * @author styzf
 * @date 2021/12/15 20:15
 */
public class JavaInfo extends ModifiersInfo {
    private static final Logger LOG = LoggerFactory.getLogger(JavaInfo.class);
    private static final Pattern COMMENT_FIRST_PATTERN = Pattern.compile(
            "^[^\\r\\n]*(?:[.;]\\s|[\\r\\n]|$|.)");
    
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
    
    /** 首句注释 */
    private String commentFirst = "";
    /** 全部注释 */
    public String comment;

    /** 是否选择的文件 */
    public boolean isSelect;

    /** 类 */
    public TypeInfo classInfo;

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
                LOG.warn("commentGenFirst fail:\n{}.{}({}.java:1) \n{}", classInfo.sign, name, classInfo.name, comment);
            } else {
                // 类
                LOG.warn("commentGenFirst fail:\n{}({}.java:1) \n{}", sign, name, comment);
            }
            commentFirst = comment;
        }
        return commentFirst;
    }

}
