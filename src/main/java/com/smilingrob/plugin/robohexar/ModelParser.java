package com.smilingrob.plugin.robohexar;

import com.intellij.psi.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Finds errors in model classes - classes that are annotated with @Generated("org.jsonschema2pojo").
 */
public class ModelParser {

    /**
     * Check for any model class errors.
     *
     * @param psiElement element to check for errors.
     * @return list of model class errors found.
     */
    public Set<ModelError> parse(PsiElement psiElement) {
        // Create errors list
        Set<ModelError> errors = new LinkedHashSet<ModelError>();

        // If the given element is a class
        if (psiElement instanceof PsiClass) {
            PsiClass clazz = (PsiClass) psiElement;

            // Iterate over its annotations
            if (clazz.getModifierList() != null) {
                for (PsiAnnotation annotation : clazz.getModifierList().getAnnotations()) {
                    // If this is a model class (generated via jsonschema2pojo)
                    if (annotation.getText().startsWith("@Generated") && annotation.getText().contains("jsonschema2pojo")) {
                        // Check for serializable
                        checkImplementsSerializable(clazz, errors);

                        // Check for non-alphabetized accessors
                        checkNonAlphabetizedAccessors(clazz, errors);

                        // Check for non-alphabetized fields
                        checkNonAlphabetizedFields(clazz, errors);
                    }
                }
            }
        }

        return errors;
    }

    /**
     * Determines whether or not the given class implements Serializable, and if so, marks a warning on the class name.
     *
     * @param clazz the class to check.
     * @param errors the list of errors to potentially add to.
     */
    private void checkImplementsSerializable(PsiClass clazz, Set<ModelError> errors) {
        // Iterate over the implements list
        for (PsiType impType : clazz.getImplementsListTypes()) {
            // If one of them is Serializable
            if (impType.getCanonicalText().equals("java.io.Serializable")) {
                // Create an error on the class name
                errors.add(new ModelError(clazz.getNameIdentifier(), ModelError.Type.SERIALIZABLE));
                break;
            }
        }
    }

    /**
     * Determines whether or not the accessors are alphabetized, and if not, marks a warning on the class name.
     *
     * @param clazz the class to check.
     * @param errors the list of errors to potentially add to.
     */
    private void checkNonAlphabetizedAccessors(PsiClass clazz, Set<ModelError> errors) {
        // Initialize the last seen accessor name to a blank string
        String lastAccessor = "";
        String lastField = "";

        // Iterate over fields in the class
        for (PsiMethod method : clazz.getMethods()) {
            // Ignore non-accessors
            if (method.getName().contains("get") || method.getName().contains("set")) {
                // Strip get/set from the accessor name
                String fieldName = method.getName().replace("get", "").replace("set", "");

                // If the current accessor's field name is alphabetically "less" than the previous
                if (fieldName.compareToIgnoreCase(lastField) < 0) {
                    // Create an error on the accessor AND the class name
                    errors.add(new ModelError(method.getNameIdentifier(), ModelError.Type.ACCESSORS_NOT_ALPHABETIZED));
                    errors.add(new ModelError(clazz.getNameIdentifier(), ModelError.Type.ACCESSORS_NOT_ALPHABETIZED));
                }

                // If the field name is the same, ensure get precedes set (g < s)
                if (fieldName.equals(lastField) && method.getName().compareToIgnoreCase(lastAccessor) < 0) {
                    // Create an error on the accessor AND the class name
                    errors.add(new ModelError(method.getNameIdentifier(), ModelError.Type.ACCESSORS_NOT_ALPHABETIZED));
                    errors.add(new ModelError(clazz.getNameIdentifier(), ModelError.Type.ACCESSORS_NOT_ALPHABETIZED));
                }

                // Save the last seen method name and accessor field
                lastAccessor = method.getName();
                lastField = fieldName;
            }
        }
    }

    /**
     * Determines whether or not the fields are alphabetized, and if not, marks a warning on the class name.
     *
     * @param clazz the class to check.
     * @param errors the list of errors to potentially add to.
     */
    private void checkNonAlphabetizedFields(PsiClass clazz, Set<ModelError> errors) {
        // Initialize the last seen field name to a blank string
        String lastField = "";

        // Iterate over fields in the class
        for (PsiField field : clazz.getFields()) {
            // If the current field name is alphabetically "less" than the previous
            if (field.getName() != null && field.getName().compareToIgnoreCase(lastField) < 0) {
                // Create an error on the field AND the class name
                errors.add(new ModelError(field.getNameIdentifier(), ModelError.Type.FIELDS_NOT_ALPHABETIZED));
                errors.add(new ModelError(clazz.getNameIdentifier(), ModelError.Type.FIELDS_NOT_ALPHABETIZED));
            }

            // Save the last seen field name
            lastField = field.getName();
        }
    }
}
