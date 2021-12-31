package io.github.styzf.generate.xmind;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import io.github.styzf.context.JavaContext;
import io.github.styzf.context.ParserContext;
import io.github.styzf.context.javainfo.MemberInfo;
import io.github.styzf.generate.xmind.dict.XMindConstant;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.util.common.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmind.core.ITopic;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author styzf
 * @date 2021/12/15 21:54
 */
public class BaseXmindGenerator extends AbstractXmindGenerator {
    
    private static final Logger LOG = LoggerFactory.getLogger(BaseXmindGenerator.class);
    
    public final File outDir = new File(FileUtils.CLASS_PATH);
    public final String outName = "test";
    
    // TODO 这个要作为服务编排进行处理
    @Override
    public void generate(JavaContext javaContext) {
        Map<String, MemberInfo> memberContext = javaContext.getMemberContext();
        // TODO 这里的过滤器预留指定类和方法的需求
        memberContext.values().stream()
                .filter(memberInfo -> MapUtil.isEmpty(memberInfo.usageInfo))
                .filter(memberInfo -> MemberEnum.isMethod(memberInfo.memberType))
                .forEach(this::addRoot);
        end();
    }
    
    public void end() {
        try {
            // 后缀大小写不对会导致打开软件没打开文件
            String path = new File(outDir, outName + "." + XMindConstant.XMIND).getCanonicalPath();
            workbook.save(path);
            LOG.info("思维导图/脑图：{}\tfile:///{}", path.replace('\\', '/'));
        } catch (Exception e) {
            LOG.error("save fail", e);
        }
    }
    
    private void addRoot(MemberInfo memberInfo) {
        ITopic iTopic = generateTopic(memberInfo);
        rootTopic.add(iTopic);
        generateCallTopic(memberInfo, iTopic);
    }
    
    private void generateCallTopic(MemberInfo memberInfo, ITopic lastTopic) {
        List<MemberInfo> callInfoList = memberInfo.callInfo.values().stream()
                .filter(callInfo -> MemberEnum.isMethod(callInfo.memberType))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(callInfoList)) {
            return;
        }
        // TODO 还要再加一段读取实现或者父方法的逻辑
    
        for (MemberInfo callInfo: callInfoList) {
            // todo 递归循环调用处理，另外循环调用怎么处理
            if (memberInfo.equals(callInfo)) {
                continue;
            }
            if (MemberEnum.isGetSet(callInfo.memberType)) {
                continue;
            }
    
            ITopic iTopic = generateTopic(callInfo);
            lastTopic.add(iTopic);
            generateCallTopic(callInfo, iTopic);
        }
    }
    
    private ITopic generateTopic(MemberInfo memberInfo) {
        ITopic topic = workbook.createTopic();
        String text = memberInfo.commentFirst + "\n" + memberInfo.sign;
        topic.setTitleText(text);
        return topic;
    }
}
