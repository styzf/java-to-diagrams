package io.github.styzf.code.parser;

import io.github.styzf.code.parser.java.api.JavaParse;
import io.github.styzf.code.parser.java.impl.graphviz.JavaParseImplGraphviz;
import io.github.styzf.code.parser.java.impl.xmind.JavaParseImplXMind;
import io.github.styzf.code.parser.java.impl.xmind.JavaParseImplXMindCall;
import io.github.styzf.code.parser.java.parser.JavaParserMain;
import io.github.styzf.util.common.Conf;
import io.github.styzf.util.common.ConfUtils;
import io.github.styzf.util.common.FileUtils;
import io.github.styzf.util.common.JFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JavaParserCmd {

    /**
     * 命令行程序入口
     */
    public static void main(String... args) {
        if (args.length == 0) {
            String path = Conf.PARSER_PATH.get();
            if (path!=null) {
                args = path.split("[,;|]");
            }
        }
        if (args.length > 0) {
            File[] files = Arrays.stream(args).map(File::new).toArray(File[]::new);
            run(files);
            return;
        }
        run(JFileUtils.chooser());
    }

    /**
     * 程序入口
     */
    protected static void run(File... files) {
        Conf.prop = ConfUtils.loadUtf8(Conf.confPath);

        File outDir = new File(FileUtils.CLASS_PATH);

        String outName;
        if (files.length == 1) {
            outName = files[0].getAbsoluteFile().getName();
        } else {
            outName = Arrays.stream(files)
                    .map(File::getAbsoluteFile)
                    .map(File::getName)
                    .collect(Collectors.joining("+"));
        }
        outName += Conf.PARSER_OUT_SUFFIX.get();

        List<JavaParse> parsers = new ArrayList<>();
        parsers.add(new JavaParseImplXMind(outDir, outName));
        parsers.add(new JavaParseImplXMindCall(outDir, outName));
        parsers.add(new JavaParseImplGraphviz(outDir, outName));

        JavaParserMain.parseDirs(parsers, files);

        for (JavaParse parser : parsers) {
            parser.end();
        }
    }
}
