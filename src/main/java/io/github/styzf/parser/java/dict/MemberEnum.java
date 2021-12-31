package io.github.styzf.parser.java.dict;

public enum MemberEnum {
    FIELD("F", "field", "字段"),
    GET_SET("G", "get_set", "属性方法"),
    STATIC("S", "static", "静态块"),
    CONSTRUCTOR("C", "constructor", "构造方法"),
    METHOD("M", "method", "普通方法"),
    ;
    
    public final String symbol;
    public final String string;
    public final String desc;
    
    MemberEnum(String symbol, String string, String desc) {
        this.symbol = symbol;
        this.string = string;
        this.desc = desc;
    }
    
    public static boolean isMethod(MemberEnum memberEnum) {
        return METHOD.equals(memberEnum);
    }
    public static boolean isNotMethod(MemberEnum memberEnum) {
        return !isMethod(memberEnum);
    }
    public static boolean isGetSet(MemberEnum memberEnum) {
        return GET_SET.equals(memberEnum);
    }
}
