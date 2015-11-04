package com.smilingrob.plugin.robohexar;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiClassImpl;
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
            PsiMethodImpl psiMethod = (PsiMethodImpl) psiElement;
            if (psiMethod.getDocComment() == null) {
                if (!hasOverrideAnnotation(psiMethod) && !isGetterOrSetter(psiMethod)) {
                    PsiElement name = psiMethod.getNameIdentifier();
                    if (name != null) {
                        errors.add(new JavaDocError(name, JavaDocError.ErrorType.MISSING_JAVA_DOC));
                    }
                }
            }
        } else if (psiElement instanceof PsiClassImpl) {
            PsiClassImpl psiClass = (PsiClassImpl) psiElement;
            if (psiClass.getDocComment() == null) {
                PsiElement name = psiClass.getNameIdentifier();
                if (name != null) {
                    errors.add(new JavaDocError(name, JavaDocError.ErrorType.MISSING_JAVA_DOC));
                }
            }
        }
        return errors;
    }

    // not sure why it's not asking for JD here.
    private boolean hasOverrideAnnotation(@NotNull PsiMethodImpl psiMethod) {
        for (PsiElement childElement : psiMethod.getChildren()) {
            String name = childElement.getText();
            if (name != null && name.contains("@Override")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param psiMethodImpl method node.
     * @return true if it is a getter or a setter.
     */
    private boolean isGetterOrSetter(@NotNull PsiMethodImpl psiMethodImpl) {
        PsiElement nameElement = psiMethodImpl.getNameIdentifier();
        if (nameElement != null) {
            String name = nameElement.getText();
            if (name != null) {
                if (name.startsWith("get") || name.startsWith("set")) {
                    return true;
                }
            }
        }
        return false;
    }
}
