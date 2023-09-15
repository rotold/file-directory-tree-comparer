package com.example.filetreecomparer;

import java.util.Comparator;

class FileInfCompare implements Comparator <FileInfo> {

    /**
     * compares FileInfo o1 to o2 by first sorting by shortPath, then by whether it is a directory
     * -1 means o1 is before o2 and +1 means it is after, but also uses String.compareTo() in some cases
     */

    @Override
    public int compare(FileInfo o1, FileInfo o2) {


        if (o1.getShortPath().equals(o2.getShortPath()))
        // no need to sort by shortPath
        {
            if (o1.isDirectory() == o2.isDirectory())
            // the Directory state is the same so already "sorted"
            {
                return 0;
            }
            else {
                // the shortPath does not need sorting but the directory status does
                if (o1.isDirectory())
                    // o2 must not be a directory
                    return 1;
                else {
                    // o1 is not a directory and o2 must be
                    return -1;
                }

            }
        }
        else {
            // the shortPaths are different and most sort them
            return o1.getShortPath().compareTo(o2.getShortPath());
        }
    }
}
