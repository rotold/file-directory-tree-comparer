package com.example.filetreecomparer;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Predicate;

/**
 * prepares an ArrayList and dispositions each file in the array list
 * The disposition codes are given in the comments for FileInfo.dispo
 */
class FileDB {

    /**
     * statsn1 the number of files when first counted
     * statsn2 the number of files that were added to the file list
     * finishTime start time of analysis - prob.to be removed because Controller has it
     * startTime end time of analysis - prob. to be removed because Controller has it
     */
    private int statsn1 =0;
    private int statsn2 =0;
    private LocalDateTime finishTime;
    private LocalDateTime startTime;
    private String pathA, pathB = null; // full paths of the root
    private String analName = "Anal1"; // a name for the analysis project
    /**
     * pathANickName a user chosen name as a nickname for the pathA
     * and similarly for pathB
     */
    private String pathANickName = "pathA";
    private String pathBNickName = "pathB";
    private boolean subfolders = true; // whether to compare subfolders
    // report choices: bits 1:show differing items; 2 show not differing items; 3 show directories; 4 show files
    private int choiceFlag = 13;
    /**
     * sameName whether a file can have a same name as a directory which is in the same parent directory as the file
     * systemFiles whether the user decides to include system files in the analysis
     */

    private boolean sameName = false;
    private boolean systemFiles = false;
    /**
     * fileList holds all the files, directories and their disposition codes
     * dirInd holds the indexes in fileList of the directories mapped with path and
     *  shortpath so that backDispo does not have to iterate through fileList to find the directories.
     *  The key is path appended to short path
     *  aText holds the text for the report on pathA; similar parameter for pathB
     *  aExtraText holds the extra info text for pathB; similar parameter for path
     *
     */
    private ArrayList<FileInfo> fileList;
    private HashMap<String, Integer> dirInd;
    private ArrayList<String> aText;
    private ArrayList<String> aExtraText;
    private ArrayList<String> bText;
    private ArrayList<String> bExtraText;

    ArrayList<String> getaText() {
        return aText;
    }

    ArrayList<String> getaExtraText() {
        return aExtraText;
    }

    ArrayList<String> getbText() {
        return bText;
    }

    ArrayList<String> getbExtraText() {
        return bExtraText;
    }

    boolean isSameName() {
        return sameName;
    }

    void setSameName(boolean sameName) {
        this.sameName = sameName;
    }


    boolean isSubfolders() {
        return subfolders;
    }

    void setSubfolders(boolean subfolders) {
        this.subfolders = subfolders;
    }

    String getPathANickName() {
        return pathANickName;
    }

    void setPathANickName(String pathANickName) {
        this.pathANickName = pathANickName;
    }

    String getPathBNickName() {
        return pathBNickName;
    }

    void setPathBNickName(String pathBNickName) {
        this.pathBNickName = pathBNickName;
    }

    boolean isSystemFiles() {
        return systemFiles;
    }

    void setSystemFiles(boolean systemFiles) {
        this.systemFiles = systemFiles;
    }

    FileDB() {

    }

    void setPathA(String pathA) {
        this.pathA = pathA;
    }

    void setPathB(String pathB) {
        this.pathB = pathB;
    }

    void setAnalName(String analName) {
        this.analName = analName;
    }

    String getPathA() {
        return pathA;
    }

    String getPathB() {
        return pathB;
    }

    String getAnalName() {
        return analName;
    }

    int getChoiceFlag() {
        return choiceFlag;
    }

    void setChoiceFlag(int choiceFlag) {
        this.choiceFlag = choiceFlag;
    }

    /**
     * this file count is correct only if user chooses to include system files
     */

    private int recurseCountFiles(String curPath) {
        // https://stackoverflow.com/questions/38706689/how-to-check-if-a-file-directory-is-a-protected-os-file

        // count files

        FileCounter<Path> count1 = new FileCounter<>();
        try {
            Files.walkFileTree(Paths.get(curPath), Collections.emptySet(),
                    subfolders ? Integer.MAX_VALUE: 1,count1 );
        } catch (IOException e) {
            // Future Improvement: Instead of aborting make a suitable dispocode
            e.printStackTrace();
        }

        System.out.println("The total number of files is: " + count1.getN());
        return count1.getN();


    }
    /**
     * uses  https://docs.oracle.com/javase/tutorial/essential/io/walk.html
     * and http://www.javapractices.com/topic/TopicAction.do?Id=68
     * counts files considering whether system files and subdirectory chosen by user
     */

    private class FileCounter<T> extends SimpleFileVisitor<T> {

        /**
         * n counts the files. n includes the initial directory itself so includes 1 extra file!
         */
        private int n;

        private final Predicate <Object> sysFile = (f) -> {
            try {
                return Files.readAttributes((Path.of(f.toString()))
                        ,DosFileAttributes.class).isSystem();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        };

        private FileCounter() {
            n=0;
        }

        /**
         *
         * @return returns n-1 which is the number of files excluding the pathA or pathB file that is added
         * by this class to n
         */
        int getN() {

            return n-1;
        }

        @Override
        public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
            // This method was verified on C:\test
            n=n+1;
            System.out.println(dir);
            return super.preVisitDirectory(dir, attrs);
        }

        @Override
        public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
            // will increment n and println the path
            if (!((!systemFiles)&&(sysFile.test(file)))){
                /*
                only case for not doing something is if user chooses no system file and it is a system file
                 */
                n=n+1;
                System.out.println(file);
            }

            return super.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
            return super.visitFileFailed(file, exc);
        }

