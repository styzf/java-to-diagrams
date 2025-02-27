package io.github.styzf.parser.java.dict;

public enum TypeEnum {
    CLAZZ("C", "class"),
    INTERFACE("I", "interface"),
    ENUM("E", "enum"),
    ANNOTATION("@", "annotation"),
    UNKNOWN("?", "unknown"),
    ;

    public final String symbol;
    public final String string;

    TypeEnum(String symbol, String string) {
        this.symbol = symbol;
        this.string = string;
    }
}
