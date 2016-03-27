package com.smilingrob.plugin.robohexar;

import com.google.common.collect.Iterables;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.javaee.model.xml.Icon;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import com.intellij.util.Icons;
import com.intellij.util.xml.*;
import com.intellij.util.xml.reflect.AbstractDomChildrenDescription;
import com.intellij.util.xml.reflect.DomGenericInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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


        ApplicationManager.getApplication().invokeLater(new Runnable(){
            public void run(){
                    final Project project = psiElement.getProject();
                    final FileEditorManager editorManager = FileEditorManager.getInstance(project);
                    final Editor editor = editorManager.getSelectedTextEditor();
                    if(editor != null) {
                        editor.getMarkupModel().addRangeHighlighter(
                                psiElement.getTextOffset(),
                                psiElement.getTextOffset() + psiElement.getTextLength(),
                                HighlighterLayer.WARNING,
                                textattributes,
                                HighlighterTargetArea.EXACT_RANGE);
                    }

                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(Icons.FOLDER_ICON);
                builder.setTargets(psiElement);
                builder.setPopupTitle(message);
                builder.setTooltipTitle(message);
                builder.createLineMarkerInfo(psiElement);

                PluginManager.getLogger().warn("Text Highlighted.");
            }
        });




//            Annotation annotation = new Annotation(0,0, HighlightSeverity.WEAK_WARNING, error.messageForError(), error.messageForError());
//            TextAttributesKey textAttributesKey = TextAttributesKey.createTextAttributesKey("externalName", textattributes);
//            annotation.setTextAttributes(textAttributesKey);
    }
}
