package edu.cmu.cs.lti.gigascript.io;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 3/12/14
 * Time: 2:17 PM
 */
public class GigaStorageReader {


    public static void main(String[] argv) throws IOException {


        File bz2File = new File("/Users/hector/Downloads/01.tar.bz2");

        FileSystemManager fsManager = VFS.getManager();
        FileObject tuplesFolderObj = fsManager.resolveFile("tbz2:" + bz2File.toURI().toURL() + "!" + "storage/txt/01/tuples");

        for (FileObject tupleFile : tuplesFolderObj.getChildren()) {
            System.out.println(tupleFile.getName().getBaseName());
        }

    }


}
