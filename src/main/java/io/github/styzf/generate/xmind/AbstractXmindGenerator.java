package io.github.styzf.generate.xmind;

import io.github.styzf.api.FileGenerate;
import io.github.styzf.context.java.JavaContext;
import org.xmind.core.*;
import org.xmind.core.style.IStyleSheet;

/**
 * Xmind基础实现
 * @author styzf
 * @date 2021/12/15 21:18
 */
public abstract class AbstractXmindGenerator implements FileGenerate<JavaContext> {
    
    protected final IWorkbookBuilder workbookBuilder = Core.getWorkbookBuilder();
    protected final IWorkbook workbook = workbookBuilder.createWorkbook();
    protected final ISheet primarySheet = workbook.getPrimarySheet();
    protected final IStyleSheet styleSheet = workbook.getStyleSheet();
    protected final ITopic rootTopic = primarySheet.getRootTopic();
    
}
