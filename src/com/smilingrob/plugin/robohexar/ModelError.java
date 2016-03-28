package com.smilingrob.plugin.robohexar;

import com.intellij.psi.PsiElement;

/**
 * Holds detected model class errors.
 */
public class ModelError {

    public enum Type {
        ACCESSORS_NOT_ALPHABETIZED,
        FIELDS_NOT_ALPHABETIZED,
        SERIALIZABLE,
    }

    private PsiElement mElementToTag;
    private Type mType;

    /**
     * Model to hold detected model class errors.
     *
     * @param elementToTag place to highlight the model class error.
     * @param type type of model class error.
     */
    public ModelError(PsiElement elementToTag, Type type) {
        mElementToTag = elementToTag;
        mType = type;
    }

    /**
     * @return place to highlight the Java Doc error.
     */
    public PsiElement getElementToTag() {
        return mElementToTag;
    }

    /**
     * Return the message to be displayed to the user for this error.
     *
     * @return message to be displayed to the user.
     */
    public String messageForError() {
        switch (mType) {
            case ACCESSORS_NOT_ALPHABETIZED:
                return "Hey dummy, accessors are not alphabetized in this class.";
            case FIELDS_NOT_ALPHABETIZED:
                return "Fields not alphabetized in class.  What is wrong with you.";
            case SERIALIZABLE:
                return "Model classes should not implement Serializable.  We are not animals.";
            default:
                return "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ModelError) {
            ModelError other = (ModelError) o;
            if (mType.equals(other.mType) && mElementToTag.equals(other.mElementToTag)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return mType.hashCode() * 17 + mElementToTag.hashCode() * 31;
    }
}
