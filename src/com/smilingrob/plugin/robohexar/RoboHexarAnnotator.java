package com.smilingrob.plugin.robohexar;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;

/**
 * Check for issues that Hexar usually brings up in his pull-request reviews.
 */
public class RoboHexarAnnotator implements Annotator {

    public void annotate(PsiElement psiElement, AnnotationHolder annotationHolder) {
        if (psiElement instanceof PsiDocComment) {
            PsiDocComment docComment = (PsiDocComment) psiElement;
            String text = docComment.getText();
            int startRange = psiElement.getTextRange().getStartOffset();

            boolean inSentence = false;
            boolean hasPunctuation = false;
            boolean hasCommentText = false;
            int lastWordCharacterIndex = 0;

            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);
                char previousCharacter = i > 0 ? text.charAt(i - 1) : ' ';
                if (!inSentence) {
                    if (isWordCharacter(character)) {
                        if (isLowerCase(character)) {
                            if (isSpace(previousCharacter)) {
                                TextRange textRange = new TextRange(startRange + i, startRange + i + 1);
                                annotationHolder.createErrorAnnotation(textRange, "Capitalize");
                            } else if (previousCharacter == '*') {
                                TextRange textRange = new TextRange(startRange + i, startRange + i + 1);
                                annotationHolder.createErrorAnnotation(textRange, "Space before text.");
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
                            TextRange textRange = new TextRange(startRange + lastWordCharacterIndex,
                                    startRange + lastWordCharacterIndex + 1);
                            annotationHolder.createErrorAnnotation(textRange, "Periods");
                        }
                    }
                }
            }

            if (hasCommentText && !hasPunctuation) {
                TextRange textRange = new TextRange(startRange + lastWordCharacterIndex,
                        startRange + lastWordCharacterIndex + 1);
                annotationHolder.createErrorAnnotation(textRange, "Add a period.");
            }

        }
    }

    static boolean isWordCharacter(char character) {
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z');
    }

    static boolean isUpperCase(char character) {
        return character >= 'A' && character <= 'Z';
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
