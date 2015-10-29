package com.smilingrob.plugin.robohexar;

/**
 * Model to hold detected grammar errors.
 */
public class GrammarError {
    public int indexStart;
    public int indexEnd;
    public Type type;

    public enum Type {
        CAPITALIZATION,
        END_PUNCTUATION,
        DOC_SPACING
    }

    public GrammarError(int indexStart, int indexEnd, Type type) {
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
        this.type = type;
    }

    public String stringForType(Type type) {
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

    public String messageForError() {
        return stringForType(type);
    }
}
