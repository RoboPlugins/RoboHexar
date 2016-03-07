package com.smilingrob.plugin.robohexar;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;

import java.util.ArrayList;
import java.util.List;

/**
 * Find grammar errors.
 */
public class GrammarParser {
    boolean inSentence = false;
    boolean hasPunctuation = false;
    boolean hasCommentText = false;
    boolean onNewLine = false;
    int lastWordCharacterIndex = 0;
    int startRange = 0;

    /**
     * Check for any grammar errors in the comment block.
     *
     * @param psiElement element to check for comment blocks.
     * @return list of grammar errors found.
     */
    public List<GrammarError> parse(PsiElement psiElement) {
        ArrayList<GrammarError> errors = new ArrayList<GrammarError>();


        if (psiElement instanceof PsiDocComment) {
            PsiDocComment docComment = (PsiDocComment) psiElement;

            startRange = docComment.getTextRange().getStartOffset();
            String text = docComment.getText();

            errors.addAll(parseStringForErrors(text));
        }

        return errors;
    }

    /**
     * Find all the grammar errors in a given text.
     *
     * @param text to search for errors.
     * @return list of errors in text.
     */
    private ArrayList<GrammarError> parseStringForErrors(String text) {
        ArrayList<GrammarError> errors = new ArrayList<GrammarError>();

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            char previousCharacter = i > 0 ? text.charAt(i - 1) : ' ';
            if (!inSentence) {
                if (isWordCharacter(character)) {
                    checkCapitalizationErrors(errors, i, character, previousCharacter);
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
                checkForEndPunctuationError(errors, character, previousCharacter);
            }
            if (onNewLine) {
                if (isWordCharacter(character)) {
                    checkForSpacingErrors(errors, i, previousCharacter);
                    onNewLine = false;
                }
            } else {
                if (isNewLineCharacter(character)) {
                    onNewLine = true;
                }
            }
        }

        if (hasCommentText && !hasPunctuation) {
            errors.add(new GrammarError(startRange + lastWordCharacterIndex, startRange + lastWordCharacterIndex + 1, GrammarError.Type.END_PUNCTUATION));
        }
        return errors;
    }

    /**
     * @param errors out errors.
     * @param i current location.
     * @param previousCharacter last character.
     */
    private void checkForSpacingErrors(ArrayList<GrammarError> errors, int i, char previousCharacter) {
        if (previousCharacter == '*') {
            errors.add(new GrammarError(startRange + i, startRange + i + 1, GrammarError.Type.DOC_SPACING));
        }
    }

    /**
     * @param errors out errors.
     * @param character current character.
     * @param previousCharacter last character.
     */
    private void checkForEndPunctuationError(ArrayList<GrammarError> errors, char character, char previousCharacter) {
        if (character == '@' && previousCharacter == ' ') {
            // end of main comment block
            if (!hasPunctuation) {
                errors.add(new GrammarError(startRange + lastWordCharacterIndex, startRange + lastWordCharacterIndex + 1, GrammarError.Type.END_PUNCTUATION));
            }
        }
    }

    /**
     * @param errors out errors.
     * @param i current index.
     * @param character current character.
     * @param previousCharacter last character.
     */
    private void checkCapitalizationErrors(ArrayList<GrammarError> errors, int i, char character, char previousCharacter) {
        if (isLowerCase(character)) {
            if (isSpace(previousCharacter)) {
                errors.add(new GrammarError(startRange + i, startRange + i + 1, GrammarError.Type.CAPITALIZATION));
            }
        }
    }

    /**
     * Any letter.
     *
     * @param character to test.
     * @return true if it is a letter.
     */
    static boolean isWordCharacter(char character) {
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z');
    }

    /**
     * Any lower-case letter.
     *
     * @param character to test.
     * @return true if it is a letter and is lowercase.
     */
    static boolean isLowerCase(char character) {
        return character >= 'a' && character <= 'z';
    }

    /**
     * Any sentence ending punctuation.  '.', '!', '?'.
     *
     * @param character to test.
     * @return true if is an ending punctuation.
     */
    static boolean isEndingPunctuation(char character) {
        return character == '.' || character == '!' || character == '?';
    }

    /**
     * @param character to test.
     * @return true if it is a spacing character or newline character.
     */
    static boolean isSpace(char character) {
        return character == ' ' || character == '\n' || character == '\r' || character == '\t';
    }

    /**
     * @param character to test.
     * @return true if it is a newline character.
     */
    static boolean isNewLineCharacter(char character) {
        return character == '\n' || character == '\r';
    }

}
