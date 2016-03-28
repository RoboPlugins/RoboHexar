package com.smilingrob.plugin.robohexar;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.codeInsight.preview.ElementPreviewHintProvider;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import com.intellij.ui.LightweightHint;
import com.intellij.util.Icons;
import com.intellij.util.xml.*;
import com.intellij.util.xml.reflect.AbstractDomChildrenDescription;
import com.intellij.util.xml.reflect.DomGenericInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;

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
//            annotateElement(error.getElementToTag(), error.messageForError());
        }

        JavaDocParser javaDocParser = new JavaDocParser();
        for (JavaDocError error : javaDocParser.parse(psiElement)) {
//            annotationHolder.createWeakWarningAnnotation(error.getElementToTag(), error.messageForError());
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

//        MyPsiElementCellRenderer renderer = new MyPsiElementCellRenderer(DomManager.getDomManager(element.getProject()));
//        if (elements.size() > 1) {
//            icon = AllIcons.Icons.W16;
//        } else {
//            icon = renderer.getIcon(Iterables.getFirst(elements, null));
//        }


//        builder.setNamer(new XmlFileByElementNamer());
//        builder.setCellRenderer(renderer);
//        result.add(builder.createLineMarkerInfo(identifier));


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
                }

                editor.addEditorMouseListener(new EditorMouseListener() {
                    @Override
                    public void mousePressed(EditorMouseEvent editorMouseEvent) {
                        PluginManager.getLogger().warn("mousePressed");
                    }

                    @Override
                    public void mouseClicked(EditorMouseEvent editorMouseEvent) {
                        PluginManager.getLogger().warn("mouseClicked");

                    }

                    @Override
                    public void mouseReleased(EditorMouseEvent editorMouseEvent) {
                        PluginManager.getLogger().warn("mouseReleased");

                    }

                    @Override
                    public void mouseEntered(EditorMouseEvent editorMouseEvent) {
                        PluginManager.getLogger().warn("mouseEntered");

                        Point mousePoint = editorMouseEvent.getMouseEvent().getPoint();
//                        Point mousePoint = editorMouseEvent.getMouseEvent().getLocationOnScreen();

//                        ElementPreviewHintProvider elementPreviewHintProvider = new ElementPreviewHintProvider();
//                        elementPreviewHintProvider.show(psiElement, editor, mousePoint, false);


                        JLabel jLabel = new JLabel(message);
d
                        LightweightHint lightweightHint = new LightweightHint(jLabel);

                        HintManagerImpl hintManager = (HintManagerImpl)HintManager.getInstance();
                        hintManager.showEditorHint(
                                lightweightHint,
                                editor,
                                mousePoint,
                                HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_OTHER_HINT | HintManager.HIDE_BY_SCROLLING,
                                0,
                                false);
                                HintManagerImpl.createHintHint(editor,mousePoint,lightweightHint,HintManager.UNDER).setAwtTooltip(false));

                        HintManager.getInstance().showInformationHint(editor, message);
                        HintManager.getInstance().showErrorHint(editor, message, (short)34);

//                        int offset = editor.getCaretModel().getOffset();
//                        boolean chosen = GotoDeclarationAction.chooseAmbiguousTarget(editor, offset, processor,
//                                FindBundle.message("find.usages.ambiguous.title", "crap"), null);
                    }

                    @Override
                    public void mouseExited(EditorMouseEvent editorMouseEvent) {
                        PluginManager.getLogger().warn("mouseExited");

                    }
                });
                PluginManager.getLogger().warn("Text Highlighted.");
            }
        });


//            Annotation annotation = new Annotation(0,0, HighlightSeverity.WEAK_WARNING, error.messageForError(), error.messageForError());
//            TextAttributesKey textAttributesKey = TextAttributesKey.createTextAttributesKey("externalName", textattributes);
//            annotation.setTextAttributes(textAttributesKey);
    }
}
