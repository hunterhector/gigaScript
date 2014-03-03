package edu.cmu.cs.lti.gigascript.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.AgigaToken;

import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/12/14
 * Time: 1:17 AM
 */
public class IOUtils {

    public static void printSentence(AgigaSentence sent, PrintStream dout) {
        for (AgigaToken token : sent.getTokens()) {
            dout.print(token.getWord());
            dout.print(" ");
        }
        dout.println();
    }
}