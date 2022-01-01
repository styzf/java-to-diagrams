package io.github.styzf.context.javainfo;

import cn.hutool.core.util.StrUtil;
import io.github.styzf.parser.java.dict.MemberEnum;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成员信息
 * @author styzf
 * @date 2021/12/16 19:38
 */
public class MemberInfo extends JavaInfo {
    
    /** 成员类型 */
    public MemberEnum memberType;
    
    /** 参数类名 */
    public List<String> paramTypes = new ArrayList<>();
    /** 参数名 */
    public List<String> paramNames = new ArrayList<>();
    /** 参数注释 */
    public List<String> paramComments = new ArrayList<>();
    /** 有参数注释 */
    public boolean haveParamComments;
    
    /** 被调 */
    public Map<String, MemberInfo> usageInfo = new LinkedHashMap<>();
    /** 调用 */
    public Map<String, MemberInfo> callInfo = new LinkedHashMap<>();
    
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
    /** 获取方法全名 */
    private String methodName;
    public String getMethodName() {
        if (StrUtil.isNotBlank(methodName)) {
            return methodName;
        }
        if (StrUtil.isBlank(sign) || MemberEnum.isNotMethod(memberType)){
            return "";
        }
        String param = sign.substring(sign.indexOf("("));
        String[] methodArr = sign.substring(0, sign.indexOf("(")).split("\\.");
        String method = methodArr[methodArr.length - 1];
        return method + param;
    }
}
