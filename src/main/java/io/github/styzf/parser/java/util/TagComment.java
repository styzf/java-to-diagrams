package io.github.styzf.parser.java.util;

import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;

import java.util.List;
import java.util.Optional;

class TagComment {

    /**
     * 获取文档注释中的标签值
     */
    static String from(Javadoc javadoc, JavadocBlockTag.Type param, String name) {
        List<JavadocBlockTag> blockTags = javadoc.getBlockTags();
        for (JavadocBlockTag tag : blockTags) {
            if (tag.getType() != param) {
                continue;
            }
            String comment = tag.getContent().toText();
            if (name == null) {
                return comment;
            }
            Optional<String> commentNameOptional = tag.getName();
            if (!commentNameOptional.isPresent()) {
                continue;
            }
            String commentName = commentNameOptional.get();
            if (commentName.equals(name)) {
                return comment;
            }
        }
        return "";
    }
}
