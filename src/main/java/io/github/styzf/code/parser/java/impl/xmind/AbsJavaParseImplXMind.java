package io.github.styzf.code.parser.java.impl.xmind;

import io.github.styzf.util.common.Conf;
import io.github.styzf.code.parser.java.api.JavaParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmind.core.*;
import org.xmind.core.style.IStyleSheet;

import java.io.File;

/**
 * XMind 思维导图/脑图 实现
 */
public abstract class AbsJavaParseImplXMind implements JavaParse {

    private static final Logger LOG = LoggerFactory.getLogger(AbsJavaParseImplXMind.class);

    public final File outDir;
    public final String outName;

    public AbsJavaParseImplXMind(File outDir, String outName) {
        this.outDir = outDir;
        this.outName = outName;
    }

    protected final IWorkbookBuilder workbookBuilder = Core.getWorkbookBuilder();
    protected final IWorkbook workbook = workbookBuilder.createWorkbook();
    protected final ISheet primarySheet = workbook.getPrimarySheet();
    protected final IStyleSheet styleSheet = workbook.getStyleSheet();
    protected final ITopic rootTopic = primarySheet.getRootTopic();

    protected final boolean showSymbol = "true".equals(Conf.PARSER_SHOW_SYMBOL.get());

    /**
     * 处理并生成思维导图文件
     */
    @Override
    public void end() {
        beforeSave();
        try {
            // 后缀大小写不对会导致打开软件没打开文件
            String path = new File(outDir, outName + "." + XMindConstant.XMIND).getCanonicalPath();
            workbook.save(path);
            LOG.info("思维导图/脑图：{}\tfile:///{}", tipName(), path.replace('\\', '/'));
        } catch (Exception e) {
            LOG.error("save fail", e);
        }
    }

    protected abstract String tipName();

    protected abstract void beforeSave();
}
