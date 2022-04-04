package io.github.styzf.parser.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import io.github.styzf.api.FileParser;
import io.github.styzf.context.java.BaseJavaContext;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.context.ParserContext;
import io.github.styzf.parser.AbstractFileParser;
import io.github.styzf.parser.java.resolver.OverResolver;
import io.github.styzf.parser.java.resolver.TypeResolver;
import io.github.styzf.util.common.Conf;
import io.github.styzf.util.common.FileUtils;
import io.github.styzf.util.common.FilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * java解析类
 *
 * @author styzf
 * @date 2021/12/15 20:51
 */
public class JavaParserImpl extends AbstractFileParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(JavaParserImpl.class);
    
    private static final CombinedTypeSolver SOLVER = new CombinedTypeSolver();
    
    private static final JavaContext JAVA_CONTEXT = new BaseJavaContext();
    
    static {
        SOLVER.add(new ClassLoaderTypeSolver(ClassLoader.getSystemClassLoader()));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(SOLVER);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }
    
    @Override
    public FileParser setParserContext(ParserContext parserContext) {
        super.context = JAVA_CONTEXT;
        return this;
    }
    
    @Override
    public FileParser parser(File... files) {
        FileUtils.deep(file -> parseFile(file), this::filterFile, files);
        OverResolver.parseOver(JAVA_CONTEXT);
        return this;
    }
    
    /**
     * 过滤文件
     */
    private boolean filterFile(File file) {
        Pattern includePath = Pattern.compile(Conf.PARSER_PATH_INCLUDE.get());
        Pattern excludePath = Pattern.compile(Conf.PARSER_PATH_EXCLUDE.get());
        return file.isDirectory() || FilterUtils.filter(FileUtils.canonicalPath(file), includePath, excludePath);
    }
    
    private void parseFile(File file) {
        LOG.info("parseFile\tfile:///{}", FileUtils.canonicalPath(file));
        parseText(FileUtils.read(file));
    }
    
    /**
     * 主解析方法
     */
    private void parseText(String s) {
        CompilationUnit cu = StaticJavaParser.parse(s);
        // todo 类的包名，暂时不丢到info里面，应该要考虑在里面实现
        List<String> packNames = new ArrayList<>();
        cu.getPackageDeclaration().ifPresent(e -> e.getChildNodes().get(0).stream()
                .map(Objects::toString)
                .forEach(packNames::add));
        // todo 实现解析包注释
        // StaticJavaParser.parseJavadoc(cu.getComment().get().getContent()).getDescription().toText();
        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type.isAnnotationDeclaration()) {
                continue;
            }
            TypeResolver.parserType(type, JAVA_CONTEXT);
        }
    }
    
}
