package io.github.styzf.parser.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import io.github.styzf.api.FileParser;
import io.github.styzf.context.ParserContext;
import io.github.styzf.context.java.BaseJavaContext;
import io.github.styzf.context.java.JavaContext;
import io.github.styzf.parser.AbstractFileParser;
import io.github.styzf.parser.java.resolver.StaticOverResolver;
import io.github.styzf.parser.java.resolver.TypeResolver;
import io.github.styzf.parser.java.util.SolverUtils;
import io.github.styzf.util.common.Conf;
import io.github.styzf.util.common.FileUtils;
import io.github.styzf.util.common.FilterUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * java解析类
 *
 * @author styzf
 * @date 2021/12/15 20:51
 */
@Slf4j
public class JavaParserImpl extends AbstractFileParser {
    private static final CombinedTypeSolver SOLVER = new CombinedTypeSolver();
    
    private static final JavaContext JAVA_CONTEXT = new BaseJavaContext();
    
    static {
        SOLVER.add(new ClassLoaderTypeSolver(ClassLoader.getSystemClassLoader()));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(SOLVER);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }
    
    @Override
    public FileParser setArgs() {
        String pomPath = Conf.POM_PATH.get();
        SolverUtils.addSolverMavenJars(SOLVER, new File(pomPath), new HashSet<String>(), new HashSet<String>());
        return super.setArgs();
    }
    
    @Override
    public FileParser setParserContext(ParserContext parserContext) {
        super.context = JAVA_CONTEXT;
        return this;
    }
    
    @Override
    public FileParser parser(File... files) {
        FileUtils.deep(this::parseFile, this::filterFile, files);
        StaticOverResolver.parseRel();
        StaticOverResolver.parseOver(JAVA_CONTEXT);
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
        parseText(FileUtils.read(file));
    }
    
    /**
     * 主解析方法
     */
    private void parseText(String s) {
        CompilationUnit cu = StaticJavaParser.parse(s);
        TypeResolver.parserType(cu, JAVA_CONTEXT);
    }
    
}
