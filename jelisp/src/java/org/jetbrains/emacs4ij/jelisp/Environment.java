package org.jetbrains.emacs4ij.jelisp;

import org.jetbrains.emacs4ij.jelisp.elisp.LispBuiltinFunction;
import org.jetbrains.emacs4ij.jelisp.elisp.LispObject;
import org.jetbrains.emacs4ij.jelisp.elisp.LispSpecialForm;
import org.jetbrains.emacs4ij.jelisp.elisp.LispSymbol;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 7/11/11
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Environment {

    public static final Environment ourGlobal = new Environment(null);
    private static int k = 0;

    private final HashMap<LispSymbol, LispObject> mySpecialForms = new HashMap<LispSymbol, LispObject>();
    private HashMap<LispSymbol, LispObject> myVariables = new HashMap<LispSymbol, LispObject>();
    private HashMap<LispSymbol, LispObject> myBuiltinVariables = new HashMap<LispSymbol, LispObject>();
    private HashMap<LispSymbol, LispObject> myFunctions = new HashMap<LispSymbol, LispObject>();
    private HashMap<LispSymbol, LispObject> myBuiltinFunctions = new HashMap<LispSymbol, LispObject>();

    private Environment myOuterEnv;

    public Environment (Environment outerEnv) {
        myOuterEnv = outerEnv;

        if (outerEnv == null) {
            setGlobal();
            k++;
            if (k>1)
                System.out.println("lol");
        }
    }

    private void setGlobal() {
        mySpecialForms.put(new LispSymbol("quote"), new LispSpecialForm("quote"));
        mySpecialForms.put(new LispSymbol("defun"), new LispSpecialForm("defun"));
        mySpecialForms.put(new LispSymbol("interactive"), new LispSpecialForm("interactive"));

        myBuiltinFunctions.put(new LispSymbol("+"), new LispBuiltinFunction("+"));
        myBuiltinFunctions.put(new LispSymbol("*"), new LispBuiltinFunction("*"));
        myBuiltinFunctions.put(new LispSymbol("set"), new LispBuiltinFunction("set"));

    }

    public LispObject find(String name) {

        LispSymbol lsName = new LispSymbol(name);

        LispObject lispObject = mySpecialForms.get(lsName);
        if (lispObject != null)
            return lispObject;

        lispObject = myBuiltinVariables.get(lsName);
        if (lispObject != null)
            return lispObject;

        lispObject = myVariables.get(lsName);
        if (lispObject != null)
            return lispObject;

        lispObject = myBuiltinFunctions.get(lsName);
        if (lispObject != null)
            return lispObject;

        lispObject = myFunctions.get(lsName);
        if (lispObject != null)
            return lispObject;

        if (myOuterEnv != null) {
            return myOuterEnv.find(name);
        }

        throw new RuntimeException("unknown symbol " + name);
    }


    public void setVariable(LispObject name, LispObject value) {
        myVariables.put((LispSymbol)name, value);
    }

    public LispObject getVariable (String name) {
        return getVariable(new LispSymbol(name));
    }

    public LispObject getVariable(LispSymbol name) {
        return myVariables.get(name);
    }

    public void defineFunction (LispObject name, LispObject value) {
        myFunctions.put((LispSymbol)name, value);
    }

}
