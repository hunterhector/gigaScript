package edu.cmu.cs.lti.gigascript.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/29/14
 * Time: 8:11 PM
 */
public class SplittedFileLinesIterator implements Iterator<String> {

    private LineIterator lineIter;
    private Iterator<File> fileIter;
    private boolean emptyDirectory;

    /**
     * Read splitted files from a directory as if they are one single file
     */
    public SplittedFileLinesIterator(File hostingDir, String basename) throws IOException {
        if (!hostingDir.exists() || !hostingDir.isDirectory()) {
            throw new IllegalArgumentException("Cannot find the provided directory: " + hostingDir.getCanonicalPath());
        }

        File[] files = hostingDir.listFiles();
        List<File> filteredFiles = new ArrayList<File>();

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });


        for (File file : files) {
            String name = file.getName();
            if (name.startsWith(basename)) {
                filteredFiles.add(file);
            }
            System.out.println(file.getName());
        }

        fileIter = filteredFiles.iterator();

        if (fileIter.hasNext()) {
            lineIter = FileUtils.lineIterator(fileIter.next());
        } else {
            emptyDirectory = true;
        }
    }


    @Override
    public boolean hasNext() {
        return !emptyDirectory && (lineIter.hasNext() || fileIter.hasNext());
    }

    @Override
    public String next() {
        if (lineIter.hasNext()) {
            return lineIter.next();
        } else {
            //so if you don't check for hasNext I probably will complain here
            File nextFile = fileIter.next();
            try {
                lineIter = FileUtils.lineIterator(nextFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lineIter.next();
        }
    }

    @Override
    public void remove() {
        //not allowed dude!
    }
}
