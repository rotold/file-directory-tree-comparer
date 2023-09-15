package com.example.filetreecomparer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class Controller {
    //future versions might allow other fileDB's to be handled in the same application session
    private FileDB anal1 = new FileDB();
    /* finishTime the time at which the analysis finishes
    startTime the time at which the analysis starts*/
    private LocalDateTime finishTime;
    private LocalDateTime startTime;

    Task<Void> task1;
    Task<Void> task2;
    Task<Void> task3;
    Task<Void> task4;
    Task<Void> task5;

    // array of the tasks. The unchecked assignment is consider acceptable
    private final Task<Void>[] taskList = new Task[5];

    ChangeListener<Worker.State> runningCheck = (ObservableValue<? extends Worker.State> ov, Worker.State oldState,
    Worker.State newState) -> {
        tasksRunning();
    };
    /* whether pathA or pathB exists
    * bit zero pathA exists, bit 1 pathB exists; 1 means exists */
    private final SimpleIntegerProperty paths = new SimpleIntegerProperty(0);

    /* analRunning whether the analysis is running */
    private final SimpleBooleanProperty analRunning = new SimpleBooleanProperty(false);
    /*reportReady whether all tasks are finished*/
    private final SimpleBooleanProperty reportReady = new SimpleBooleanProperty(false);


    private void tasksRunning() {
        // determines if analysis is running or about to run and sets analRunning accordingly
        // to be used in task listeners to change analRunning
        // also checks if all tasks are successful in which case a report can be run
        // if all tasks are successful, the finishTime is also recorded
        boolean isRunning = false;
        boolean reportable = true;
        Worker.State state;
        for (Task<Void> a : taskList){
            state = a.getState();
            if(state.equals(Worker.State.RUNNING)||state.equals(Worker.State.READY)||
            state.equals(Worker.State.SCHEDULED))
                isRunning=true;
            if(!state.equals(Worker.State.SUCCEEDED))
                reportable=false;
        }

        if (reportable) {
            finishTime=LocalDateTime.now();
        }

        analRunning.set(isRunning);
        reportReady.set(reportable);

    }
    @FXML
    private TextField analNameField;

    @FXML
    private GridPane pathApane;

    @FXML
    private Button pathAButton;

    @FXML
    private Button pathBButton;

    @FXML
    private Label pathALabel;

    @FXML
    private Label pathBLabel;

    @FXML
    private TextField pathANick;

    @FXML
    private TextField pathBNick;

    @FXML
    private CheckBox subfoldersCheck;

    @FXML
    private CheckBox sameNameCheck;

    @FXML
    private CheckBox sysFilesCheck;

    @FXML
    private ProgressBar populateProgress;

    @FXML
    private ProgressBar sortProgress;

    @FXML
    private ProgressBar dispoProgress;

    @FXML
    private Label initLabel;

    // --- report tab items
    @FXML
    private CheckBox choiceBit1box, choiceBit2box, choiceBit3box, choiceBit4box;

    @FXML
    private Button startButton;

    @FXML
    private Button cancelButton;

    // -- end report option tab items

    // beginning of report tab items
    @FXML
    private Button mkReportButton;

    @FXML
    private Label aNameLabel;

    @FXML
    private Label bNameLabel;

    @FXML
    private TextFlow aTxtFlow;

    @FXML
    private TextFlow aInfoTxtFlow;

    @FXML
    private TextFlow bTxtFlow;

    @FXML
    private TextFlow bInfoTxtFlow;

    @FXML
    private Label AnalNameLabel;

    @FXML
    private Label AnalStartTimeLabel;

    @FXML
    private Label AnalEndTimeLabel;

    @FXML
    private Label AnalElapsedTimeLabel;

    @FXML
    private void setAnalName() {
        anal1.setAnalName(analNameField.getText());
        System.out.println("fileDB analysis name set to: " + anal1.getAnalName());
    }

    @FXML
    private void loadPathA(ActionEvent event) {
        // attempted with http://tutorials.jenkov.com/javafx/directorychooser.html and pg 943
        // need to add code to check for null and for pathA=pathB

        // double check analysis is not running
        if(analRunning.getValue())
            return;
        reportReady.set(false);

        Stage pathAstage = new Stage();
        DirectoryChooser dirChooseA = new DirectoryChooser();
        File selected;
        selected = dirChooseA.showDialog(pathAstage);
        if (selected==null)
        {
            // future improvement: use also a pop-up
            pathALabel.setText("You chose no File - please choose file");
            // set pathA bit to false
            int noPathA = paths.get()&(~(1));
            paths.set(noPathA);
        }
        else {
        String patha = selected.getAbsolutePath();

        // future improvement: if patha = anal1.pathB then pop up and say paths are the same
        System.out.println("The chosen path is: " + patha);
        anal1.setPathA(patha);
        System.out.println("PathA in fileDB is set to: " + anal1.getPathA());
        pathALabel.setTextFill(Color.web("#008800"));
        pathALabel.setText(anal1.getPathA());

        int pathAis = paths.get()|1;
        paths.set(pathAis);

        }


    }

    @FXML
    private void loadPathB(ActionEvent event) {
        // attempted with http://tutorials.jenkov.com/javafx/directorychooser.html and pg 943
        // need to add code to check for null and pathA=pathB

        //double check analysis is not running
        if(analRunning.getValue())
            return;
        reportReady.set(false);

        Stage pathBstage = new Stage();
        DirectoryChooser dirChooseB = new DirectoryChooser();
        File selected;
        selected = dirChooseB.showDialog(pathBstage);
        if (selected==null)
        {
            pathBLabel.setText("You chose no File - please choose file");
            //set pathB bit to false
            int noPathB = paths.get()&(~(1<<1));
            paths.set(noPathB);

        }
        else {
        String pathb = selected.getAbsolutePath();
        // future improvement: if pathb = anal1.pathA then show pop up and say paths are the same
        System.out.println("The 2nd chosen path is: " + pathb);
        anal1.setPathB(pathb);
        System.out.println("PathB in fileDB is set to: " + anal1.getPathB());
        pathBLabel.setTextFill(Color.web("#008800"));
        pathBLabel.setText(anal1.getPathB());

        int pathBis = paths.get()|(1<<1);
        paths.set(pathBis);

        }

    }

    @FXML
    private void setPathANick() {
        anal1.setPathANickName(pathANick.getText());
        System.out.println("FileDB pathANickName set to: " + anal1.getPathANickName());
    }

    @FXML
    private void setPathBNick() {
        anal1.setPathBNickName(pathBNick.getText());
        System.out.println("FileDB pathBNickName set to: " + anal1.getPathBNickName());
    }

    @FXML

    private void setSubfoldersChoice() {
        anal1.setSubfolders(subfoldersCheck.isSelected());
        System.out.println("fileDB subfolders value is set to: " + anal1.isSubfolders());
        reportReady.set(false);
    }

    @FXML

    private void setSameNameChoice() {
        anal1.setSameName(sameNameCheck.isSelected());
        System.out.println("fileDB sameName is set to: " + anal1.isSameName());
        reportReady.set(false);
    }

    @FXML

    private void setSysFilesChoice() {
        anal1.setSystemFiles(sysFilesCheck.isSelected());
        System.out.println("System Files is set to: " + anal1.isSystemFiles());
        reportReady.set(false);
    }

    // future improvement: need to make a restore button that restores - make it be the initial values in fileDB

    // begin report option tab methods
    @FXML
    private void setChoiceFlag() {
        /* This will read all check boxes to be safe in case there are delays in the system, although code is
         * more efficient to update only the box that is changed. Another way to do the code is to
         * use anal1.getChoiceFlag before all the if statements then delete anal1.setChoiceFlag(choice) in
         * all if statements and instead of calling anal1.getChoiceFlag in the if statements, simply use
         * choice. Finally at the end of all the if statements use anal1.setChoiceFlag(choice); This is because
         * using choice in the if statements will not hurt any of the bits of choice, but all the bits are
         * updated by the if statements sequentially.  This whole idea was noted from the fact that the same
         * statement anal1.setChoiceFlag(choice) is in both the if and else clause and thus will always be
         * executed and thus can be put outside the if-else statements */

        int choice;

        if (choiceBit1box.isSelected()) {
            choice = anal1.getChoiceFlag() | (1 << 0);
            anal1.setChoiceFlag(choice);
        } else {
            choice = anal1.getChoiceFlag() & ~(1 << 0);
            anal1.setChoiceFlag(choice);
        }

        if (choiceBit2box.isSelected()) {
            choice = anal1.getChoiceFlag() | (1 << 1);
            anal1.setChoiceFlag(choice);
        } else {
            choice = anal1.getChoiceFlag() & ~(1 << 1);
            anal1.setChoiceFlag(choice);
        }

        if (choiceBit3box.isSelected()) {
            choice = anal1.getChoiceFlag() | (1 << 2);
            anal1.setChoiceFlag(choice);
        } else {
            choice = anal1.getChoiceFlag() & ~(1 << 2);
            anal1.setChoiceFlag(choice);
        }

        if (choiceBit4box.isSelected()) {
            choice = anal1.getChoiceFlag() | (1 << 3);
            anal1.setChoiceFlag(choice);

        } else {
            choice = anal1.getChoiceFlag() & ~(1 << 3);
            anal1.setChoiceFlag(choice);
        }

        System.out.println("bit 1 show differences is: " + (anal1.getChoiceFlag() & (1 << 0)));
        System.out.println("bit 2 show \"same \" is : " + (anal1.getChoiceFlag() & (1 << 1)));
        System.out.println("bit 3 show directories is: " + (anal1.getChoiceFlag() & (1 << 2)));
        System.out.println("bit 4 show files is: " + (anal1.getChoiceFlag() & (1 << 3)));
        System.out.println("choiceFlag is: " + anal1.getChoiceFlag());


    }

    @FXML
    private void start() {
        // update label for start time
        // references:
        // https://stackoverflow.com/questions/43779114/how-to-bind-a-javafx-progressbar-to-a-double-value-stored-in-an-object
        // and https://stackoverflow.com/questions/46614509/javafx-progress-bar-doesnt-update/46616046#46616046

        // making exec a class instance variable caused exceptions when re-running analysis and also application
        // did not fully terminate

        startTime=LocalDateTime.now();
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Controller.this.task1 = anal1.new intializeFileList();
        Controller.this.task2 = anal1.new populateFileList();
        Controller.this.task3 = anal1.new sortFileList();
        Controller.this.task4 = anal1.new populateDirInd();
        // determine if code is needed to handle the case of a file having the same name as another (but
        // different type as allowed in UNIX type OS) in which case backslash would be changed to slash
        if(anal1.isSameName()){
            // handles same name condition
            Controller.this.task5 = anal1.new dispoFileList();
        }
        else {
            Controller.this.task5=anal1.new dispoFileListNoSameName();
        }

        taskList[0]=Controller.this.task1;
        taskList[1]=Controller.this.task2;
        taskList[2]=Controller.this.task3;
        taskList[3]=Controller.this.task4;
        taskList[4]=Controller.this.task5;

        populateProgress.progressProperty().bind(task2.progressProperty());
        sortProgress.progressProperty().bind(task3.progressProperty());
        dispoProgress.progressProperty().bind(task5.progressProperty());

        for (Task<Void> a : taskList){
            //https://www.programcreek.com/java-api-examples/?api=javafx.concurrent.Worker.State
            //https://docs.oracle.com/javase/8/javafx/api/javafx/beans/value/ChangeListener.html, which says:
            // "The same instance of ChangeListener can be registered to listen to multiple ObservableValues"

            a.stateProperty().addListener(runningCheck);
        }

        reportReady.set(false);

        exec.submit(task1);
        exec.submit(task2);
        exec.submit(task3);
        exec.submit(task4);
        exec.submit(task5);
        // necessary to make application terminate after closing window
        exec.shutdown();


        // https://stackoverflow.com/questions/21083945/how-to-avoid-not-on-fx-application-thread-currentthread-javafx-application-th
        //@Yannic Hansen it is because in javaFX only the FX thread can modify the ui elements.
        // That's why you use Platform.runLater. – GOXR3PLUS Apr 10 '16 at 21:00
        // calling (Platform.runLater(new Runnable(){
        //// ...
        //}); will fix it
        /// ---
        // tech article at https://www.developer.com/java/data/multithreading-in-javafx.html
        // seems these techniques have to be used correctly per
        // https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/concurrency.htm
        // and https://openjfx.io/javadoc/14/javafx.graphics/javafx/concurrent/package-summary.html
        // per my question at https://stackoverflow.com/questions/62975528/javafx-controller-intermittently-not-updating-gui-despite-platform-runlater-and


    }

    @FXML
    private void cancel()
    {
        for (Task<Void> a : taskList){

            if(a!=null && !a.getState().equals(Worker.State.CANCELLED))
                a.cancel();
        }
        reportReady.set(false);
    }

    // end of report option tab methods

    // beginning of report tab methods

    @FXML
    private void makeReport() {
        anal1.makeReport();
        // used
        // https://www.geeksforgeeks.org/javafx-textflow-class/

        aNameLabel.setText(anal1.getPathANickName());
        bNameLabel.setText(anal1.getPathBNickName());

        // clear report
        aTxtFlow.getChildren().clear();
        bTxtFlow.getChildren().clear();
        aInfoTxtFlow.getChildren().clear();
        bInfoTxtFlow.getChildren().clear();

        for(String s:anal1.getaText()){
            Text a = new Text(s);
            aTxtFlow.getChildren().add(a);
        }

        for (String s:anal1.getaExtraText()){
            Text a = new Text(s);
            aInfoTxtFlow.getChildren().add(a);
        }

        for (String s:anal1.getbText()) {
            Text a = new Text(s);
            bTxtFlow.getChildren().add(a);
        }

        for (String s:anal1.getbExtraText()) {
            Text a = new Text(s);
            bInfoTxtFlow.getChildren().add(a);
        }

        Duration diff= Duration.between(startTime,finishTime);
        String delta = diff.toDaysPart() + " Days " + diff.toHoursPart() + " Hours " + diff.toMinutesPart() +
                " minutes " + (diff.toSecondsPart()+diff.toMillisPart()/1000.0) +
                " seconds";
        AnalStartTimeLabel.setText(startTime.toString());
        AnalEndTimeLabel.setText(finishTime.toString());
        AnalElapsedTimeLabel.setText(delta);
        AnalNameLabel.setText(anal1.getAnalName());

    }

    // end of report tab methods

    @FXML
    public void initialize() {

        // Do bindings
        /* reference for createBooleanBinding
        https://www.codota.com/code/java/methods/javafx.scene.control.Button/disableProperty
         */

        startButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            // condition for not being able to start analysis: both paths not defined or analysis running
            int bits01 = 1 | (1 <<1);
            return ((paths.getValue() != bits01) || analRunning.getValue());

        },paths,analRunning));

        Callable<Boolean> Notrunning = () -> {
            // see if analysis is not running to disable Cancel
            return !analRunning.getValue();
        };

        Callable<Boolean> Running = analRunning::getValue;

        cancelButton.disableProperty().bind(Bindings.createBooleanBinding(Notrunning,analRunning));
        pathAButton.disableProperty().bind(Bindings.createBooleanBinding(Running,analRunning));
        pathBButton.disableProperty().bind(Bindings.createBooleanBinding(Running,analRunning));
        subfoldersCheck.disableProperty().bind(Bindings.createBooleanBinding(Running,analRunning));
        sysFilesCheck.disableProperty().bind(Bindings.createBooleanBinding(Running,analRunning));
        sameNameCheck.disableProperty().bind(Bindings.createBooleanBinding(Running,analRunning));

        Callable<String> statusTxt = () -> {
            // creates the text for initLabel
            int bits11 = (1)|(1<<1);
            if (paths.getValue()!=bits11){
                // still need to input paths
                return "Please enter directory options";
            }
            else {
                // paths entered need to see if running
                if(!analRunning.getValue()){
                    //not running so need to see if all tasks are done
                    if(reportReady.getValue())
                        return "Report Ready";
                    else
                        return "Ready";
                }
                else {
                    return "Analysis Running";
                }
            }
        };

        Callable<Boolean> noReport = () -> !reportReady.getValue();


        initLabel.textProperty().bind(Bindings.createStringBinding(statusTxt,paths,analRunning,reportReady));
        mkReportButton.disableProperty().bind(Bindings.createBooleanBinding(noReport,reportReady));


    }

}
