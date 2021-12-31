package io.github.styzf.code.parser.java.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import io.github.styzf.util.common.Conf;
import io.github.styzf.code.parser.java.api.JavaParse;
import io.github.styzf.code.parser.java.api.bean.JavaInfo;
import io.github.styzf.util.common.FileUtils;
import io.github.styzf.util.common.FilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Java 解析工具类
 * <br/>调用关系从上到下
 */
public class JavaParserMain {

    private static final Logger LOG = LoggerFactory.getLogger(JavaParserMain.class);
    public static final CombinedTypeSolver SOLVER = new CombinedTypeSolver();

    static {
        SOLVER.add(new ClassLoaderTypeSolver(ClassLoader.getSystemClassLoader()));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(SOLVER);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }

    /**
     * 添加源文件根目录
     */
    public static void addSolverDir(String srcRootDir) {
        SOLVER.add(new JavaParserTypeSolver(srcRootDir));
    }

    /**
     * 添加用于解析的 jar 包
     * <br/>路径获取命令：<br/>
     * mvn dependency:build-classpath
     */
    public static void addSolverJars(String pathToJars) {
        // mvn dependency:build-classpath
        JarTypeSolver jarTypeSolver = null;
        String[] pathToJarArr = pathToJars.split(";:");
        for (String pathToJar : pathToJarArr) {
            try {
                jarTypeSolver = new JarTypeSolver(pathToJar);
            } catch (IOException e) {
                LOG.error("addSolverJars fail\n", e);
            }
        }
        SOLVER.add(jarTypeSolver);
    }

    /**
     * 解析多个文件或目录
     * 用可变参数而不是 List 是因为 {@link File#listFiles(FileFilter)} 是数组
     */
    public static void parseDirs(List<JavaParse> javaParses, File... files) {
        Pattern includePath = Pattern.compile(Conf.PARSER_PATH_INCLUDE.get());
        Pattern excludePath = Pattern.compile(Conf.PARSER_PATH_EXCLUDE.get());
        FileUtils.deep(
                f -> parseFile(javaParses, f),
                f -> f.isDirectory() || FilterUtils.filter(FileUtils.canonicalPath(f), includePath, excludePath),
                files);
    }

    /**
     * 解析文件
     */
    private static void parseFile(List<JavaParse> javaParses, File file) {
        LOG.info("parseFile\tfile:///{}", FileUtils.canonicalPath(file));
        parseText(javaParses, FileUtils.read(file));
    }

    /**
     * 解析文本
     */
    private static void parseText(List<JavaParse> javaParses, String s) {
        CompilationUnit cu = StaticJavaParser.parse(s);
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
            JavaInfo classInfo = new JavaInfo();
            classInfo.packNames = packNames;
            ResolvedReferenceTypeDeclaration rt = type.resolve();
            InfoUtils.addTypeInfo(classInfo, rt, type);
            javaParses.forEach(v -> v.type(classInfo));

            MembersUtils.parseMembers(javaParses, classInfo, type, rt);
            if (!type.isClassOrInterfaceDeclaration()) {
                continue;
            }
            if (rt.isClass()) {
                ResolvedClassDeclaration rcd = rt.asClass();
                OverUtils.parseOver(javaParses, classInfo, type, rcd.getAllInterfaces());
                OverUtils.parseOver(javaParses, classInfo, type, rcd.getAllSuperClasses());
            }
            if (rt.isInterface()) {
                ResolvedInterfaceDeclaration rid = rt.asInterface();
                OverUtils.parseOver(javaParses, classInfo, type, rid.getAllInterfacesExtended());
            }
        }
    }

}
