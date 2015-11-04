package com.smilingrob.plugin.robohexar;

import com.intellij.psi.PsiElement;

/**
 * An error relating to adding Java Docs.
 */
public class JavaDocError {
    private PsiElement elementToTag;
    private ErrorType errorType;

    public enum ErrorType {
        MISSING_JAVA_DOC
    }

    /**
     * @param elementToTag place to highlight the Java Doc error.
     * @param errorType type of Java Doc error.
     */
    public JavaDocError(PsiElement elementToTag, ErrorType errorType) {
        this.elementToTag = elementToTag;
        this.errorType = errorType;
    }

    /**
     * @return place to highlight the Java Doc error.
     */
    public PsiElement getElementToTag() {
        return elementToTag;
    }

    /**
     * @return message to be displayed to the user if this error happens.
     */
    public String messageForError() {
        switch (errorType) {
            case MISSING_JAVA_DOC:
                return "Java Doc!";
            default:
                return "";
        }
    }

}
