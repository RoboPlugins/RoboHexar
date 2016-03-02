package com.smilingrob.plugin.robohexar;

import com.intellij.openapi.util.TextRange;

/**
 * Model to hold detected grammar errors.
 */
public class GrammarError {
    private int indexStart;
    private int indexEnd;
    private Type type;

    /**
     * What kind of error this is highlighting.
     */
    public enum Type {
        CAPITALIZATION,
        END_PUNCTUATION,
        DOC_SPACING
    }

    /**
     * Model to hold detected grammar errors.
     *
     * @param indexStart string index where error starts.
     * @param indexEnd   string index where error ends.
     * @param type       GrammarError.Type of the error.
     */
    public GrammarError(int indexStart, int indexEnd, Type type) {
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
        this.type = type;
    }

    /**
     * Return the message to be displayed to the user for this error.
     * //TODO test todo parsing
     * @return message to be displayed to the user.
     */
    public String messageForError() {
        switch (type) {
            case CAPITALIZATION:
                return "Capitalize.";
            case END_PUNCTUATION:
                return "Missing period.";
            case DOC_SPACING:
                return "Inconsistent spacing.";
            default:
                return "";
        }
    }

    /**
     * Generate a TextRange of where to highlight the error.
     *
     * @return TextRange of where to highlight the error.
     */
    public TextRange rangeOfError() {
        return new TextRange(indexStart, indexEnd);
    }
}
