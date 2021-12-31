package io.github.styzf.code.parser.java.impl.xmind;

import io.github.styzf.code.parser.java.api.bean.JavaInfo;
import io.github.styzf.code.parser.java.api.dict.MemberEnum;
import org.xmind.core.ITopic;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * XMind 思维导图/脑图 成员树 实现
 */
public class JavaParseImplXMind extends AbsJavaParseImplXMind {

    public JavaParseImplXMind(File outDir, String outName) {
        super(outDir, outName);
    }

    @Override
    protected String tipName() {
        return "成员树";
    }

    private final LinkedHashMap<String, ITopic> packageMap = new LinkedHashMap<>();
    /**
     * 类与成员节点 Map
     * <br/>用于构造结构树
     */
    private final LinkedHashMap<String, LinkedHashMap<MemberEnum, LinkedHashMap<String, ITopic>>>
            typeMemberMap = new LinkedHashMap<>();
    /**
     * 成员节点 Map
     * <br/>因为是一个类一个类解析的所以不在 {@link #member 中另外判断是不是属于这个类提高性能
     */
    private LinkedHashMap<MemberEnum, LinkedHashMap<String, ITopic>> memberMap;
    /**
     * 当前节点 Map
     * <br/>因为是一个类一个类解析的所以不在 {@link #member 中另外判断是不是属于这个类提高性能
     */
    private ITopic typeTopic;

    /**
     * 类处理
     */
    @Override
    public void type(JavaInfo classInfo) {
        ITopic lastTopic = null;
        List<String> packs = classInfo.packNames;
        for (String packName : packs) {
            ITopic iTopic = packageMap.get(packName);
            if (iTopic != null) {
                if (lastTopic != null) {
                    iTopic.add(lastTopic);
                }
                break;
            }
            iTopic = workbook.createTopic();
            int i = packName.lastIndexOf(".");
            if (i == -1) {
                rootTopic.add(iTopic);
            }
            iTopic.setTitleText(packName.substring(i + 1));
            packageMap.put(packName, iTopic);
            if (lastTopic != null) {
                iTopic.add(lastTopic);
            }
            lastTopic = iTopic;
        }
        typeTopic = workbook.createTopic();
        StringBuilder text = new StringBuilder();
        if (classInfo.commentFirst != null) {
            text.append(classInfo.commentFirst);
        }
        text.append("\n");
        if (showSymbol) {
            text.append(classInfo.modSymbol());
        }
        text.append(classInfo.name);
        typeTopic.setTitleText(text.toString());
        typeTopic.setFolded(true);
        packageMap.get(packs.get(0)).add(typeTopic);
        packageMap.put(classInfo.sign, typeTopic);

        memberMap = new LinkedHashMap<>();
        typeMemberMap.put(classInfo.sign, memberMap);
    }

    /**
     * 方法生成主题
     */
    @Override
    public void member(JavaInfo info) {
        // 暂时跳过这些无关紧要的
        if (info.memberType == MemberEnum.GET_SET) {
            return;
        }
        ITopic memberTopic = workbook.createTopic();
        StringBuilder memberText = new StringBuilder();
        if (info.commentFirst != null) {
            memberText.append(info.commentFirst);
        }
        memberText.append("\n");
        if (showSymbol) {
            memberText.append(info.modSymbol());
        }
        memberText.append(info.name);
        if (info.memberType == MemberEnum.CONSTRUCTOR || info.memberType == MemberEnum.METHOD) {
            memberText.append("()");
        }
        memberTopic.setTitleText(memberText.toString());
        LinkedHashMap<String, ITopic> map =
                memberMap.computeIfAbsent(info.memberType, k -> new LinkedHashMap<>());
        map.put(info.sign, memberTopic);
    }

    /**
     * 空实现
     */
    @Override
    public void call(JavaInfo usageInfo, JavaInfo callInfo) {
    }

    /**
     * 空实现
     */
    @Override
    public void over(JavaInfo overInfo, JavaInfo parentInfo) {
    }

    @Override
    protected void beforeSave() {
        primarySheet.setTitleText("member");
        rootTopic.setStructureClass(XMindConstant.LOGIC_RIGHT);
        typeMemberMap.forEach((type, memberMap) -> {
            ITopic typeTopic = packageMap.get(type);
            memberMap.forEach((memberEnum, topicMap) -> {
                ITopic memberTopic = workbook.createTopic();
                memberTopic.setTitleText(memberEnum.desc + "\n" + memberEnum.string);
                memberTopic.setFolded(true);
                typeTopic.add(memberTopic);
                topicMap.forEach((s, iTopic) -> memberTopic.add(iTopic));
            });
        });
    }
}
