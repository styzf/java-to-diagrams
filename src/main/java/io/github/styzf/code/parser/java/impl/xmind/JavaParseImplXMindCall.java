package io.github.styzf.code.parser.java.impl.xmind;

import io.github.styzf.util.common.Conf;
import io.github.styzf.code.parser.java.api.bean.JavaInfo;
import io.github.styzf.code.parser.java.api.dict.MemberEnum;
import io.github.styzf.code.parser.java.impl.utils.CallUtils;
import io.github.styzf.util.common.FilterUtils;
import org.xmind.core.ITopic;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * XMind 思维导图/脑图 调用树 实现
 */
public class JavaParseImplXMindCall extends AbsJavaParseImplXMind {

    public JavaParseImplXMindCall(File outDir, String outName) {
        super(outDir, outName + "_call");
    }

    @Override
    protected String tipName() {
        return "调用树";
    }

    /**
     * 类与方法节点 Map
     */
    private final LinkedHashMap<String, LinkedHashMap<String, ITopic>> typeMethodMap = new LinkedHashMap<>();
    /**
     * 方法节点 Map
     * <br/>因为是一个类一个类解析的所以不在 {@link #member} 中另外判断是不是属于这个类提高性能
     */
    private LinkedHashMap<String, ITopic> methodMap;

    /**
     * 类处理
     */
    @Override
    public void type(JavaInfo classInfo) {
        methodMap = new LinkedHashMap<>();
        typeMethodMap.put(classInfo.sign, methodMap);
    }

    private final boolean showClass = "true".equals(Conf.PARSER_XMIND_CLASS.get());

    /**
     * 方法生成主题
     */
    @Override
    public void member(JavaInfo info) {
        ITopic iTopic = topicFrom(info);
        if (iTopic == null) {
            return;
        }
        methodMap.put(info.sign, iTopic);
    }

    public final Pattern includeMethod = Pattern.compile(Conf.PARSER_XMIND_METHOD_INCLUDE.get());
    public final Pattern excludeMethod = Pattern.compile(Conf.PARSER_XMIND_METHOD_EXCLUDE.get());

