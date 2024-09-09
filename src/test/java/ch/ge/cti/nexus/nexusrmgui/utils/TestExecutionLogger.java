/*
 * Copyright (C) <Date> Republique et canton de Geneve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
