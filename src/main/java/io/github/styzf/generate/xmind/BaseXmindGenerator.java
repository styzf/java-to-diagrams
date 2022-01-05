package io.github.styzf.generate.xmind;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.java.javainfo.MemberInfo;
import io.github.styzf.generate.xmind.dict.XMindConstant;
import io.github.styzf.generate.xmind.style.StyleUtil;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.util.common.Conf;
import io.github.styzf.util.common.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmind.core.ITopic;
import org.xmind.core.style.IStyle;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author styzf
 * @date 2021/12/15 21:54
 */
public class BaseXmindGenerator extends AbstractXmindGenerator {
    
    private static final Logger LOG = LoggerFactory.getLogger(BaseXmindGenerator.class);
    
    public final File outDir = new File(FileUtils.CLASS_PATH);
    public final String outName = "test";
    
    /** 循环调用判断用 */
    public static Set<String> single = new HashSet<>();
    
    @Override
    public void generate(JavaContext javaContext) {
        StyleUtil.setStyleSheet(styleSheet);
        
        Map<String, MemberInfo> memberContext = javaContext.getMemberContext();
        String includePatternStr = Conf.PARSER_XMIND_METHOD_INCLUDE.get();
        Pattern includeMethod = Pattern.compile(includePatternStr);
        boolean notIncludePattern = StrUtil.isBlank(includePatternStr);
        
        memberContext.values().stream()
                .filter(memberInfo -> (notIncludePattern && MapUtil.isEmpty(memberInfo.usageInfo))
                        || includeMethod.matcher(memberInfo.sign).find())
                .filter(memberInfo -> MemberEnum.isMethod(memberInfo.memberType))
                .forEach(this::addRoot);
        end();
    }
    
    /**
     * 结束生成思维导图
     */
    public void end() {
        try {
            String path = new File(outDir, outName + "." + XMindConstant.XMIND).getCanonicalPath();
            workbook.save(path);
            LOG.info("思维导图/脑图：\tfile:///{}", path.replace('\\', '/'));
        } catch (Exception e) {
            LOG.error("save fail", e);
        }
    }
    
    /**
     * 添加到根目录
     */
    private void addRoot(MemberInfo memberInfo) {
        ITopic iTopic = generateTopic(memberInfo, 1);
        rootTopic.add(iTopic);
        generateCallTopic(memberInfo, iTopic);
    }
    
    /**
     * 遍历循环生成被调用方Topic
     * @param memberInfo 成员信息
     * @param lastTopic 上一个Topic节点，传入用于设置当前成员信息生成的topic添加到上一个节点
     */
    private void generateCallTopic(MemberInfo memberInfo, ITopic lastTopic) {
        List<MemberInfo> callInfoList = memberInfo.callInfo.values().stream()
                .filter(callInfo -> MemberEnum.isMethod(callInfo.memberType))
                .collect(Collectors.toList());
    
        int index = 1;
        for (MemberInfo callInfo : callInfoList) {
            if (MemberEnum.isGetSet(callInfo.memberType)) {
                continue;
            }
    
            IStyle style = StyleUtil.getRedBackgroundStyle();
    
            try {
                ITopic iTopic = generateTopic(callInfo, index++);
                iTopic.setStyleId(style.getId());
                lastTopic.add(iTopic);
                generateCallTopic(callInfo, iTopic);
            } catch (RuntimeException ignored) {index--;}
        }
        single.remove(memberInfo.sign);
    }
    
    /**
     * 生成ITopic
     * @param memberInfo 成员信息
     * @param index 索引号，用于生成调用步骤
     * @return 生成的ITopic
     */
    private ITopic generateTopic(MemberInfo memberInfo, int index) {
        boolean singleFlag = single.add(memberInfo.sign);
    
        // TODO 定义对应的业务异常类
        if (! singleFlag) {
            LOG.warn("存在循环调用：{}", memberInfo.sign);
            throw new RuntimeException("存在循环调用：" + memberInfo.sign);
        }
        
        ITopic topic = workbook.createTopic();
        String text = index + "、" + memberInfo.getCommentFirst() + "\n" + memberInfo.classInfo.name + "." + memberInfo.name + "()";
        topic.setTitleText(text);
        
        return topic;
    }
}
