package com.smilingrob.plugin.robohexar;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.List;

/**
 * Test JavaDoc parser.
 */
public class JavaDocParserTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testAnyHighlights() {
        boolean highlightedSomething = false;

        // GIVEN a file with no java docs
        myFixture.testHighlighting("JavaDocTestData.java");

        // WHEN highlighting is done
        List<HighlightInfo> highlights = myFixture.doHighlighting();

        // THEN we should get some kind of Java Doc warning
        String javaDocError = new JavaDocError(null, JavaDocError.ErrorType.MISSING_JAVA_DOC_METHOD).messageForError();
        for (HighlightInfo info : highlights) {
            if (javaDocError.equals(info.getDescription())) {
                highlightedSomething = true;
                break;
            }
        }

        assertTrue("Should have had some highlight.", highlightedSomething);
    }

    public void testJavaDocTestData() {
        myFixture.testHighlighting("JavaDocTestData.java");
    }
}