    // todo 对比去重 {@link JavaParseImplXMind#member} 修复文档注释
    /**
     *
     */
    private ITopic topicFrom(JavaInfo info) {
        // 暂时跳过这些无关紧要的
        if (info.memberType == MemberEnum.STATIC) {
            return null;
        } else if (info.memberType == MemberEnum.FIELD) {
            return null;
        } else if (info.memberType == MemberEnum.GET_SET) {
            return null;
        }
        if (!FilterUtils.filter(info.sign, includeMethod, excludeMethod)) {
            return null;
        }
        ITopic iTopic = workbook.createTopic();
        StringBuilder text = new StringBuilder();
        if (info.commentFirst != null) {
            text.append(info.commentFirst);
        }
        text.append("\n");
        if (showSymbol) {
            text.append(info.classInfo.type.symbol).append(info.modSymbol());
        }
        if (showClass) {
            text.append(info.className()).append(".");
        }
        text.append(info.name);
        if (info.memberType == MemberEnum.CONSTRUCTOR || info.memberType == MemberEnum.METHOD) {
            text.append("()");
        }
        iTopic.setTitleText(text.toString());
        return iTopic;
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

    @Override
    protected void beforeSave() {
        forCall();
        CallUtils.forOver(overList, typeMethodMap, ITopic::add);
        toRoot();
        mergeSingleImpl();
    }

    public final boolean showOtherCall = "true".equals(Pattern.compile(Conf.PARSER_XMIND_SHOW_OTHER_CALL.get()));

    /**
     * 方法调用
     */
    private void forCall() {
        for (JavaInfo[] infos : usageCallList) {
            JavaInfo usageInfo = infos[0];
            JavaInfo callInfo = infos[1];
            LinkedHashMap<String, ITopic> callMethodMap = typeMethodMap.get(callInfo.classInfo.sign);
            // 如果调用的方法不在扫描的类中则跳过
            if (callMethodMap == null) {
                if (showOtherCall) {
                    continue;
                }
                callMethodMap = new LinkedHashMap<>();
                ITopic iTopic = topicFrom(callInfo);
                if (iTopic == null) {
                    continue;
                }
                callMethodMap.put(callInfo.sign, iTopic);
            }
            ITopic usage = typeMethodMap.get(usageInfo.classInfo.sign).get(usageInfo.sign);
            // 调用方被省略（字段、静态）
            if (usage == null) {
                continue;
            }
            ITopic call = callMethodMap.get(callInfo.sign);
            // 被筛选掉的方法跳过
            if (call == null) {
                continue;
            }
            // 递归调用
            if (usage.equals(call)) {
                usage.setTitleText(usage.getTitleText() + "↺");
                continue;
            }
            ITopic parent = call.getParent();
            // 正常情况
            if (parent == null) {
                usage.add(call);
                continue;
            }
            // 多次调用已记录跳过
            if (parent.equals(usage)) {
                continue;
            }
            // 多个父调用，后面考虑重构数据结构和算法，在 set 方法遍历副本，实现树的深复制
            ITopic callCopy = workbook.cloneTopic(call);
            // 设置红色字体
            IStyle style = styleSheet.findStyle(callCopy.getStyleId());
            if (style == null) {
                style = styleSheet.createStyle(IStyle.TOPIC);
                styleSheet.addStyle(style, IStyleSheet.NORMAL_STYLES);
            }
            callCopy.setStyleId(style.getId());
            style.setProperty("fo:color", "#FF3C00FF");
            // 设置链接
            callCopy.setHyperlink(XMindConstant.XMIND + ":#" + call.getId());

            usage.add(callCopy);
        }
    }


    /**
     * 合并单个接口实现
     */
    private void mergeSingleImpl() {
        for (JavaInfo[] infos : overList) {
            JavaInfo faceInfo = infos[1];
            LinkedHashMap<String, ITopic> faceMethodMap = typeMethodMap.get(faceInfo.sign);
            if (faceMethodMap == null) {
                continue;
            }
            for (Map.Entry<String, ITopic> fe : faceMethodMap.entrySet()) {
                ITopic faceTopic = fe.getValue();
                List<ITopic> allChildren = faceTopic.getAllChildren();
                if (allChildren.size() != 1) {
                    continue;
                }
                ITopic implTopic = allChildren.get(0);
                faceTopic.remove(implTopic);
                for (ITopic child : implTopic.getAllChildren()) {
                    faceTopic.add(child);
                }
            }
        }
    }

    public final Pattern includeRoot = Pattern.compile(Conf.PARSER_XMIND_INCLUDE.get());
    public final Pattern excludeRoot = Pattern.compile(Conf.PARSER_XMIND_EXCLUDE.get());

    /**
     * 把没有父节点的主题连接到根节点，没有父子的连到 alone 节点并折叠
     */
    private void toRoot() {
        primarySheet.setTitleText("call");
        ITopic rootTopic = primarySheet.getRootTopic();
        rootTopic.setStructureClass(XMindConstant.LOGIC_RIGHT);

        ITopic alone = workbook.createTopic();
        alone.setTitleText("独立节点\nalone");
        alone.setFolded(true);

        for (Map.Entry<String, LinkedHashMap<String, ITopic>> methodEntry : typeMethodMap.entrySet()) {
            methodEntry.getValue().entrySet().stream()
                    .filter(e -> FilterUtils.filter(e.getKey(), includeRoot, excludeRoot))
                    .filter(e -> e.getValue().getParent() == null)
                    .forEach(e -> (e.getValue().getAllChildren().isEmpty() ? alone : rootTopic).add(e.getValue()));
        }

        // 独立节点非空时接入
        if (!alone.getAllChildren().isEmpty()) {
            rootTopic.add(alone);
        }
    }
}
