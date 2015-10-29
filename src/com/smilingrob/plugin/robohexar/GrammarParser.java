package com.smilingrob.plugin.robohexar;

import java.util.ArrayList;
import java.util.List;

/**
 * Find grammar errors.
 */
public class GrammarParser {
    boolean inSentence = false;
    boolean hasPunctuation = false;
    boolean hasCommentText = false;
    int lastWordCharacterIndex = 0;

    public List<GrammarError> parse(String text) {
        ArrayList<GrammarError> errors = new ArrayList<>();


        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            char previousCharacter = i > 0 ? text.charAt(i - 1) : ' ';
            if (!inSentence) {
                if (isWordCharacter(character)) {
                    if (isLowerCase(character)) {
                        if (isSpace(previousCharacter)) {
                            errors.add(new GrammarError(i, i + 1, GrammarError.Type.CAPITALIZATION));
                        } else if (previousCharacter == '*') {
                            errors.add(new GrammarError(i, i + 1, GrammarError.Type.DOC_SPACING));
                        }
                    }
                    inSentence = true;
                    hasPunctuation = false;
                    hasCommentText = true;
                    lastWordCharacterIndex = i;
                }
            } else {
                if (isEndingPunctuation(character)) {
                    inSentence = false;
                    hasPunctuation = true;
                } else if (isWordCharacter(character)) {
                    lastWordCharacterIndex = i;
                }
                if (character == '@' && previousCharacter == ' ') {
                    // end of main comment block
                    if (!hasPunctuation) {
                        errors.add(new GrammarError(lastWordCharacterIndex, lastWordCharacterIndex + 1, GrammarError.Type.END_PUNCTUATION));
                    }
                }
            }
        }

        if (hasCommentText && !hasPunctuation) {
            errors.add(new GrammarError(lastWordCharacterIndex, lastWordCharacterIndex + 1, GrammarError.Type.END_PUNCTUATION));
        }

        return errors;
    }

    static boolean isWordCharacter(char character) {
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z');
    }

    static boolean isLowerCase(char character) {
        return character >= 'a' && character <= 'z';
    }

    static boolean isEndingPunctuation(char character) {
        return character == '.' || character == '!' || character == '?';
    }

    static boolean isSpace(char character) {
        return character == ' ' || character == '\n' || character == '\r' || character == '\t';
    }

}
