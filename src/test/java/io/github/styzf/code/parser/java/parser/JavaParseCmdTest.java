package io.github.styzf.code.parser.java.parser;

import io.github.styzf.code.parser.JavaParserCmd;
import org.testng.annotations.Test;

public class JavaParseCmdTest {

    private String testField;

    public String getTestField() {
        return testField;
    }

    public void setTestField(String testField) {
        this.testField = testField;
    }

    @Test
    public void testRun() {
        JavaParserCmd.main("");
    }
}
