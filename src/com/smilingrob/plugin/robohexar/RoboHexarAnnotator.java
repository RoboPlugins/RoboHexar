package com.smilingrob.plugin.robohexar;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import java.awt.*;

/**
 * Check for issues that Hexar usually brings up in his pull-request reviews.
 */
public class RoboHexarAnnotator implements Annotator {

    PsiElement lastElement = null;

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        GrammarParser grammarParser = new GrammarParser();
        for (GrammarError error : grammarParser.parse(psiElement)) {
            annotationHolder.createWeakWarningAnnotation(error.rangeOfError(), error.messageForError());
        }

        JavaDocParser javaDocParser = new JavaDocParser();
        for (JavaDocError error : javaDocParser.parse(psiElement)) {
            annotateElement(error.getElementToTag(), error.messageForError());
        }

        ModelParser modelParser = new ModelParser();
        for (ModelError error : modelParser.parse(psiElement)) {
            annotateElement(error.getElementToTag(), error.messageForError());
        }

        lastElement = psiElement;
    }

    private void annotateElement(final PsiElement psiElement, final String message) {

        final TextAttributes textattributes = new TextAttributes(null, null, JBColor.RED, EffectType.WAVE_UNDERSCORE, Font.ITALIC);

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                final Project project = psiElement.getProject();
                final FileEditorManager editorManager = FileEditorManager.getInstance(project);
                final Editor editor = editorManager.getSelectedTextEditor();
                if (editor != null) {
                    editor.getMarkupModel().addRangeHighlighter(
                            psiElement.getTextOffset(),
                            psiElement.getTextOffset() + psiElement.getTextLength(),
                            HighlighterLayer.WARNING,
                            textattributes,
                            HighlighterTargetArea.EXACT_RANGE);

                    editor.addEditorMouseListener(new EditorMouseListener() {
                        @Override
                        public void mousePressed(EditorMouseEvent editorMouseEvent) {
                        }

                        @Override
                        public void mouseClicked(EditorMouseEvent editorMouseEvent) {
                            showHint(editorMouseEvent);
                        }

                        @Override
                        public void mouseReleased(EditorMouseEvent editorMouseEvent) {
                        }

                        @Override
                        public void mouseEntered(EditorMouseEvent editorMouseEvent) {
                            showHint(editorMouseEvent);
                        }

                        @Override
                        public void mouseExited(EditorMouseEvent editorMouseEvent) {}

                        private void showHint(EditorMouseEvent editorMouseEvent) {
                            if (editorMouseEvent.getArea().equals(EditorMouseEventArea.EDITING_AREA)) {
                                PsiIdentifier psiIdentifier = (PsiIdentifier) psiElement;
                                TextRange textRange = EditorUtil.getSelectionInAnyMode(editor);
                                TextRange elementTextRange = psiIdentifier.getTextRange();
                                int mouseOffset = textRange.getStartOffset();
                                int textStartOffset = elementTextRange.getStartOffset();
                                int textEndOffset = elementTextRange.getEndOffset();
                                if (mouseOffset >= textStartOffset && mouseOffset <= textEndOffset) {
                                    HintManager.getInstance().showErrorHint(editor, message);
                                }
                            }
                        }


                    });
                }
            }
        });
    }
}
