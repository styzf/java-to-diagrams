package io.github.styzf.code.parser.java.impl.graphviz;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import io.github.styzf.code.parser.java.api.JavaParse;
import io.github.styzf.code.parser.java.api.bean.JavaInfo;
import io.github.styzf.code.parser.java.api.dict.MemberEnum;
import io.github.styzf.code.parser.java.impl.utils.CallUtils;
import io.github.styzf.util.graphviz.GraphvizUtils;
import io.github.styzf.util.graphviz.Uml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

/**
 * Graphviz 关系图 实现
 */
public class JavaParseImplGraphviz implements JavaParse {

    private final LinkedHashMap<String, LinkedHashMap<String, Node>> typeMethodMap = new LinkedHashMap<>();

    public Rank.RankDir rankDir = Rank.RankDir.LEFT_TO_RIGHT;

    public Graph g = GraphvizUtils.initWithFont(rankDir, "");

    public final File outDir;
    public final String outName;

    public JavaParseImplGraphviz(File outDir, String outName) {
        this.outDir = outDir;
        this.outName = outName;
    }

    /**
     * 类边框
     * <br/>因为是一个类一个类解析的所以不在 method 中另外判断是不是属于这个类提高性能
     */
    private Graph cluster;

    /**
     * 类处理
     */
    @Override
    public void type(JavaInfo classInfo) {
        if (cluster != null) {
            g = g.with(cluster);
        }
        String classComment = classInfo.commentFirst == null ? "" : classInfo.commentFirst + "\n";
        cluster = graph(classInfo.sign + "_C")
                .cluster()
                .graphAttr()
                .with(Color.BLUE, Label.of(classComment + classInfo.modSymbol() + classInfo.sign));
        typeMethodMap.put(classInfo.sign, new LinkedHashMap<>());
    }

    /**
     * 方法生成主题
     */
    @Override
    public void member(JavaInfo info) {
        if (info.memberType == MemberEnum.GET_SET) {
            return;
        }
        Node node = node(GraphvizUtils.escape(info.sign));
        // 字段点线
        if (info.memberType == MemberEnum.FIELD) {
            node = node.with(Style.DOTTED);
        }
        // 构造方法虚线
        if (info.memberType == MemberEnum.CONSTRUCTOR) {
            node = node.with(Style.DASHED);
        }
        node = GraphvizUtils.rankDirRecords(node, this.rankDir, NodeUtils.methodRecords(info));
        cluster = cluster.with(node);
        typeMethodMap.get(info.classInfo.sign).put(info.sign, node);
    }


    private final List<JavaInfo[]> usageCallList = new ArrayList<>();

    /**
     * 保存调用关系
     */
    @Override
    public void call(JavaInfo usageInfo, JavaInfo callInfo) {
        usageCallList.add(new JavaInfo[]{usageInfo, callInfo});
    }

    private final List<JavaInfo[]> overList = new ArrayList<>();

    /**
     * 保存实现关系
     */
    @Override
    public void over(JavaInfo overInfo, JavaInfo parentInfo) {
        overList.add(new JavaInfo[]{overInfo, parentInfo});
    }

    /**
     * 处理并生成调用关系图
     */
    @Override
    public void end() {
        // 包含最后一个类
        if (cluster != null) {
            g = g.with(cluster);
        }
        forCall();
        CallUtils.forOver(overList, typeMethodMap,
                (parent, over) -> g = g.with(Uml.from(over).implementsFor(parent)));
        g = g.with(graph("tip").cluster()
                .graphAttr().with(Color.BLUE, Label.of("tip:class"))
                .with(NodeUtils.tipNode(rankDir)));
        GraphvizUtils.toFile(g, outDir, outName);
    }

    /**
     * 方法调用
     */
    private void forCall() {
        for (JavaInfo[] infos : usageCallList) {
            JavaInfo usageInfo = infos[0];
            JavaInfo callInfo = infos[1];
            LinkedHashMap<String, Node> callMethodMap = typeMethodMap.get(callInfo.classInfo.sign);
            // 如果调用的方法不在扫描的类中则跳过
            if (callMethodMap == null) {
                continue;
            }
            Node call = callMethodMap.get(callInfo.sign);
            // 被筛选掉的方法跳过
            if (call == null) {
                continue;
            }
            Node usage = typeMethodMap.get(usageInfo.classInfo.sign).get(usageInfo.sign);
            // 调用关系不能写在 cluster 里，否则会把被调的节点也放进去
            g = g.with(Uml.from(usage).dependency(call));
        }
    }
}
