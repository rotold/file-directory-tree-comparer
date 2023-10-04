# file-directory-tree-comparer
A sample program that finds the differences between 2 directory trees. It detects differences in CRC32's and whether files are missing. It uses a JavaFX GUI that was built with Scene Builder.

<p align="center"> General overview (clarifying details are in code comments): </p>

The program will find differences (missing files, or files with different contents based on the CRC32 hash code) between two directory trees.  An example use of this program is checking or comparing backups.  Example candidates for future improvements is to do a byte by byte comparison of files to essentially guarantee whether there is a difference in content between the corresponding files.

To use the program, the user chooses 2 different directories, optional reference names, and appropriate options into the "Directory Options" and "Report Options" tabs of the GUI (Graphical User Interface).  There are various options for the user including whether to show items with differences detected or to show only items with no differences detected, etc.  The program will then analyse both file directory trees. The progress of the analysis can be monitored in the "Report Options" tab.  When the analysis is finished, the "Generate Report" button in the "Report" Tab becomes active, and the results will be displayed. Options in the Report Options tab can be changed and the report re-run in the Report tab without re-running the analysis.

<p align="center"> Directory Options Tab</p>

<img align="center" src="assets/Screenshot Dir Options.jpg"/>

<p align="center"> Report Options Tab with a Large Analysis in Progress </p>

<img align="center" src="assets/Screenshot running and options.jpg"/>

<p align="center"> Report Tab </p>

<img align="center" src="assets/Screenshot Report.jpg"/>

<p align="center"> Other Details </p>

To find differences, the algorithm steps once through the sorted file data in an ArrayList and compares the data staying within 2 positions of the current position.

A disposition code (shorted to "dispo code" in the comments) is a bit vector int (FileInfo.dispo) that gives the attributes of any detected discrepancy between files. The value is zero if the file was analysed and the file exists in both directory trees and no differences were detected in the CRC32 hash codes.

To "disposition" (shorted to "dispo" in the comments) means to determine the disposition code of a file or directory.
 
Backdispo is an added feature that associates a parent directory with a discrepancy (missing or different contents) in any file within its subtree and is in the extra info data on the bottom windows of the Report tab. It is relevant only to directories and not files. Also add that FileDB.dirInd and related code is therefore not necessary for the analysis of files. dirInd is a HashMap to allow O(1) retrieval of the dispocode codes of the relevant files that need to be "Back-dispositioned".  The user-chosen root directory itself does not inherit these items via backdispo.

The class FileDB.dispoFileList is unnecessary especially in a Windows environment. It contains untested code that allows the 2 files with the same name but with one being a directory and one being a file. It also was not modified for UNIX type directory hierarchy delimiter (slash).  The "Windows" version is FileDB.dispoFileListNoSameName and was tested.

Code uses pre-Java8 and post Java8 syntax to practice both.

Some fields and method variables are unused - these can be used for future enhancements or deleted.  Also, some logic and arithmetic expressions are purposely left unsimplified to make the purpose of the expression more apparent. Of course, the expression could be simplified and moved to the comments section.  Similarly, sometimes braces are used where unnecessary and statements that exist in several if statements are not taken out of them, etc. for ease of updating code in the future. 

There are many println statements that show the details of operation that can be deleted, or perhaps changed to output into a log file.

The use of including system files in the analysis is yet untested.

Known Bug: Testing shows that the 2 large TextFlow windows (Controller.aTxtFlow and Controller.bTxtFlow) have a display issue as follows: If the number of items is 3 or more, the last item is not drawn when it is supposed to be or it is not cleared when it is supposed to be, unless one clicks in the TextFlow box itself or one forces a redraw by going to another tab and coming back or resizing the window, etc. This does not happen when the report is run the first time. This does not happen on the smaller aInfoTxtFLow and bInfoTxtFlow TxtFLows.  Quick research indicates that a quick way to remedy this is to code in a very small (in pixel, for example) change in size of the window, but I would rather find a more elegant solution.

