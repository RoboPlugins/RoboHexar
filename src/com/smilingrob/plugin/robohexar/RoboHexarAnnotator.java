package com.smilingrob.plugin.robohexar;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Check for issues that Hexar usually brings up in his pull-request reviews.
 */
public class RoboHexarAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof PsiDocComment) {
            PsiDocComment docComment = (PsiDocComment) psiElement;
            String text = docComment.getText();

            GrammarParser grammarParser = new GrammarParser();
            List<GrammarError> errorList = grammarParser.parse(text);

            int startRange = psiElement.getTextRange().getStartOffset();
            for (GrammarError error : errorList) {
                TextRange textRange = new TextRange(startRange + error.indexStart, startRange + error.indexEnd);
                annotationHolder.createErrorAnnotation(textRange, error.messageForError());
            }
        }
    }

}
