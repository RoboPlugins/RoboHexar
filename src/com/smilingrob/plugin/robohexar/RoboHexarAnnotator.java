package com.smilingrob.plugin.robohexar;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Check for issues that Hexar usually brings up in his pull-request reviews.
 */
public class RoboHexarAnnotator implements Annotator {

    PsiElement lastElement = null;

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        GrammarParser grammarParser = new GrammarParser();
        for (GrammarError error : grammarParser.parse(psiElement)) {
            annotationHolder.createWarningAnnotation(error.rangeOfError(), error.messageForError());
        }

        JavaDocParser javaDocParser = new JavaDocParser();
        for (JavaDocError error : javaDocParser.parse(psiElement)) {
            annotationHolder.createWarningAnnotation(error.getElementToTag(), error.messageForError());
        }

        lastElement = psiElement;
    }


}
