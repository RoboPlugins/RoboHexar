package com.smilingrob.plugin.robohexar;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.ui.JBColor;
import com.intellij.ui.LightweightHint;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Check for issues that Hexar usually brings up in his pull-request reviews.
 */
public class RoboHexarAnnotator implements Annotator {

    private PsiElement lastElement = null;
    private static final Key<String> mKey = new Key<String>("RoboHexarHighlighter");
    private static final String HIGHLIGHT_ON = "HIGHLIGHT_ON";
    private static final String HIGHLIGHT_OFF = "HIGHLIGHT_OFF";
    private static final JBColor ERROR_COLOR = JBColor.YELLOW;
    private static final int HINT_TIMEOUT = 50000;

    @Override
    public void annotate(@NotNull final PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        boolean errorFound = false;

        GrammarParser grammarParser = new GrammarParser();
        for (GrammarError error : grammarParser.parse(psiElement)) {
            errorFound = true;
            annotationHolder.createWeakWarningAnnotation(error.rangeOfError(), error.messageForError());
        }

        JavaDocParser javaDocParser = new JavaDocParser();
        for (JavaDocError error : javaDocParser.parse(psiElement)) {
            errorFound = true;
            annotateElement(true, error.getElementToTag(), error.messageForError());
        }

        ModelParser modelParser = new ModelParser();
        for (final ModelError error : modelParser.parse(psiElement)) {
            errorFound = true;
            annotateElement(true, error.getElementToTag(), error.messageForError());
        }

        lastElement = psiElement;

        if (!errorFound) {
            annotateElement(false, psiElement, null);
        }
    }

    /**
     * Turns ON and OFF annotations/highlighting on a PSIElement
     *
     * @param turnOnHighlight turn ON or OFF based on this parameter.
     * @param psiElement      The element in question to annotate.
     * @param message         The error message displayed when turned ON.
     */
    private void annotateElement(final boolean turnOnHighlight, final PsiElement psiElement, final String message) {

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                Project project = psiElement.getProject();
                FileEditorManager editorManager = FileEditorManager.getInstance(project);
                Editor editor = editorManager.getSelectedTextEditor();
                if (editor != null) {
                    RangeHighlighter[] rangeHighlighters = editor.getMarkupModel().getAllHighlighters();
                    if (turnOnHighlight) {
                        TextAttributes textattributes = new TextAttributes(null, null, ERROR_COLOR, EffectType.WAVE_UNDERSCORE, Font.ITALIC);
                        RangeHighlighter rangeHighlight = editor.getMarkupModel().addRangeHighlighter(
                                psiElement.getTextOffset(),
                                psiElement.getTextOffset() + psiElement.getTextLength(),
                                HighlighterLayer.WARNING,
                                textattributes,
                                HighlighterTargetArea.EXACT_RANGE);

                        rangeHighlight.putUserData(mKey, HIGHLIGHT_ON);
                        rangeHighlight.setErrorStripeTooltip(message);
                        rangeHighlight.setThinErrorStripeMark(true);
                        rangeHighlight.setErrorStripeMarkColor(ERROR_COLOR);
                        editor.addEditorMouseListener(new RoboMouseListener(psiElement, message));
//                        editor.addEditorMouseMotionListener(new EditorMouseMotionListener() {
//                            @Override
//                            public void mouseMoved(EditorMouseEvent editorMouseEvent) {
//                                showHint(editorMouseEvent, psiElement, message);
//                            }
//
//                            @Override
//                            public void mouseDragged(EditorMouseEvent editorMouseEvent) {
//                            }
//                        });
                    } else {
                        for (RangeHighlighter rangeHighlighter : rangeHighlighters) {
                            if (doesRangeMatch(rangeHighlighter, psiElement.getTextRange())) {
                                String userDataStr = rangeHighlighter.getUserData(mKey);
                                if (userDataStr != null && userDataStr.equalsIgnoreCase(HIGHLIGHT_ON)) {
                                    rangeHighlighter.putUserData(mKey, HIGHLIGHT_OFF);
                                    editor.getMarkupModel().removeHighlighter(rangeHighlighter);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Convenience function that compares two elements range.
     *
     * @param rangeHighlighter a highlighter that contains a range.
     * @param textRange        the range we are trying to match.
     * @return true if the ranges match.
     */
    private boolean doesRangeMatch(RangeHighlighter rangeHighlighter, TextRange textRange) {
        return rangeHighlighter.getEndOffset() == textRange.getEndOffset() &&
                rangeHighlighter.getStartOffset() == textRange.getStartOffset();
    }

    private class RoboMouseListener implements EditorMouseListener {

        private PsiElement mPsiElement;
        private String mMessage;

        RoboMouseListener(PsiElement psiElement, String message) {
            mPsiElement = psiElement;
            mMessage = message;
        }

        @Override
        public void mousePressed(EditorMouseEvent editorMouseEvent) {
        }

        @Override
        public void mouseClicked(EditorMouseEvent editorMouseEvent) {
            showHint(editorMouseEvent, mPsiElement, mMessage);
        }

        @Override
        public void mouseReleased(EditorMouseEvent editorMouseEvent) {
        }

        @Override
        public void mouseEntered(EditorMouseEvent editorMouseEvent) {
        }

        @Override
        public void mouseExited(EditorMouseEvent editorMouseEvent) {
        }
    }

    /**
     * If the mouse event is within the area of the PSI element, a "hint" is displayed.
     *
     * @param editorMouseEvent The mouse event containing the location of the mouse pointer.
     */
    private void showHint(EditorMouseEvent editorMouseEvent, PsiElement psiElement, String message) {
        Editor editor = editorMouseEvent.getEditor();
        if (editorMouseEvent.getArea().equals(EditorMouseEventArea.EDITING_AREA)) {
            TextRange textRange = EditorUtil.getSelectionInAnyMode(editor);
            TextRange elementTextRange = psiElement.getTextRange();
            int mouseOffset = textRange.getStartOffset();
            int textStartOffset = elementTextRange.getStartOffset();
            int textEndOffset = elementTextRange.getEndOffset();
            // If the mouse is within the element text range, then show a hint.
            if (mouseOffset >= textStartOffset && mouseOffset <= textEndOffset) {
                JBLabel label = new JBLabel();
                label.setText(message);
                LightweightHint hint = new LightweightHint(label);
                LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();
                Point hintPoint = HintManagerImpl.getHintPosition(hint, editor, logicalPosition,
                        HintManagerImpl.RIGHT_UNDER);
                HintManagerImpl.getInstanceImpl().showEditorHint(hint, editor, hintPoint,
                                HintManagerImpl.HIDE_BY_ANY_KEY |
                                HintManagerImpl.HIDE_BY_OTHER_HINT |
                                HintManagerImpl.HIDE_BY_SCROLLING |
                                HintManagerImpl.HIDE_BY_TEXT_CHANGE |
                                HintManagerImpl.HIDE_IF_OUT_OF_EDITOR, HINT_TIMEOUT, true);
            }
        }
    }
}
