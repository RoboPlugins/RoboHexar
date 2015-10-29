package com.smilingrob.plugin.robohexar;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Check for issues that Hexar usually brings up in his pull-request reviews.
 */
public class RoboHexarAnnotator implements Annotator {

    PsiElement lastElement = null;

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

        if (psiElement instanceof PsiMethodImpl) {
            if (((PsiMethodImpl) psiElement).getDocComment() == null) {

                if (!hasOverrideAnnotation(psiElement)) {
                    PsiElement name = ((PsiMethodImpl) psiElement).getNameIdentifier();
                    if (name != null) {
                        annotationHolder.createErrorAnnotation(name, "Java Doc!");
                    }
                }
            }
        }

        lastElement = psiElement;
    }

    private boolean hasOverrideAnnotation(@NotNull PsiElement psiElement) {
        for (PsiElement childElement : psiElement.getChildren()) {
            String name = childElement.getText();
            if (name != null && name.contains("@Override")) {
                return true;
            }
        }
        return false;
    }

}
