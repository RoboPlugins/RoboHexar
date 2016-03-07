package com.smilingrob.plugin.robohexar;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotEquals;

/**
 * Testing grammar in Java Docs.
 */
public class GrammarParserTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testJavaDocNoWarnings() throws Exception {
        // GIVEN a java file with good punctuation
        myFixture.configureByFiles("JavaDocGood.java");

        // WHEN highlighting is done
        List<HighlightInfo> highlights = myFixture.doHighlighting();
        ArrayList<HighlightInfo> warnings = new ArrayList<HighlightInfo>();
        for (HighlightInfo highlight : highlights) {
            if (highlight.getSeverity() == HighlightSeverity.WARNING) {
                warnings.add(highlight);
            }
        }

        // THEN we should not get any warnings
        if (warnings.size() > 0) {
            assertNull(warnings.get(0).getDescription());
            assertNull(warnings.get(0));
        }
    }

    public void testJavaDocNoWarningsWithLinks() throws Exception {
        // GIVEN a java file with good punctuation and links
        myFixture.configureByFiles("JavaDocGoodWithLinks.java");

        // WHEN highlighting is done
        List<HighlightInfo> highlights = myFixture.doHighlighting();
        ArrayList<HighlightInfo> warnings = new ArrayList<HighlightInfo>();
        for (HighlightInfo highlight : highlights) {
            if (highlight.getSeverity() == HighlightSeverity.WARNING) {
                warnings.add(highlight);
            }
        }

        // THEN we should not get any warnings
        if (warnings.size() > 0) {
            assertNull(warnings.get(0).getDescription());
            assertNull(warnings.get(0));
        }
    }

    public void testJavaDocEndPunctuationNormal() throws Exception {
        // GIVEN a java file with bad punctuation
        myFixture.configureByFiles("JavaDocEndPunctuationBadNormal.java");

        // WHEN highlighting is done
        List<HighlightInfo> highlights = myFixture.doHighlighting();

        // THEN we should get an END_PUNCTUATION error.
        String expectedErrorMessage = new GrammarError(0, 0, GrammarError.Type.END_PUNCTUATION).messageForError();

        assertNotEquals(0, highlights.size());
        assertEquals(expectedErrorMessage, highlights.get(0).getDescription());
    }

    public void testJavaDocEndPunctuationWithParams() throws Exception {
        // GIVEN a java file with bad punctuation
        myFixture.configureByFiles("JavaDocEndPunctuationBadParams.java");

        // WHEN highlighting is done
        List<HighlightInfo> highlights = myFixture.doHighlighting();
        ArrayList<HighlightInfo> warnings = new ArrayList<HighlightInfo>();
        for (HighlightInfo highlight : highlights) {
            if (highlight.getSeverity() == HighlightSeverity.WARNING) {
                warnings.add(highlight);
            }
        }

        // THEN we should get an END_PUNCTUATION error.
        String expectedErrorMessage = new GrammarError(0, 0, GrammarError.Type.END_PUNCTUATION).messageForError();

        assertEquals(1, warnings.size());
        HighlightInfo warning = warnings.get(0);
        assertEquals(expectedErrorMessage, warning.getDescription());
        assertEquals(130, warning.startOffset);
        assertEquals(131, warning.endOffset);
    }
}