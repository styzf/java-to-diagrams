package io.github.styzf.parser.java.util;

import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取依赖清单
 */
class DepHandler implements InvocationOutputHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DepHandler.class);

    private boolean nextIsDep = false;
    String output;

    @Override
    public void consumeLine(String s) {
        if (s.startsWith("[INFO] Dependencies classpath:")) {
            nextIsDep = true;
        } else if (nextIsDep) {
            output = s;
            nextIsDep = false;
        } else if (s.startsWith("[ERROR]")) {
            LOG.error(s);
        }
    }
}
