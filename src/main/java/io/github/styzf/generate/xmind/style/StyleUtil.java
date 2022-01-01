package io.github.styzf.generate.xmind.style;

import cn.hutool.core.util.ObjectUtil;
import org.xmind.core.IFileEntry;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 样式工具类
 * @author styzf
 * @date 2022/1/1 17:40
 */
public class StyleUtil {
    
    private static final Map<String, IStyle> STYLE_MAP = new HashMap<>();
    
    public static final String RED = "RED";
    
    private static IStyleSheet styleSheet;
    
    public static void setStyleSheet(IStyleSheet styleSheet) {
        StyleUtil.styleSheet = styleSheet;
    }
    
    public IStyle getRedBackgroundStyle() {
        IStyle style = STYLE_MAP.get(StyleUtil.RED);
        if (ObjectUtil.isNotNull(style)) {
            return style;
        }
        
        style = styleSheet.createStyle(IStyle.TOPIC);
        style.setProperty("fo:color", "#FF3C00FF");
        STYLE_MAP.put(StyleUtil.RED, style);
        return style;
    }
}
