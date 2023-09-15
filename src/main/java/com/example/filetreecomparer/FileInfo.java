package com.example.filetreecomparer;

class FileInfo {
    /**
     * Holds information about a file
     * in the comments, pathA will be called the root path of one of the root paths the user chooses to compare.
     * The other comparison directory will be called pathB
     */
    private String shortPath; //path with comparison dir stripped off
    //path A will mean fileDB.pathA and B will mean fileDB.pathB
    private char path; //root directory A= pathA, B=pathB
    private long crcValue; //crc 32 of the file; -1 means not calculated; -2 means is a directory

    /* bit 0:
        1 if the file path A has different CRC32 than the corresponding file in path B and vise versa
        This bit is also assigned to all parent directories of the file. 0 means no difference in CRC32
       bit 1:
        1 if the file path B is missing. This disposition code is applied to the file in pathA because it is
        impossible to associate conveniently to any file in path B. Vise versa for a file missing in pathA.
        This bit is also assigned to all parent directories of the file, and is also assigned to the parent
        directories of a directory having dispocode bit 3.
       bit 2:
        unused
       bit 3:
        1 if the directory in path B is missing. This disposition code is applied to the directory in pathA
        because it is impossible to associate conveniently to any directory in path B.
        Vise versa for a directory missing in pathA. This bit does not pertain to files.
        bit 4:
         notEval. The value is 1 if the file is not dispositioned yet.
        bit 5:
         This is for future enhancements and is analogous to bit 1 except
         a bit by bit comparison of the 2 files is used instead of CRC32

         If diso=0 then the file was evaluated and no differences were detected
*/
    private int dispo;

    private boolean directory; //whether it is a directory or a file

    FileInfo(String shortPath, char path, long crcValue, int dispo, boolean directory) {
        this.shortPath = shortPath;
        this.path = path;
        this.crcValue = crcValue;
        this.dispo = dispo;
        this.directory = directory;
    }

    void setShortPath(String shortPath) {
        this.shortPath = shortPath;
    }

    void setPath(char path) {
        this.path = path;
    }

    void setCrcValue(long crcValue) {
        this.crcValue = crcValue;
    }

    void setDispo(int dispo) {
        this.dispo = dispo;
    }

    void setDirectory(boolean directory) {
        this.directory = directory;
    }

    String getShortPath() {
        return shortPath;
    }

    char getPath() {
        return path;
    }

    long getCrcValue() {
        return crcValue;
    }

    int getDispo() {
        return dispo;
    }

    boolean isDirectory() {
        return directory;
    }

    public String toString(){
        return path + ":" + shortPath + " - dir?: " + directory + "\n"
                + "crc: "+ crcValue + " - dispo: " + dispo;
    }
    /**
     * determines if the items from different shortpaths are the same as indicated by crc
     * a bit by bit comparison might prove that they are different
     */
    boolean equals(FileInfo x) {

        return ((shortPath.equals(x.shortPath))&&(crcValue==x.crcValue)&& directory == x.directory);
    }
    /**
     * determines if items from different short paths only differ by crc
     */

    boolean isDiffer(FileInfo x) {

        return ((shortPath.equals(x.shortPath))&&!(crcValue==x.crcValue)&& directory==x.directory);
    }
}