        @Override
        public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
            return super.postVisitDirectory(dir, exc);
        }
    }

     class intializeFileList extends Task<Void> {

        int t;
        @Override
        protected void cancelled() {
            updateProgress(0.00,100);
        }

        @Override
        protected Void call() {

            startTime=LocalDateTime.now();
            System.out.println("\tStarting task 1 at:" + startTime +"Initialize File List ");
            if (isCancelled())
                return null;
            t = recurseCountFiles(pathA) + recurseCountFiles(pathB);
            System.out.println("total files are: " + t);
            statsn1 = t;
            // pehaps add a 10% buffer - make t = t*1.1

            try {
                FileDB.this.fileList = new ArrayList<>(t);
                System.out.println("fileList of size " + t + " was created");
            } catch (Exception e) {
                // future improvement: Handle in an appropriate way instead of aborting
                System.out.println("fileList array size was to be: " + t);
                e.printStackTrace();
            }
            if (isCancelled())
                return null;

            // Assume a dirInd of half the size of the total number of files is sufficient initial capacity
            try {
                FileDB.this.dirInd = new HashMap<>(t / 2);
                System.out.println("HashMap of size " + (t / 2) + " was created.");
            } catch (Exception e) {
                System.out.println("HahMap creation failure of size " + (t / 2));
                e.printStackTrace();
            }

            // assume one tenth file size is sufficient for these
            FileDB.this.aText = new ArrayList<>((statsn1) / 10);
            FileDB.this.bText = new ArrayList<>((statsn1) / 10);
            FileDB.this.aExtraText = new ArrayList<>((statsn1) / 10);
            FileDB.this.bExtraText = new ArrayList<>((statsn1) / 10);

            return null;
        }
    }

    class populateFileList extends Task<Void> {

        /**
         * creates a FileInfo for each file and adds it to the fileList
         */

        // reference:
        // https://stackoverflow.com/questions/38706689/how-to-check-if-a-file-directory-is-a-protected-os-file
        // count files
        // set number of files processed to zero
        @Override
        protected void cancelled() {

            updateProgress(0.00,100);
        }

        @Override
        protected Void call() {

            System.out.println("\tStarting task 2: populate file list");
            if (isCancelled())
                return null;
            FileInfoAdder<Path> adder1 = new FileInfoAdder<>('A');
            FileInfoAdder<Path> adder2 = new FileInfoAdder<>('B');
            try {

                if(isCancelled())
                    return null;
                // I think Collections.emptyset() causes there to be no FileVistOption
                Files.walkFileTree(Paths.get(pathA), Collections.emptySet(),
                        subfolders ? Integer.MAX_VALUE : 1, adder1);
                int a = adder1.getN();
                adder2.setA(a);
                if (isCancelled())
                    return null;
                Files.walkFileTree(Paths.get(pathB), Collections.emptySet(),
                        subfolders ? Integer.MAX_VALUE : 1, adder2);
            } catch (IOException e) {
                // future improvement: handle with appropriate dispocode instead of aborting
                e.printStackTrace();
            }

            int x = adder1.getN() + adder2.getN();

            /*
              statsna the number of files in path A
              statsnb the number of files in path B; should be n1=n2=statsna + statsnb
             */
            int statsna = adder1.getN();
            int statsnb = adder2.getN();
            System.out.println("The number of a files: " + statsna + " and the number of b files: " + statsnb);
            System.out.println("The total number of added files is: " + x);
            statsn2 = x;
            if (statsn1 == statsn2) {
                System.out.println("number of files unchanged: " + statsn1);
            } else {
                System.out.println("The number of files changed. Before: " + statsn1
                        + " After: " + statsn2);
            }
            return null;
        }

        /**
         * uses  https://docs.oracle.com/javase/tutorial/essential/io/walk.html
         * and http://www.javapractices.com/topic/TopicAction.do?Id=68
         * counts files considering whether system files and subdirectory chosen by user
         * adds appropriate data to FileInfo
         */

        class FileInfoAdder<T> extends SimpleFileVisitor<T> {


             /* n counts the files. n includes the initial directory itself!
             * a is the total files from the previous count to calculate total progress
             * pathLetter corresponds to path of FileInfo
             */

            private int n, a;
            private final char pathLetter;
            private final GetCRC32 crcAdd = new GetCRC32();

            private final Predicate <Object> sysFile = (f) -> {
                try {
                return Files.readAttributes((Path.of(f.toString()))
                        ,DosFileAttributes.class).isSystem();
                }
                catch (IOException e) {
                    // future improvement: Handle appropriately without aborting
                    e.printStackTrace();
                    return false;
                }
            };

            // the length of pathA or pathB to create the FileInfo.shortPath
            int l;
            // path of current file being processed
            String curFile;
            // value of directory for the current file
            boolean director;
            // holds the crc value
            long crcval = -1;

            FileInfoAdder(char pathLetter) {
                n=0;
                a=0;
                this.pathLetter = pathLetter;
                l = (pathLetter == 'A') ? pathA.length() : pathB.length();
            }


            void setA(int a) {
                this.a=a;
            }

            int getN() {
                return n;
            }


            @Override
            public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
                if (!((!systemFiles)&&(sysFile.test(dir)))) {
                    /*
                    only case for not doing something is if user chooses no system file and it is a system file
                     */
                    System.out.println(dir);
                    curFile = dir.toString();
                    // create short path
                    curFile = curFile.substring(l);
                    // if short path is null then is pathA or pathB so do not process
                    if(curFile.length()==0){
                        return super.preVisitDirectory(dir, attrs);
                        // which is return CONTINUE
                    }

                    if (isCancelled()) {
                        updateProgress(0.00,100);
                        return FileVisitResult.TERMINATE;
                    }
                    else if(statsn1!=0) {
                        updateProgress(n+a, statsn1);
                    }

                    n=n+1;
                    director = attrs.isDirectory();
                    crcval = director ? -2 : crcAdd.calc(dir.toString());
                    System.out.println("Adding " + pathLetter + " file " + n + " with Short path: " + curFile);
                    FileInfo cur = new FileInfo(curFile, pathLetter, crcval, 1 << 4, director);
                    System.out.println("Added with directory: " + director + " and crc value: "+crcval
                            + "dispo: " + (1<<4));
                    try {
                        fileList.add(cur);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
                // will increment n and println the path
                if (!((!systemFiles)&&(sysFile.test(file)))){
                    /*
                    only case for not doing something is if user chooses no system file and it is a system file
                     */
                    n=n+1;

                    if (isCancelled()) {
                        updateProgress(0.00,100);
                        return FileVisitResult.TERMINATE;
                    }
                    else if(statsn1!=0) {
                        updateProgress(n+a, statsn1);
                    }

                    System.out.println(file);
                    curFile = file.toString();
                    curFile = curFile.substring(l);
                    director = attrs.isDirectory();
                    crcval = director ? -2 : crcAdd.calc(file.toString());
                    System.out.println("Adding " +pathLetter +  " file " +n + " with Short path: "+curFile);
                    FileInfo cur = new FileInfo(curFile,pathLetter,crcval,1 << 4,director);
                    System.out.println("Added with directory: " + director + " and crc value: "+crcval
                            + "disp: " + (1<< 4));
                    try{
                        fileList.add(cur);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return super.visitFile(file,attrs);
            }
            @Override
            public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
                if (isCancelled()){
                    return FileVisitResult.TERMINATE;
                }
                return super.postVisitDirectory(dir, exc);
            }
        }

    }

    /**
     * Sorts fileList and updates status bar to show progress
     */
    class sortFileList extends Task <Void> {
        @Override
        protected void cancelled() {
            updateProgress(0.00,100);
        }
        @Override
        protected Void call() {
            System.out.println("/tStarting task 3: sort file list");
            if(isCancelled())
                return null;
            fileList.sort(new FileInfCompare());
            System.out.println("Sorting Complete");

            int n = 0;

            System.out.println("Showing sort results:");
            for (FileInfo fiInfo : fileList) {
                if (isCancelled())
                    break;
                n++;
                if(statsn1!=0) {
                    updateProgress(n, statsn1);
                }
                System.out.println("file " + n + ":");
                System.out.println(fiInfo);
            }

            return null;
        }

    }

    /**
     * Creates a Hash of the only directory paths (no file paths) as a key and the corresponding index in
     * so that the appropriate dispocode can be added to the parent directories if a file in its subtree
     * is found to be different. This is an added feature and is not required for knowing the disposition
     * of each file.
     */

    class populateDirInd extends Task<Void> {

        @Override
        protected void cancelled() {
            updateProgress(0.00,100);
        }
        @Override
        protected Void call() {
            /*
             * populated dirInd
             * @param n is the current index of the fileList
             */
            System.out.println("/tStarting task 4 Populate Dir Ind");
            int n = 0;
            if (isCancelled())
                return null;
            for (FileInfo a : fileList) {
                if (isCancelled())
                    break;
                if (a.isDirectory()) {
                    String dirKey = a.getPath() + a.getShortPath();
                    try {
                        dirInd.put(dirKey, n);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                n = n + 1;
            }
            return null;
        }


    }

    /**
     * sets the dispo codes in the FileInfo's of fileList. This method assumes it is possible to have
     * 2 files with the same name, such as one being a file and the other a directory. This is not
     * used for Windows. If used for UNIX type systems then should be updated for slash vs backslash
     * for hierarchical path divisions
     */

    class dispoFileList extends Task<Void> {


        /**
         * N the number of files to be analysed
         * n the index of fileList corresponding to the current "1st" item being analysed
         * more this parameter is true while there is more to analyse
         */

        int n =0;
        int N=0;

        @Override
        protected void cancelled() {
            updateProgress(0.00,100);
        }

        @Override
        protected Void call() {
            N = fileList.size();
            System.out.println("\tStarting task 5 dispo file list");
            if (N != statsn2)
                System.out.println("Warning fileList is of size " + N + "but number of files is " + statsn2);

            do {

                // numLeft is the number of items left to compare. 1 is a special case and 2 is a special case of there
                // are differences

                if (isCancelled())
                    break;
                int numLeft = N - n - 1;
                //S
                if (statsn1!=0) {
                    updateProgress(n, statsn1);
                }

                if (numLeft == 0) {
                    // Case #1: this is the last item and missing
                    System.out.println("comparing item " + (n + 1) + " of " + N);
                    FileInfo m = fileList.get(n);
                    System.out.println("This is the last item and is missing: " + m.getShortPath());
                    if (m.isDirectory()) {
                        // need to set and clear flags and back dispo
                        int mf = m.getDispo();
                        // set directory itself is missing flag
                        mf = mf | 8;
                        // clear not dispo'd flag
                        mf = mf & ~16;
                        // apply changes
                        m.setDispo(mf);
                        System.out.println(m.getShortPath() + ": dispoflag is now: " + fileList.get(n).getDispo());

                        backDispo(m.getShortPath(), m.getPath(), 2);
                        //more = false;
                        // no more files to dispo but update anyway to terminate do loop
                        n = n + 1;
                    } else {
                        // item is a file
                        m.setDispo(2);
                        System.out.println(m.getShortPath() + ": dispo is now: " + fileList.get(n).getDispo());
                        backDispo(m.getShortPath(), m.getPath(), 2);
                        //more = false;
                        //no more files to dispo but update anyway to terminate do loop
                        n = n + 1;
                    }

                } else {
                    // else numLeft is 1 or more. if the next 2 items are different then we might have to see if there is
                    // a 3rd item to dispo the first 2 items. The rest of the cases will be handled here.


                    // get first and second items a and b respectively
                    System.out.println("comparing items " + (n + 1) + " and " + (n + 2) + " of " + N);
                    FileInfo a = fileList.get(n);
                    FileInfo b = fileList.get(n + 1);

                    if (a.equals(b)) {
                        // Case #2: items are the same by crc so dispocode is 0
                        // a bit by bit comparison might prove they are different
                        a.setDispo(0);
                        b.setDispo(0);
                        // 2 items are dispo'd thus advance n by 2.
                        n = n + 2;
                    } else {
                        // starting case 3 (there are differences)

                        if (a.isDiffer(b)) {
                            // everything is the same but the crc codes are different
                            System.out.println("Items: " + a.getShortPath() + " are different by crc " + a.getCrcValue()
                                    + " vs " + b.getCrcValue());
                            // set dispocodes to zero
                            a.setDispo(1);
                            b.setDispo(1);
                            System.out.println("item " + (n + 1) + "dispo set to differ crc : " + fileList.get(n + 1).getDispo());
                            // backdispo a and b
                            backDispo(a.getShortPath(), a.getPath(), 1);
                            backDispo(b.getShortPath(), b.getPath(), 1);
                            // 2 items were dispo'd thus advance n by 2.
                            n = n + 2;

                        }

                        // Begin unnecessary and untested code for windows (dir and file with same name) >>>------>
                        if (a.getShortPath().equals(b.getShortPath()) && a.isDirectory() != b.isDirectory()) {
                            // gate is true if item 2 is not the last and item 3 is different from item 1
                            System.out.println("case of file and dir with same name. if this is Windows" +
                                    "there is probably an error");
                            boolean gate = false;
                            if (n + 2 <= N - 1) {
                                // there is a 3rd element
                                FileInfo c = fileList.get(n + 2);
                                String sp3 = c.getShortPath();
                                String sp1 = a.getShortPath();
                                if (!sp1.equals(sp3))
                                    gate = true;
                            }

                            if (gate || n + 1 + 1 == N) {
                            /* Item 1 is different from item 3 or item 2 is the last item. In this case both items
                            1 and 2 are missing
                             */
                                if (a.isDirectory()) {
                                    // item 1 is a dir and item 2 is a file
                                    // set item1 bit 4 and clear undispo'd flag
                                    int ad = a.getDispo();
                                    ad = ad | 8;
                                    ad = ad & ~16;
                                    a.setDispo(ad);
                                    backDispo(a.getShortPath(), a.getPath(), 2);

                                    b.setDispo(2);
                                    backDispo(b.getShortPath(), b.getPath(), 2);

                                    // both items were dispo'd
                                    n = n + 2;
                                } else {
                                    // item 2 is a dir and item 2 is a file
                                    a.setDispo(2);
                                    backDispo(a.getShortPath(), a.getPath(), 2);

                                    int bd = b.getDispo();
                                    bd = bd | 8;
                                    bd = bd & ~16;
                                    b.setDispo(bd);
                                    backDispo(b.getShortPath(), b.getPath(), 2);

                                    // both items were dispo'd
                                    n = n + 2;
                                }

                            } else {
                            /* only one of the items 1 or 2 is missing and item 3 is the matching item
                            by name. Analysis shows that item 1 is missing and item2
                            will be the same type (directory or file) as item 3, and type is the one that
                            comes second in the sort of the array. type was not checked, so this
                            will now be done explicitly. Only the dispo of item 1 is determined
                             */

                                if (a.isDirectory()) {
                                    // missing item 1 is a dir
                                    int ad = a.getDispo();
                                    ad = ad | 8;
                                    ad = ad & ~16;
                                    a.setDispo(ad);
                                    backDispo(a.getShortPath(), a.getPath(), 2);
                                    // one item was dispo'd
                                    n = n + 1;
                                } else {
                                    // missing item 1 is a file
                                    a.setDispo(2);
                                    backDispo(a.getShortPath(), a.getPath(), 2);
                                    // one item was dispo'd
                                    n = n + 1;

                                }
                            }
                        }

                        // <-------<<< End unnecessary and untested code for windows (dir and file with same name)
                        // now continuing case that item1 and item2 are different

                        if (!a.getShortPath().equals(b.getShortPath())) {
                        /*
                        case of different files (case 4). A diligent but not exhaustive analysis indicates that
                        if item i != item 3 then item i is missing, and that if item2 is missing, then so is
                        item 1.  Note, item 1 being at the end of the list was handled above
                         */

                            if (n + 3 <= N) {
                                // there is a 3rd item
                                FileInfo c = fileList.get(n + 2);

                                if (!b.getShortPath().equals(c.getShortPath())) {
                                    // items 1 (a) and 2(b) are missing
                                    System.out.println("Items " + a.getShortPath() + " and " +
                                            b.getShortPath() + "\n are missing");
                                    // set item 1 (item a) to missing
                                    if (a.isDirectory()) {
                                        int ad = a.getDispo();
                                        // set directory itself is missing flag code 8
                                        ad = ad | (1 << 3);
                                        // clear not dispo'd bit
                                        ad = ad & (~(1 << 4));
                                        a.setDispo(ad);
                                        backDispo(a.getShortPath(), a.getPath(), 2);
                                    } else {
                                        // item 1 is a file (not a dir)
                                        a.setDispo(2);
                                        backDispo(a.getShortPath(), a.getPath(), 2);
                                    }
                                    // set item 2 (item b) to missing

                                    if (b.isDirectory()) {
                                        int bd = b.getDispo();
                                        // set directory itself is missing flag code 8
                                        bd = bd | (1 << 3);
                                        // clear not dispo'd bit
                                        bd = bd & (~(1 << 4));
                                        b.setDispo(bd);
                                        backDispo(b.getShortPath(), b.getPath(), 2);
                                    } else {
                                        // item 2 is a file (not a dir)
                                        b.setDispo(2);
                                        backDispo(b.getShortPath(), b.getPath(), 2);
                                    }
                                    // 2 items were dispositioned - set n
                                    n = n + 2;

                                } else {
                                    // item2 = item3 and therefore only item 1 is missing
                                    // therefore only item 1 will be dispositioned
                                    // set item 1 to missing
                                    System.out.println("Item " + a.getShortPath() + " is missing");
                                    if (a.isDirectory()) {
                                        int ad = a.getDispo();
                                        // set directory itself is missing flag code 8
                                        ad = ad | (1 << 3);
                                        // clear not dispo'd bit
                                        ad = ad & (~(1 << 4));
                                        a.setDispo(ad);
                                        backDispo(a.getShortPath(), a.getPath(), 2);
                                    } else {
                                        // item 1 is a file (not a dir)
                                        a.setDispo(2);
                                        backDispo(a.getShortPath(), a.getPath(), 2);
                                    }
                                    // 1 item was dispostioned - set n
                                    n = n + 1;
                                }
                            } else {
                                // items 1 and items 2 are different and they are the last files
                                // therefore they are both missing
                                // set item 1 (item a) to missing
                                System.out.println("items " + a.getShortPath() + " and " + b.getShortPath()
                                        + "\n are missing");
                                if (a.isDirectory()) {
                                    int ad = a.getDispo();
                                    // set directory itself is missing flag code 8
                                    ad = ad | (1 << 3);
                                    // clear not dispo'd bit
                                    ad = ad & (~(1 << 4));
                                    a.setDispo(ad);
                                    backDispo(a.getShortPath(), a.getPath(), 2);
                                } else {
                                    // item 1 is a file (not a dir)
                                    a.setDispo(2);
                                    backDispo(a.getShortPath(), a.getPath(), 2);
                                }
                                // set item 2 (item b) to missing

                                if (b.isDirectory()) {
                                    int bd = b.getDispo();
                                    // set directory itself is missing flag code 8
                                    bd = (bd | (1 << 3));
                                    // clear not dispo'd bit
                                    bd = bd & (~(1 << 4));
                                    b.setDispo(bd);
                                    backDispo(b.getShortPath(), b.getPath(), 2);
                                } else {
                                    // item 2 is a file (not a dir)
                                    b.setDispo(2);
                                    backDispo(b.getShortPath(), b.getPath(), 2);
                                }
                                // 2 items were dispositioned - set n and will bring one beyond the index
                                n = n + 2;
                                if (n != N)
                                    System.out.println("expected these items were last items and they were not"
                                            + "\n " + a.getShortPath() + " and " + b.getShortPath());

                            }

                        }
                    }

                }
                updateProgress(n, statsn1);


            } while ((N - (n + 1)) >= 0);
            finishTime = LocalDateTime.now();
            System.out.println("Analysis finished at: " + finishTime);
            Duration diff=Duration.between(startTime,finishTime);
            System.out.println("Analysis done in " + diff.toDaysPart() + " Days " + diff.toHoursPart() +
                    " Hours " + diff.toMinutesPart() + " minutes " + (diff.toSecondsPart()+diff.toMillisPart()/1000.0) +
                    " seconds");

            return null;
        }
    }

    /**
     * sets the dispo codes in the FileInfo's of fileList. This is the same as the method
     * dispoFileList except it assumes it is impossible to have 2 files with the same name in the same
     * directory, which is applicable to Windows
     */

    class dispoFileListNoSameName extends Task<Void> {

         /**
         * N the number of files to be analysed
         * n the index of fileList corresponding to the current "1st" item being analysed
         * more this parameter is true while there is more to analyse
         */

        int n =0;
        int N=0;

        @Override
        protected void cancelled() {
            updateProgress(0.00,100);
        }

        @Override
        protected Void call() {
            N = fileList.size();
            System.out.println("\t** Starting task 5 dispo file list ** No Same Name version!");
            if (N != statsn2)
                System.out.println("Warning fileList is of size " + N + "but number of files is " + statsn2);

            do {

                // numLeft is the number of items left to compare. 1 is a special case and 2 is a special case of there
                // are differences

                if (isCancelled())
                    break;
                int numLeft = N - n - 1;

                if (statsn1!=0) {
                    updateProgress(n, statsn1);
                }

                if (numLeft == 0) {
                    System.out.println("comparing item " + (n + 1) + " of " + N);
                    FileInfo m = fileList.get(n);
                    System.out.println("This is the last item and is missing: " + m.getShortPath());
                    if (m.isDirectory()) {
                        // need to set and clear flags and back dispo
                        int mf = m.getDispo();
                        // set directory itself is missing flag
                        mf = mf | 8;
                        // clear not dispo'd flag
                        mf = mf & ~16;
                        // apply changes
                        m.setDispo(mf);
                        System.out.println(m.getShortPath() + ": dispoflag is now: " + fileList.get(n).getDispo());
                        backDispo(m.getShortPath(), m.getPath(), 2);
                        //more = false;
                        // no more files to dispo but update anyway to terminate do loop
                        n = n + 1;
                    } else {
                        // item is a file
                        m.setDispo(2);
                        System.out.println(m.getShortPath() + ": dispo is now: " + fileList.get(n).getDispo());
                        backDispo(m.getShortPath(), m.getPath(), 2);
                        //more = false;
                        //no more files to dispo but update anyway to terminate do loop
                        n = n + 1;
                    }

                } else {
                    // else numLeft is 1 or more. if the next 2 items are different then we might have to see if there is
                    // a 3rd item to dispo the first 2 items. The rest of the cases will be handled here.


                    // get first and second items a and b respectively
                    System.out.println("comparing items " + (n + 1) + " and " + (n + 2) + " of " + N);
                    FileInfo a = fileList.get(n);
                    FileInfo b = fileList.get(n + 1);

                    if (a.equals(b)) {
                        // Case #2: items are the same by crc so dispocode is 0
                        // a bit by bit comparison might prove they are different
                        a.setDispo(0);
                        b.setDispo(0);
                        // 2 items are dispo'd thus advance n by 2.
                        n = n + 2;
                    } else {
                        // starting case 3 (there are differences)

                        if (a.isDiffer(b)) {
                            // everything is the same but the crc codes are different
                            System.out.println("Items: " + a.getShortPath() + " are different by crc " + a.getCrcValue()
                                    + " vs " + b.getCrcValue());
                            // set dispocodes to zero
                            a.setDispo(1);
                            b.setDispo(1);
                            System.out.println("item " + (n + 1) + "dispo set to differ crc : " + fileList.get(n + 1).getDispo());
                            // backdispo a and b
                            backDispo(a.getShortPath(), a.getPath(), 1);
                            backDispo(b.getShortPath(), b.getPath(), 1);
                            // 2 items were dispo'd thus advance n by 2.
                            n = n + 2;

                        }

                                                // now continuing case that item1 and item2 are different

                        if (!a.getShortPath().equals(b.getShortPath())) {
                        /*
                        case of different files (case 4). A diligent but not exhaustive analysis indicates that
                        if item i != item 3 then item i is missing, and that if item2 is missing, then so is
                        item 1.  Note, item 1 being at the end of the list was handled above
                         */

                            if (n + 3 <= N) {
                                // there is a 3rd item
                                FileInfo c = fileList.get(n + 2);

                                if (!b.getShortPath().equals(c.getShortPath())) {
                                    // items 1 (a) and 2(b) are missing
                                    System.out.println("Items " + a.getShortPath() + " and " +
                                            b.getShortPath() + "\n are missing");
                                    // set item 1 (item a) to missing
                                    if (a.isDirectory()) {
                                        int ad = a.getDispo();
                                        // set directory itself is missing flag code 8
                                        ad = ad | (1 << 3);
                                        // clear not dispo'd bit
                                        ad = ad & (~(1 << 4));
                                        a.setDispo(ad);
                                        backDispo(a.getShortPath(), a.getPath(), 2);
                                    } else {
                                        // item 1 is a file (not a dir)
                                        a.setDispo(2);
                                        backDispo(a.getShortPath(), a.getPath(), 2);
                                    }
                                    // set item 2 (item b) to missing

                                    if (b.isDirectory()) {
                                        int bd = b.getDispo();
                                        // set directory itself is missing flag code 8
                                        bd = bd | (1 << 3);
                                        // clear not dispo'd bit
                                        bd = bd & (~(1 << 4));
                                        b.setDispo(bd);
                                        backDispo(b.getShortPath(), b.getPath(), 2);
                                    } else {
                                        // item 2 is a file (not a dir)
                                        b.setDispo(2);
                                        backDispo(b.getShortPath(), b.getPath(), 2);
                                    }
                                    // 2 items were dispositioned - set n
                                    n = n + 2;

                                } else {
                                    // item2 = item3 and therefore only item 1 is missing
                                    // therefore only item 1 will be dispositioned
                                    // set item 1 to missing
                                    System.out.println("Item " + a.getShortPath() + " is missing");
                                    if (a.isDirectory()) {
                                        int ad = a.getDispo();
                                        // set directory itself is missing flag code 8
                                        ad = ad | (1 << 3);
                                        // clear not dispo'd bit
                                        ad = ad & (~(1 << 4));
                                        a.setDispo(ad);
                                        backDispo(a.getShortPath(), a.getPath(), 2);
                                    } else {
                                        // item 1 is a file (not a dir)
                                        a.setDispo(2);
                                        backDispo(a.getShortPath(), a.getPath(), 2);
                                    }
                                    // 1 item was dispostioned - set n
                                    n = n + 1;
                                }
                            } else {
                                // items 1 and items 2 are different and they are the last files
                                // therefore they are both missing
                                // set item 1 (item a) to missing
                                System.out.println("items " + a.getShortPath() + " and " + b.getShortPath()
                                        + "\n are missing");
                                if (a.isDirectory()) {
                                    int ad = a.getDispo();
                                    // set directory itself is missing flag code 8
                                    ad = ad | (1 << 3);
                                    // clear not dispo'd bit
                                    ad = ad & (~(1 << 4));
                                    a.setDispo(ad);
                                    backDispo(a.getShortPath(), a.getPath(), 2);
                                } else {
                                    // item 1 is a file (not a dir)
                                    a.setDispo(2);
                                    backDispo(a.getShortPath(), a.getPath(), 2);
                                }
                                // set item 2 (item b) to missing

                                if (b.isDirectory()) {
                                    int bd = b.getDispo();
                                    // set directory itself is missing flag code 8
                                    bd = bd | (1 << 3);
                                    // clear not dispo'd bit
                                    bd = bd & (~(1 << 4));
                                    b.setDispo(bd);
                                    backDispo(b.getShortPath(), b.getPath(), 2);
                                } else {
                                    // item 2 is a file (not a dir)
                                    b.setDispo(2);
                                    backDispo(b.getShortPath(), b.getPath(), 2);
                                }
                                // 2 items were dispositioned - set n and will bring one beyond the index
                                n = n + 2;
                                if (n != N)
                                    System.out.println("expected these items were last items and they were not"
                                            + "\n " + a.getShortPath() + " and " + b.getShortPath());

                            }

                        }
                    }

                }
                updateProgress(n, statsn1);


            } while ((N - (n + 1)) >= 0);
            finishTime = LocalDateTime.now();
            System.out.println("Analysis finished at: " + finishTime);
            Duration diff=Duration.between(startTime,finishTime);
            System.out.println("Analysis done in " + diff.toDaysPart() + " Days " + diff.toHoursPart() +
                    " Hours " + diff.toMinutesPart() + " minutes " + (diff.toSecondsPart()+diff.toMillisPart()/1000.0) +
                    " seconds");

            return null;
        }
    }

    /**
     * @param backPath the short path of the file whose parent directories are to be marked with
     *                 disposition code dispocode. The bit of the dispocode is to be set, but all
     *                 other bits to remain unchanged by the operation
     * @param pathLet the letter 'A' or 'B' corresponding to pathA or pathB respectively
     * @param dispocode the bit of the disposition code that is to be set for all parent directories
     * if a file in a directory is missing or the file in root path A is different in crc than
     * the crc code of the corresponding file in root path B, the dispo code
     * of 1 will be applied to all parent directories to show that such
     * directories have files in their tree with a different crc than the corresponding
     * file in root path B. An analogous case exists for a missing file and dispo code 2.
     * a missing directory (dispo code 8) is backdispositioned as dispo code 2 in
     * parent directories. The description of disposition codes is in the comments for
     * FileInfo.dispo
     */

    private void backDispo(String backPath, char pathLet, int dispocode) {

        System.out.println("back dispo'ing " + backPath);
        //String[] dirs = backPath.split("\\\\");
        //System.out.println("dirs length is: " + dirs.length);
        int i = backPath.lastIndexOf('\\');
        // if i = 0 then I think we have just "\" on which nothing should be done
        // but this will not handle the case of dispo'ing pathA or pathB, which is probably not useful either

        FileInfo a;
        Integer ind;
        int newdispo = dispocode;
        while (i>0) {

            String k = backPath.substring(0,i);
            System.out.println("looking up: " + k + " with i = " + i);
            ind = dirInd.get(pathLet+k);

            if (ind==null)
                System.out.println("An error occurred - index not found");
            // null pointer is very unlikely; fixing with a new dispo code can be a future improvement
            a = fileList.get(ind);

            System.out.println("Retrieved "+a.getShortPath() +" with dispo: "+a.getDispo() + " to add bit: "+dispocode);
            newdispo = a.getDispo() | newdispo;
            a.setDispo(newdispo);

            System.out.println("Dispo is now:");
            System.out.println(a.getPath() + ": " + a.getShortPath() + " dispo = " + fileList.get(ind).getDispo());

            i=i-1;
            i = backPath.lastIndexOf('\\',i);
        }

        System.out.println(dirInd);


    }
    /**
     * makes a report of the analysis per user choices
     */

     void makeReport() {
         
        /*
        * If bit 4 (file's dispo not evaluated) is an error (all should have been evaluated)
        * so will consider bit 4 as a difference in files and will report. Future improvement
        * would provide more information on why bit 4 remained.
        * Thus will iterate through the list*/

        int numAitems =0;
        int numBitems =0;
        Predicate<FileInfo> fileInfDiff = (FileInfo a) -> {
            /* whether a difference was detected */

            return a.getDispo()!=0;
        };

        Predicate <FileInfo> showItemDiffSame = (FileInfo a) -> {
            // determines if an item should be shown based on show different or show same user choices
            // an item has a difference and user choices to see differences
            if (fileInfDiff.test(a)&&((1&choiceFlag)>0) )
                return true;
            // an item has no detected differences and the user chooses to see items with no differences
            return !fileInfDiff.test(a) && (((1 << 1) & choiceFlag) > 0);
            // other possibilities are contrary to user request
        };

        // clear report
        aText.clear();
        bText.clear();
        aExtraText.clear();
        bExtraText.clear();


        System.out.println("//// Beginning of Report ////");

        for (FileInfo a : fileList){

            if (!a.isDirectory()&&(((1<<3)&choiceFlag) >0)&&showItemDiffSame.test(a)){
                if (a.getPath()=='A')
                    numAitems=numAitems+1;
                else {numBitems=numBitems+1;}
                System.out.println(dispoMeaning(a));
            }

            // case of item being a dir and item should be shown based on show diff/ no diff user choice
            if (a.isDirectory()&&(((1<<2)&choiceFlag) >0)&&showItemDiffSame.test(a)) {
                if (a.getPath()=='A')
                    numAitems=numAitems+1;
                else {numBitems=numBitems+1;}
                System.out.println(dispoMeaning(a));

            }
            // no other cases should be displayed

        }
        System.out.println(numAitems+" A items shown and " + numBitems+" B items shown");
        System.out.println("Note: notes about directories having differences in children are not counted in the" +
                " above total.");
    }

    /**
     *
     * @param x the File info whose disposition code is to be explained in plain English and which will be displayed
     *          in the JavaFX interface.
     * @return A string for printing comments via println, for example. This is not necessary. One might want to
     * make this method have no return value in the future.
     */
    private String dispoMeaning (FileInfo x) {
        int disp = x.getDispo();
        // the current path
        char cp = x.getPath();
        // current path nickname
        String cpnn = cp == 'A' ? pathANickName : pathBNickName;
        // other path nickname
        String opnn = cp == 'A' ? pathBNickName : pathANickName;

        String outMessage="";


        if (disp==0)
        {
            outMessage = outMessage +"No differences found in files " +x.getShortPath() + "\n";
            // add message to the appropriate window - might want to add try catch
            if (cp=='A') {
                aText.add("No differences found in files " +x.getShortPath() + "\n");

        }
            else {
                bText.add("No differences found in files " +x.getShortPath() + "\n");
            }

        }

        if ((disp&(1<<0)) > 0)
        {
            if (!x.isDirectory()){
            outMessage = outMessage + "Differences were found in the files " + x.getShortPath() + "\n";
            // add message to the appropriate window might want to add try catch
                if (cp=='A') {
                    aText.add("Differences were found in the files " + x.getShortPath() + "\n");
                }
                else {
                    bText.add("Differences were found in the files " + x.getShortPath() + "\n");
                }
            }
            else {
                outMessage = outMessage + "\tExtra info: The directory "+ x.getShortPath() + " in \"" +cpnn + "\" " +
                        "has files that are different " +
                    "\tin content than the corresponding files in \"" + opnn + "\"\n";
                // add message to the appropriate window might want to add try catch
                if (cp=='A') {
                    aExtraText.add("Extra info: The directory "+ x.getShortPath() + " in \"" +cpnn + "\" " +
                            "has files that are different " +
                            "in content than the corresponding files in \"" + opnn + "\"\n");
                }
                else {
                    bExtraText.add("Extra info: The directory "+ x.getShortPath() + " in \"" +cpnn + "\" " +
                            "has files that are different " +
                            "in content than the corresponding files in \"" + opnn + "\"\n");
                }
            }

        }

        if ((disp&(1<<1)) > 0)
        {
            if(!x.isDirectory()){
            outMessage = outMessage + "The file " +x.getShortPath() + " in \"" + cpnn + "\" was not found in \""
                    +opnn + "\"\n";
            // add message to the appropriate window might want to add try catch
                if (cp == 'A') {
                    aText.add("The file " +x.getShortPath() + " in \"" + cpnn + "\" was not found in \""
                            +opnn + "\"\n");
                }
                else {
                    bText.add("The file " +x.getShortPath() + " in \"" + cpnn + "\" was not found in \""
                            +opnn + "\"\n");
                }
            }
            else {
                outMessage = outMessage + "\t Extra info: The directory " +x.getShortPath() + " in \"" +cpnn +
                        "\" contains files not found in \t"
                        +opnn +"\"\n";
                // add message to the appropriate window. Adding try catch in future rev's can be considered
                if (cp == 'A') {
                    aExtraText.add("Extra info: The directory " +x.getShortPath() + " in \"" +cpnn +
                            "\" contains files not found in "+opnn +"\"\n");
                }
                else {
                    bExtraText.add("Extra info: The directory " +x.getShortPath() + " in \"" +cpnn +
                            "\" contains files not found in "+opnn +"\"\n");
                }

            }
        }

        if ((disp&(1<<3))>0)
        {
            outMessage = outMessage + "The directory " + x.getShortPath() + " in \"" +cpnn + "\" was not found in \"" + opnn +"\"\n";
            // add message to appropriate window might want to add try catch
            if (cp == 'A') {
                aText.add("The directory " + x.getShortPath() + " in \"" +cpnn + "\" was not found in \"" + opnn +"\"\n");

            }
            else {
                bText.add("The directory " + x.getShortPath() + " in \"" +cpnn + "\" was not found in \"" + opnn +"\"\n");
            }

            }

        if ((disp&(1<<4)) > 0) // case of unevaluated file is unlikely and can be handed in future versions
        {
            if (!x.isDirectory()){
                outMessage = outMessage + "Error! File " + x.getShortPath() + " was not evaluated" + "\n" ;
                // add message to the appropriate window might want to add try catch
                if (cp=='A') {
                    aText.add("Error! File " + x.getShortPath() + " was not evaluated" + "\n");
                }
                else {
                    bText.add("Error! File " + x.getShortPath() + " was not evaluated" + "\n");
                }
            }
            else {
                outMessage = outMessage + "\tExtra info: Error! The directory "+ x.getShortPath() + " in \"" +cpnn + "\" " +
                        " was not valuated!" + "\n";
                // add message to the appropriate window might want to add try catch
                if (cp=='A') {
                    aExtraText.add("Extra info: Error! The directory "+ x.getShortPath() + " in \"" +cpnn + "\" " +
                            " was not valuated!" + "\n");
                }
                else {
                    bExtraText.add("Extra info: Error! The directory "+ x.getShortPath() + " in \"" +cpnn + "\" " +
                            " was not valuated!" + "\n");
                }
            }

        }


        if (outMessage.equals("")) {
            // add message to appropriate window
            if (cp=='A'){
            aText.add("no comment on file: " + x.getShortPath()); }
            else {
                bText.add("no comment on file: "+x.getShortPath());
            }
            return "no comment on file " +x.getShortPath() + " in \"" + opnn + "\"" + " with dispo " +x.getDispo();

        }

        return outMessage;
    }




    }


