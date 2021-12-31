package io.github.styzf;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.styzf.api.FileGenerate;
import io.github.styzf.api.FileParser;
import io.github.styzf.context.ParserContext;
import io.github.styzf.factory.GeneratorFactory;
import io.github.styzf.factory.ParserFactory;
import io.github.styzf.util.common.Conf;
import io.github.styzf.util.common.ConfUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 主流程生成类
 * @author styzf
 * @date 2021/12/15 20:59
 */
public class AutoGenerator {
    
    public void execute() {
        Conf.prop = ConfUtils.loadUtf8(Conf.confPath);
        String path = Conf.PARSER_PATH.get();
        Assert.notBlank(path, "parser_path，解析路径配置不能为空");
        File[] files = Arrays.stream(path.split("[,;|]"))
                    .map(File::new).toArray(File[]::new);
        execute(files);
    }
    
    /**
     * 主执行方法
     */
    public void execute(File... files) {
        if (ObjectUtil.isNull(Conf.prop)) {
            Conf.prop = ConfUtils.loadUtf8(Conf.confPath);
        }
        List<FileParser> fileParsers = ParserFactory.get();
        fileParsers.forEach(fileParser -> fileParser
                .setParserContext(null)
                .setFileGenerate(GeneratorFactory.get())
                .parser(files)
                .end());
    }
}
