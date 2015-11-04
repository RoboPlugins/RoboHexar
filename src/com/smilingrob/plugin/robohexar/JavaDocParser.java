package com.smilingrob.plugin.robohexar;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiMethodImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Check for things that need JavaDocs.
 */
public class JavaDocParser {

    /**
     * Get any Java Doc errors from the element.
     *
     * @param psiElement check for Java Doc errors.
     * @return list of errors to mark.
     */
    public List<JavaDocError> parse(PsiElement psiElement) {
        ArrayList<JavaDocError> errors = new ArrayList<JavaDocError>();
        if (psiElement instanceof PsiMethodImpl) {
            if (((PsiMethodImpl) psiElement).getDocComment() == null) {
                if (!hasOverrideAnnotation(psiElement)) {
                    PsiElement name = ((PsiMethodImpl) psiElement).getNameIdentifier();
                    if (name != null) {
                        errors.add(new JavaDocError(name, JavaDocError.ErrorType.MISSING_JAVA_DOC));
                    }
                }
            }
        }
        return errors;
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
