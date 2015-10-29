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
            int lastCharacterIndex = 0;

            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);
                char previousCharacter = i > 0 ? text.charAt(i - 1) : ' ';
                if (!inSentence) {
                    if (character >= 'a' && character <= 'z') {
                        TextRange textRange = new TextRange(startRange + i, startRange + i + 1);
                        annotationHolder.createErrorAnnotation(textRange, "Capitalize");
                        inSentence = true;
                        hasPunctuation = false;
                        hasCommentText = true;
                        lastCharacterIndex = i;
                    } else if (character >= 'A' && character <= 'Z') {
                        inSentence = true;
                        hasPunctuation = false;
                        hasCommentText = true;
                        lastCharacterIndex = i;
                    }
                } else {
                    if (character == '.' || character == '!' || character == '?') {
                        inSentence = false;
                        hasPunctuation = true;
                    } else if ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z')) {
                        lastCharacterIndex = i;
                    }
                }
                if (character == '@' && previousCharacter == ' ') {
                    // end of main comment block
                    if (hasCommentText && !hasPunctuation) {
                        TextRange textRange = new TextRange(startRange + lastCharacterIndex,
                                startRange + lastCharacterIndex + 1);
                        annotationHolder.createErrorAnnotation(textRange, "Periods");
                    }
                }

            }

            if (hasCommentText && !hasPunctuation) {
                TextRange textRange = new TextRange(startRange + lastCharacterIndex,
                        startRange + lastCharacterIndex + 1);
                annotationHolder.createErrorAnnotation(textRange, "Add a period.");
            }

        }
    }
}
