package org.jetbrains.emacs4ij.jelisp.elisp;

import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.exception.WrongNumberOfArgumentsException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 7/13/11
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class LispBuiltinFunction extends LispFunction {

    public LispBuiltinFunction(String myName) {
        this.myName = new LispSymbol(myName);
    }

    public LispObject execute (List<LispObject> args, Environment environment) {
        if (myName.is("+")) {
            int ans = 0;

            for (LispObject lispObject: args) {
                ans += ((LispInteger)lispObject).getMyData();
            }

            return new LispInteger(ans);
        }
        if (myName.is("*")) {
            int ans = 1;

            for (LispObject lispObject: args) {
                ans *= ((LispInteger)lispObject).getMyData();
            }

            return new LispInteger(ans);
        }

        if (myName.is("set")) {
            if (args.size() != 2)
                throw new WrongNumberOfArgumentsException();
            environment.setVariable(args.get(0), args.get(1));
            return args.get(1);
        }
        throw new RuntimeException("unknown builtin function " + myName);
    }

    @Override
    public LispString toLispString() {
        return new LispString(myName.getName());
    }
}
