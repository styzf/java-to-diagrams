package io.github.styzf.generate.xmind;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.github.styzf.context.JavaContext;
import io.github.styzf.context.ParserContext;
import io.github.styzf.context.javainfo.MemberInfo;
import io.github.styzf.generate.xmind.dict.XMindConstant;
import io.github.styzf.parser.java.dict.MemberEnum;
import io.github.styzf.util.common.Conf;
import io.github.styzf.util.common.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmind.core.ITopic;

import java.io.File;
import java.util.List;
import java.util.Map;
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
    
    @Override
    public void generate(JavaContext javaContext) {
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
    
    public void end() {
        try {
            // 后缀大小写不对会导致打开软件没打开文件
            String path = new File(outDir, outName + "." + XMindConstant.XMIND).getCanonicalPath();
            workbook.save(path);
            LOG.info("思维导图/脑图：\tfile:///{}", path.replace('\\', '/'));
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
        // TODO 还要再加一段读取实现或者父方法的逻辑。这个在解析那边完成
        
        for (MemberInfo callInfo : callInfoList) {
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
