package ch.ge.cti.nexus.nexusrmgui.utils;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

/**
 * Callback JUnit 5 qui trace le debut et la fin de l'execution de chaque methode de test.
 */
public class TestExecutionLogger implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        log("DEBUT", context);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        log("FIN  ", context);
    }

    private void log(String comment, ExtensionContext context) {
        var className = context.getTestClass().get().getSimpleName();
        var methodName = context.getTestMethod().get().getName();
        LoggerFactory.getLogger(className).info("*** " + comment + " de {} ***", methodName);
    }

}
