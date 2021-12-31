package io.github.styzf.code.parser.java.impl.graphviz;

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.model.Node;
import io.github.styzf.util.graphviz.GraphvizUtils;
import io.github.styzf.code.parser.java.api.bean.JavaInfo;

import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.node;

/**
 * 节点工具类
 */
class NodeUtils {

    private NodeUtils() {}

    /**
     * 方法块
     */
    static String[] methodRecords(JavaInfo info) {
        String returnComment = info.returnComment == null || "".equals(info.returnComment)
                ? ""
                : " : " + info.returnComment;
        String returnType = info.returnType == null ? "" : " : " + info.returnType;
        // 没有参数
        if (info.paramNames.isEmpty()) {
            return new String[]{
                    GraphvizUtils.label(info.commentFirst) + returnComment,
                    info.modSymbol() + info.name + returnType,
            };
        }
        // 没有参数注释
        if (!info.haveParamComments) {
            return new String[]{
                    GraphvizUtils.label(info.commentFirst) + returnComment,
                    info.modSymbol() + info.name + returnType,
                    info.paramNamesStr(),
                    info.paramTypes.stream()
                            .map(GraphvizUtils::escape)
                            .collect(Collectors.joining(", ", "(", ")")),
            };
        }
        // 有参数有注释
        return new String[]{
                GraphvizUtils.label(info.commentFirst) + returnComment,
                info.modSymbol() + info.name + returnType,
                info.paramCommentsStr(),
                info.paramNamesStr(),
                info.paramTypes.stream()
                        .map(GraphvizUtils::escape)
                        .collect(Collectors.joining(", ", "(", ")")),
        };
    }

    /** 提示节点 */
    static Node tipNode(Rank.RankDir rankDir) {
        String[] recs = {
                "methodComment : returnComment",
                "methodName : returnType",
                "paramComments",
                "paramNames",
                "paramTypes",
        };
        return GraphvizUtils.rankDirRecords(node("tip"), rankDir, recs);
    }
}
