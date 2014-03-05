package edu.cmu.cs.lti.gigascript.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.AgigaToken;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

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


    public static void writeMap(Writer writer , TIntIntHashMap map, String valueKeySep, String interSep) throws IOException {
        TIntIntIterator iter = map.iterator();

        String sep = "";
        while (iter.hasNext()){
            iter.advance();
            writer.write(sep);
            writer.write(iter.key()+valueKeySep+iter.value());
            sep = interSep;
        }
    }

    public static void writeList(Writer writer , TIntArrayList list, String interSep) throws IOException {
        TIntIterator iter = list.iterator();

        String sep = "";
        while (iter.hasNext()){
            writer.write(sep);
            writer.write(iter.next());
            sep = interSep;
        }

    }
}