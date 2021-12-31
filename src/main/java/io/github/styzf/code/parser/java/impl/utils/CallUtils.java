package io.github.styzf.code.parser.java.impl.utils;

import io.github.styzf.code.parser.java.api.bean.JavaInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class CallUtils {

    /**
     * 重写方法联系
     */
    public static <T> void forOver(List<JavaInfo[]> overList,
                                    LinkedHashMap<String, LinkedHashMap<String, T>> typeMethodMap,
                                    BiConsumer<T, T> link) {
        for (JavaInfo[] infos : overList) {
            JavaInfo overInfo = infos[0];
            JavaInfo parentInfo = infos[1];
            LinkedHashMap<String, T> parentMethodMap = typeMethodMap.get(parentInfo.classInfo.sign);
            // 如果实现的接口不在扫描的类中则跳过
            if (parentMethodMap == null) {
                continue;
            }
            LinkedHashMap<String, T> overMethodMap = typeMethodMap.get(overInfo.classInfo.sign);
            T parent = parentMethodMap.get(parentInfo.sign);
            T over = overMethodMap.get(overInfo.sign);
            link.accept(parent, over);
        }
    }

}
