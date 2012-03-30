package org.jetbrains.emacs4ij;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.emacs4ij.jelisp.BackwardMultilineParser;
import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.elisp.*;
import org.jetbrains.emacs4ij.jelisp.exception.EndOfFileException;
import org.jetbrains.emacs4ij.jelisp.exception.MarkerPointsNowhereException;
import org.jetbrains.emacs4ij.jelisp.exception.VoidVariableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 8/4/11
 * Time: 11:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class IdeaBuffer implements LispBuffer {
    private static Project ourProject;
    protected String myName;    
    protected Environment myEnvironment;
    private HashMap<String, LispSymbol> myLocalVariables = new HashMap<>();
    protected List<LispMarker> myMarkers = new ArrayList<>();
    private LispMarker myMark = new LispMarker();
    protected EditorManager myEditorManager = new EditorManager();

    protected IdeaBuffer() {}

    public IdeaBuffer(Environment environment, String name, String path, Editor editor) {
        myEnvironment = environment;
        myName = name;
        setEditor(editor);
        myEnvironment.defineBuffer(this);
        setLocalVariable("default-directory", new LispString(path));
    }
    
//    private void addLocalVar (String name, LispObject value) {
//        myLocalVariables.put(name, new LispSymbol(name, value, true));
//    }

    public static void setProject(Project project) {
        ourProject = project;
    }

    private void setLocalVariable (String name, LispObject value) {
        LispSymbol var = myLocalVariables.get(name);
        if (var == null)
            throw new VoidVariableException(name);
        var.setValue(value);
    }

    @Override
    public LispObject getLocalVariableValue (String name) {
        LispSymbol localVar = getLocalVariable(name);
        return localVar.getValue();
    }

    @Override
    public LispSymbol getLocalVariable(String name) {
        LispSymbol var = myLocalVariables.get(name);
        if (var == null)
            throw new VoidVariableException(name);
        return var;
    }

//    @Override
//    public void defineLocalVariable(LispSymbol variable) {
//        myLocalVariables.put(variable.getName(), new LispSymbol(variable));
//    }

    @Override
    public void defineLocalVariable(LispSymbol variable, boolean noValue) {
        myLocalVariables.put(variable.getName(), new LispSymbol(variable, null));
    }

    protected Document getDocument() {
        return getEditor().getDocument();
    }

    @Override
    public LispObject evaluateLastForm() {
        String[] code = getDocument().getText().split("\n");
        int line = getEditor().getCaretModel().getVisualPosition().getLine();
        int column = getEditor().getCaretModel().getVisualPosition().getColumn() - 1;
        if (code.length-1 < line) {
            line = code.length - 1;
            if (line < 0)
                throw new EndOfFileException();
            column = code[line].length() - 1;
        }
        if (code[line].length() - 1 < column) {
            column = code[line].length() - 1;
        }
        BackwardMultilineParser parser = new BackwardMultilineParser(code);
        LispObject parsed = parser.parse(line, column);
        return parsed.evaluate(myEnvironment);
    }

    @Override
    public Editor getEditor() {
        return myEditorManager.getActiveEditor().getEditor();
    }

    @Override
    public void setEditor(Editor editor) {
        myEditorManager.setActiveEditor(editor);
    }

    public String toString() {
        return "#<buffer " + myName + ">";
    }

    @Override
    public LispObject evaluate(Environment environment) {
        return this;
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public int getSize() {
        return myEditorManager.getActiveEditor().getSize();
    }

    @Override
    public int point() {
        return myEditorManager.getActiveEditor().point();
    }

    @Override
    public void setPoint(int position) {
        myEditorManager.getActiveEditor().setPoint(position);
    }

    @Override
    public int pointMin() {
        return myEditorManager.getActiveEditor().pointMin();
    }

    @Override
    public int pointMax() {
        return myEditorManager.getActiveEditor().pointMax();
    }

    @Override
    public String gotoChar (int position) {
        return myEditorManager.getActiveEditor().gotoChar(position);
    }

    @Override
    public String forwardChar (int shift) {
        return gotoChar(point() + shift);
    }

    protected void write (final String text) {
        myEditorManager.getActiveEditor().write(text);
    }
    
    private void insertAt (final int position, final String insertion) {
        myEditorManager.getActiveEditor().insertAt(position, insertion);
    }

    @Override
    public void setActive() {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(ourProject);
        VirtualFile[] openedFiles = fileEditorManager.getOpenFiles();
        for (VirtualFile file: openedFiles) {
            if (file.getName().equals(myName)) {
                fileEditorManager.openTextEditor(new OpenFileDescriptor(ourProject, file), true);
            }
        }
    }

    @Override
    public void kill () {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(ourProject);
        VirtualFile[] openedFiles = fileEditorManager.getOpenFiles();
        for (VirtualFile file: openedFiles) {
            if (file.getName().equals(myName)) {
                fileEditorManager.closeFile(file);
            }
        }
    }

    @Override
    public void closeHeader () {
        myEditorManager.getActiveEditor().closeHeader();
    }

    //--------------- mark --------------------------------
    @Override
    public void addMarker (LispMarker marker) {
        if (!myMarkers.contains(marker) && myMark != marker)
            myMarkers.add(marker);
    }

    @Override
    public void removeMarker (LispMarker marker) {
        myMarkers.remove(marker);
    }

    @Override
    public boolean hasMarkersAt (int position) {
        for (LispMarker marker: myMarkers) {
            if (marker.isSet() && marker.getPosition() == position)
                return true;
        }
        return myMark.isSet() && myMark.getPosition() == position;
    }

    @Override
    public LispMarker getMark() {
        return myMark;
    }

    @Override
    public void setMark(LispMarker mark) {
        myMark = mark;
        if (myMarkers.contains(mark))
            myMarkers.remove(mark);
    }

    @Override
    public void insert(String insertion, int where) {
        if (StringUtil.isEmpty(insertion))
            return;
        insertAt(where - 1, insertion);
        updateMarkersPositions(where, insertion.length(), true);
        gotoChar(where + insertion.length());
    }
    
    private void updateMarkersPositions (int point, int shift, boolean moveAnyway) {
        for (LispMarker marker: myMarkers) {
            if (!marker.isSet())
                throw new Attention();
            marker.move(shift, point, moveAnyway);
        }
        if (myMark.isSet())
            myMark.move(shift, point, moveAnyway);
    }

    @Override
    public void insert(LispObject insertion, @Nullable LispMarker where) {
        String ins = insertion.toString();
        if (insertion instanceof LispString) {
            LispObject kbd = kbd(myEnvironment, (LispString) insertion);
            ins = kbd instanceof LispString ? ((LispString) kbd).getData() : kbd.toString();
        }
        if (where != null && !where.isSet())
            throw new MarkerPointsNowhereException();
        int pos = where == null ? point() : where.getPosition();
        insert(ins, pos);
    }

    @Override
    public void insert(LispObject insertion) {
        insert(insertion, null);
    }

    @Override
    public void addEditor(Editor editor) {
        myEditorManager.add(editor);
    }

    @Override
    public void switchToEditor(Editor editor) {
        myEditorManager.switchToEditor(editor);
    }

    @Override
    public void insert(String insertion) {
        insert(insertion, point());
    }

    private LispObject kbd (Environment environment, LispString keys) {
        return LispList.list(new LispSymbol("kbd"), keys).evaluate(environment);
    }
}
