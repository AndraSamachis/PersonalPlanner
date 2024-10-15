import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.json.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Calendar {
    private final Stage primaryStage;
    private YearMonth currentYearMonth;
    private Label monthYearLabel;
    private HBox header;
    private final List<Task> tasks = new ArrayList<>();
    private static Image selectedPhoto;

    private final TextArea overlappingEventsTextArea;
    private final Map<String, Boolean> taskButtonStates = new HashMap<>();

    private final String username;
    public Calendar(String username, Stage stage) {
        this.username = username;
        overlappingEventsTextArea = new TextArea();
        overlappingEventsTextArea.setMaxWidth(300);
        overlappingEventsTextArea.setMinWidth(300);
        overlappingEventsTextArea.setEditable(false);
        overlappingEventsTextArea.setWrapText(true);
        primaryStage=stage;

        currentYearMonth = YearMonth.now();
        header = createHeader();
        loadTasks();
    }


    //functie de incarcare a task-urilor in calendar
    private void loadTasks() {
        if (username == null) {
            System.err.println("Username is null. Cannot load tasks.");
            return;
        }
        String tasksfile = "tasks_" + username + ".txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(tasksfile))) {
            String line;
            Task currentTask = null;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        int day = Integer.parseInt(parts[0].trim());
                        int month = Integer.parseInt(parts[1].trim());
                        int year = Integer.parseInt(parts[2].trim());

                        currentTask = new Task(day, month, year, "");
                        tasks.add(currentTask);
                    } else if (currentTask != null) {
                        currentTask.getEventList().add(line.trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //functie de salvare task-uri in fisierul .txt
    private void saveTasks() {
        System.out.println(username);

        String tasksfile = "tasks_" + username + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tasksfile))) {
            for (Task task : tasks) {
                writer.write(task.getDay() + "," + task.getMonth() + "," + task.getYear());

                List<String> eventList = task.getEventList();

                if (eventList != null && !eventList.isEmpty()) {
                    for (String event : eventList) {
                        writer.newLine();
                        writer.write(event);
                    }
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTask(int day, int month, int year, String taskText) {
        Task task = getTaskForDay(day, month, year);
        if (task == null) {
            task = new Task(day, month, year, "");
            tasks.add(task);
        }
        task.getEventList().add(taskText);

        saveTasks();

        updateCalendar();
    }


    private VBox createGreyContainerScene(String profileUrl, String firstName, String lastName) {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Rectangle greyContainer = new Rectangle(320, 900);
        greyContainer.setFill(Color.web("#0C356A"));

        Circle photoCircle = new Circle(80);
        Button changePhotoButton = new Button("Schimbă poza");
        changePhotoButton.getStyleClass().add("changePhotoButton");
        changePhotoButton.setOnAction(e -> changeProfilePhoto(lastName, firstName, photoCircle, primaryStage));

        if (profileUrl != null && !profileUrl.isEmpty()) {
            selectedPhoto = new Image(profileUrl);
            photoCircle.setFill(new ImagePattern(selectedPhoto));
        } else {
            selectedPhoto = new Image("assets/greyaProfile.png");
            photoCircle.setFill(new ImagePattern(selectedPhoto));
        }

        Label welcomeLabel = new Label(firstName + " " + lastName);
        welcomeLabel.setTextFill(Color.web("#FFF0CE"));
        welcomeLabel.setStyle("-fx-font-size: 20px;");

        overlappingEventsTextArea.setPrefWidth(240);

        root.getChildren().addAll(photoCircle,changePhotoButton, welcomeLabel, overlappingEventsTextArea);

        VBox.setMargin(photoCircle, new Insets(40, 0, 0, 0));
        VBox.setMargin(changePhotoButton, new Insets(0, 0, 0, 0));
        VBox.setMargin(welcomeLabel, new Insets(20, 0, 0, 0));
        VBox.setMargin(overlappingEventsTextArea, new Insets(0, 0, 10, 0));
        root.setPadding(new Insets(10));

        Button backToLoginButton = new Button("Logout");
        backToLoginButton.getStyleClass().add("logoutDashboardButton");

        backToLoginButton.setOnAction(event -> backToLogin());
        backToLoginButton.setPrefWidth(300);

        VBox.setMargin(backToLoginButton, new Insets(350, 0, 0, 0));

        root.getChildren().add(backToLoginButton);

        Group group = new Group(greyContainer, root);
        group.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

        return new VBox(group);
    }



    private void backToLogin() {
        Stage loginStage = new Stage();
        Scene loginScene = LogInFormGUI.createLogInScene(loginStage);
        loginStage.setScene(loginScene);
        loginStage.show();
        primaryStage.close();
    }

    private void changeProfilePhoto(String username, String firstName, Circle photoCircle, Stage primaryStage) {
        String profileUrl;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selectați o fotografie");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            selectedPhoto = new javafx.scene.image.Image(selectedFile.toURI().toString());
            photoCircle.setFill(new javafx.scene.paint.ImagePattern(selectedPhoto));
        }

        if (selectedFile != null) {
            profileUrl = selectedFile.toURI().toString();
            selectedPhoto = new javafx.scene.image.Image(profileUrl);
            photoCircle.setFill(new javafx.scene.paint.ImagePattern(selectedPhoto));

            updateUserProfilePhoto(username, firstName, profileUrl);
        }
    }

    private void updateUserProfilePhoto(String lastName, String firstName, String newProfileUrl) {
        try {
            JSONArray usersArray = new JSONArray(FileUtils.readFileToString(new File("users.json"), "UTF-8"));

            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userObject = usersArray.getJSONObject(i);
                if (userObject.getString("lastName").equals(lastName) && userObject.getString("firstName").equals(firstName)) {
                    userObject.put("profilePhotoUrl", newProfileUrl);
                    break;
                }
            }

            try (FileWriter file = new FileWriter("users.json")) {
                file.write(usersArray.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createCalendarVBox() {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        GridPane calendarGrid = createCalendarGrid();
        calendarGrid.setAlignment(Pos.CENTER);
        header = createHeader();
        vbox.getChildren().addAll(header, calendarGrid);
        return vbox;
    }

    public Scene createScene(String profileUrl, String firstName, String lastName) {
        HBox mainContainer = new HBox(10);

        VBox greyContainer = createGreyContainerScene(profileUrl, firstName, lastName);

        VBox calendarVBox = createCalendarVBox();

        mainContainer.getChildren().addAll(greyContainer, calendarVBox);
        mainContainer.setStyle("-fx-background-color: #FFF0CE;");

        Scene scene = new Scene(mainContainer, 600, 400);
        scene.getStylesheets().add("styles.css");

        return scene;
    }

    private GridPane createCalendarGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);

        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayOfWeekLabel = new Label(daysOfWeek[i]);
            dayOfWeekLabel.setStyle("-fx-text-fill: #0C356A;");
            grid.add(dayOfWeekLabel, i, 0);
        }

        LocalDate firstDayOfMonth = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonthValue(), 1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int dayOfMonth = 1;
        for (int row = 1; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if ((row == 1 && col >= dayOfWeek) || (row > 1 && dayOfMonth <= daysInMonth)) {
                    VBox dayBox = new VBox(5);
                    dayBox.setAlignment(Pos.TOP_CENTER);

                    Label dayLabel = new Label(String.valueOf(dayOfMonth));
                    dayLabel.setStyle("-fx-text-fill: #0C356A;");

                    Pane eventPane = new Pane();
                    eventPane.setPrefSize(200, 100);
                    eventPane.setStyle("-fx-border-color: #0C356A; -fx-border-width: 1.5;");

                    if (LocalDate.now().getDayOfMonth() == dayOfMonth && currentYearMonth.getMonth() == LocalDate.now().getMonth()) {
                        eventPane.setStyle("-fx-border-color: #FFC436; -fx-border-width: 2;");

                    }

                    Task task = getTaskForDay(dayOfMonth, currentYearMonth.getMonthValue(), currentYearMonth.getYear());
                    if (task != null) {
                        List<String> eventList = task.getEventList();
                        if (eventList != null && !eventList.isEmpty()) {
                            eventList.sort((event1, event2) -> {
                                String startTime1 = event1.substring(event1.indexOf("(") + 1, event1.indexOf("-")).trim();
                                String startTime2 = event2.substring(event2.indexOf("(") + 1, event2.indexOf("-")).trim();
                                return LocalTime.parse(startTime1).compareTo(LocalTime.parse(startTime2));
                            });

                            Label eventsLabel = new Label(String.join("\n", eventList));
                            eventPane.getChildren().add(eventsLabel);
                        }
                    }

                    int finalDayOfMonth = dayOfMonth;
                    eventPane.setOnMouseClicked(event -> showAddEventPanel(finalDayOfMonth, currentYearMonth.getMonthValue(), currentYearMonth.getYear()));

                    List<String> overlappingEvents = checkForTaskOverlap(dayOfMonth, currentYearMonth.getMonthValue(), currentYearMonth.getYear());

                    if (!overlappingEvents.isEmpty()) {
                        dayLabel.setStyle("-fx-text-fill: red;");
                        String overlappingEventInfo = String.format("Evenimente suprapuse pe " + dayOfMonth + "-" + currentYearMonth + ": " + overlappingEvents);
                        if (!overlappingEventsTextArea.getText().contains(overlappingEventInfo)) {
                            overlappingEventsTextArea.clear();
                            overlappingEventsTextArea.appendText(overlappingEventInfo);
                        }
                    } else {
                        if (LocalDate.now().getDayOfMonth() == dayOfMonth && currentYearMonth.getMonth() == LocalDate.now().getMonth()){
                            dayLabel.setStyle("-fx-text-fill: #FFC436;");
                        }else{
                            dayLabel.setStyle("-fx-text-fill: #0C356A;");
                        }
                    }

                    dayBox.getChildren().addAll(dayLabel, eventPane);
                    grid.add(dayBox, col, row);

                    dayOfMonth++;
                }
            }
        }
        return grid;
    }

    //functie pt verificarea suprapunerii task-urilor
    private List<String> checkForTaskOverlap(int day, int month, int year) {
        List<String> overlappingEvents = new ArrayList<>();
        Task currentTask = getTaskForDay(day, month, year);

        if (currentTask != null) {
            List<String> eventList = currentTask.getEventList();
            if (eventList != null && eventList.size() > 1) {
                // Check for overlap in events on the same day
                for (int i = 0; i < eventList.size() - 1; i++) {
                    for (int j = i + 1; j < eventList.size(); j++) {

                        String event1 = eventList.get(i);
                        String event2 = eventList.get(i + 1);

                        findOverlappingEvents(overlappingEvents, event1, event2);

                    }

                }

            }
        }

        return overlappingEvents;
    }

    private void findOverlappingEvents(List<String> overlappingEvents, String event1, String event2) {
        LocalTime endTime1 = extractEndTime(event1);
        LocalTime startTime2 = extractStartTime(event2);

        if (endTime1.isAfter(startTime2)) {
            if (!overlappingEvents.contains(event1)) {
                overlappingEvents.add(event1);
            }
            if (!overlappingEvents.contains(event2)) {
                overlappingEvents.add(event2);
            }


        }
    }

    private LocalTime extractStartTime(String event) {
        String startTime = event.substring(event.indexOf("(") + 1, event.indexOf("-")).trim();
        return LocalTime.parse(startTime);
    }

    private LocalTime extractEndTime(String event) {
        String endTime = event.substring(event.indexOf("-") + 1, event.indexOf(")")).trim();
        return LocalTime.parse(endTime);
    }

    private Task getTaskForDay(int day, int month, int year) {
        for (Task task : tasks) {
            if (task.getDay() == day && task.getMonth() == month && task.getYear() == year) {
                return task;
            }
        }
        return null;
    }


    private HBox createHeader() {
        HBox header = new HBox(10);

        Image prev = new Image("assets/previous.png");
        ImageView prevView = new ImageView(prev);

        Button previousMonthButton = new Button();
        previousMonthButton.setGraphic(prevView);
        previousMonthButton.setStyle("-fx-background-color: transparent;");

        previousMonthButton.setOnAction(event -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar(); // Call updateCalendar here
        });

        monthYearLabel = new Label(currentYearMonth.getMonth() + " " + currentYearMonth.getYear());
        monthYearLabel.setStyle("-fx-text-fill: #0C356A;");

        Image next = new Image("assets/next.png");
        ImageView nextView = new ImageView(next);

        Button nextMonthButton = new Button();
        nextMonthButton.setGraphic(nextView);
        nextMonthButton.setStyle("-fx-background-color: transparent;");

        nextMonthButton.setOnAction(event -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        header.getChildren().addAll(previousMonthButton, monthYearLabel, nextMonthButton);

        return header;
    }

    private void updateTaskList(VBox taskListVBox, int day, int month, int year) {
        taskListVBox.getChildren().clear();

        Task task = getTaskForDay(day, month, year);
        if (task != null) {
            for (String event : task.getEventList()) {
                HBox taskBox = createTaskBox(event, day, month, year, taskListVBox);
                taskListVBox.getChildren().add(taskBox);
            }
        }

    }



    private void updateCalendar() {
        Stage stage = (Stage) monthYearLabel.getScene().getWindow();

        VBox mainContainer = (VBox) stage.getScene().getRoot();

        if (mainContainer.getChildren().size() >= 1 && mainContainer.getChildren().get(0) instanceof HBox) {
            HBox existingHBox = (HBox) mainContainer.getChildren().get(0);

            if (existingHBox.getChildren().size() >= 2) {
                Node secondChild = existingHBox.getChildren().get(1);

                if (secondChild instanceof VBox) {
                    VBox newCalendarVBox = createCalendarVBox();
                    existingHBox.getChildren().set(1, newCalendarVBox);

                    monthYearLabel.setText(currentYearMonth.getMonth() + " " + currentYearMonth.getYear());
                }
            }
        }
    }

    private void showAddEventPanel(int day, int month, int year) {
        Stage addEventStage = new Stage();
        addEventStage.setTitle("Adăugare Eveniment");

        VBox addEventVBox = new VBox(10);
        addEventVBox.setAlignment(Pos.CENTER);

        TextField eventNameField = new TextField();
        eventNameField.setPromptText("Numele evenimentului");

        TextField startTimeField = new TextField();
        startTimeField.setPromptText("Ora de început");

        TextField endTimeField = new TextField();
        endTimeField.setPromptText("Ora de sfârșit");
        Button saveButton = new Button("Salvare");


        Button toggleVisibilityButton = new Button("Adaugă eveniment");
        toggleVisibilityButton.setStyle("-fx-background-color: #0C356A; -fx-text-fill: #FFFFFF");
        toggleVisibilityButton.setOnAction(event -> {
            eventNameField.setVisible(!eventNameField.isVisible());
            startTimeField.setVisible(!startTimeField.isVisible());
            endTimeField.setVisible(!endTimeField.isVisible());
            saveButton.setVisible(!saveButton.isVisible());

        });


        //butonul de slavare task
        saveButton.setOnAction(event -> {
            String eventName = eventNameField.getText();
            String startTimeText = startTimeField.getText();
            String endTimeText = endTimeField.getText();

            try {
                LocalTime startTime = LocalTime.parse(startTimeText);
                LocalTime endTime = LocalTime.parse(endTimeText);

                String eventDetails = eventName + " (" + startTime + " - " + endTime + ")";
                saveTask(day, month, year, eventDetails);
                VBox taskListVBox = new VBox(10);
                taskListVBox.setAlignment(Pos.CENTER);
                taskListVBox.getChildren().clear();

                addEventVBox.getChildren().clear();
                addEventVBox.getChildren().addAll(eventNameField, startTimeField, endTimeField, toggleVisibilityButton, saveButton, taskListVBox);

                addEventStage.close();
            } catch (DateTimeParseException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Eroare");
                alert.setHeaderText("Formatul orelor este incorect");
                alert.setContentText("Vă rugăm să introduceți orele în formatul corect (HH:mm)");
                alert.showAndWait();
            }
        });

        VBox taskListVBox = new VBox(10);
        taskListVBox.setAlignment(Pos.CENTER);
        taskListVBox.getChildren().clear();

        updateTaskList(taskListVBox, day, month, year);

        eventNameField.setVisible(false);
        startTimeField.setVisible(false);
        endTimeField.setVisible(false);
        saveButton.setVisible(false);
        addEventVBox.getChildren().addAll(taskListVBox, eventNameField, startTimeField, endTimeField, toggleVisibilityButton, saveButton);

        Scene addEventScene = new Scene(addEventVBox, 400, 500);
        addEventScene.getRoot().setStyle("-fx-background-color: #0174BE;");

        addEventStage.setScene(addEventScene);

        addEventStage.show();
    }


    private String generateTaskIdentifier(String taskEvent, int day, int month, int year) {
        return taskEvent + "_" + day + "_" + month + "_" + year;
    }
    private HBox createTaskBox(String taskEvent, int day, int month, int year, VBox taskListVBox) {
        String taskIdentifier = generateTaskIdentifier(taskEvent, day, month, year);

        HBox taskBox = new HBox(10);
        taskBox.setAlignment(Pos.CENTER);

        // Crează un chenar pentru task
        HBox taskVBox = new HBox(5);
        taskVBox.setAlignment(Pos.CENTER);

        Label taskLabel = new Label(taskEvent);

        Image star = new Image("assets/star.png");
        ImageView starView = new ImageView(star);
        Image star2 = new Image("assets/star2.png");
        ImageView starView2 = new ImageView(star2);
        ToggleButton starButton = new ToggleButton();
        starButton.setGraphic(starView);
        starButton.setStyle("-fx-background-color: transparent;");

        ToggleGroup toggleGroup = new ToggleGroup();
        starButton.setToggleGroup(toggleGroup);

        boolean isStarSelected = taskButtonStates.getOrDefault(taskIdentifier, false);
        starButton.setSelected(isStarSelected);
        if (isStarSelected) {
            starButton.setGraphic(starView2);
        } else {
            starButton.setGraphic(starView);
        }

        starButton.setOnAction(event -> {
            taskButtonStates.put(taskIdentifier, starButton.isSelected());

            if (starButton.isSelected()) {
                starButton.setGraphic(starView2);
            } else {
                starButton.setGraphic(starView);
            }
        });

        Image delete = new Image("assets/remove.png");
        ImageView deleteView = new ImageView(delete);

        Button deleteButton = new Button();
        deleteButton.setGraphic(deleteView);

        // buton pentru stergerea unui task
        deleteButton.setStyle("-fx-background-color: transparent;");
        deleteButton.setOnAction(event -> {
            deleteTask(day, month, year, taskEvent);
            taskListVBox.getChildren().clear();
            updateTaskList(taskListVBox, day, month, year);
            updateCalendar();
        });

        taskLabel.setOnMouseClicked(event -> showEditTaskPanel(taskEvent, day, month, year));
        taskVBox.getChildren().add(taskLabel);
        taskVBox.setStyle("-fx-border-radius: 5; -fx-border-color: #FFF0CE; -fx-border-width: 2px;");

        taskBox.getChildren().addAll(starButton, taskVBox, deleteButton);

        return taskBox;
    }
    private void deleteTask(int day, int month, int year, String taskEvent) {
        Task task = getTaskForDay(day, month, year);
        if (task != null) {
            List<String> overlappingEvents = new ArrayList<>();

            if (task.getEventList().size() > 1) {
                for (int i = 0; i < task.getEventList().size() - 1; i++) {
                    for (int j = i + 1; j < task.getEventList().size(); j++) {
                        String event1 = task.getEventList().get(i);
                        String event2 = task.getEventList().get(j);
                        findOverlappingEvents(overlappingEvents, event1, event2);
                    }
                }
            }
            task.getEventList().remove(taskEvent);
            if (task.getEventList().isEmpty()) {
                tasks.remove(task);
            }

            saveTasks();
            updateCalendar();
            overlappingEventsTextArea.clear();

        }
    }
    private void showEditTaskPanel(String taskEvent, int day, int month, int year) {
        Stage editTaskStage = new Stage();
        editTaskStage.setTitle("Editare Eveniment");

        VBox editTaskVBox = new VBox(10);
        editTaskVBox.setAlignment(Pos.CENTER);

        // eveniment = nume (orastart - orasfarsit)
        String[] eventParts = taskEvent.split(" \\(| - |\\)");
        String eventName = eventParts[0];
        String startTime = eventParts[1];
        String endTime = eventParts[2];

        Label eventNameLabel = new Label("Numele evenimentului:");
        TextField eventNameField = new TextField();
        eventNameField.setPromptText("Numele evenimentului");
        eventNameField.setText(eventName);

        Label startTimeLabel = new Label("Ora de început:");
        TextField startTimeField = new TextField();
        startTimeField.setPromptText("HH:mm");
        startTimeField.setText(startTime);

        Label endTimeLabel = new Label("Ora de sfârșit:");
        TextField endTimeField = new TextField();
        endTimeField.setPromptText("HH:mm");
        endTimeField.setText(endTime);

        //butonul de salvare modificari aduse unui task
        Button saveChangesButton = new Button("Salvare");
        editTaskVBox.getChildren().addAll(eventNameLabel, eventNameField, startTimeLabel, startTimeField, endTimeLabel, endTimeField, saveChangesButton);

        saveChangesButton.setOnAction(event -> {
            String editedEventName = eventNameField.getText();
            String editedStartTime = startTimeField.getText();
            String editedEndTime = endTimeField.getText();

            Task task = getTaskForDay(day, month, year);

            if (task != null) {
                List<String> eventList = task.getEventList();
                eventList.remove(taskEvent);
                String editedEvent = editedEventName + " (" + editedStartTime + " - " + editedEndTime + ")";
                eventList.add(editedEvent);

                saveTasks();

                updateCalendar();

            }

            editTaskStage.close();
        });


        Scene editTaskScene = new Scene(editTaskVBox, 300, 200);
        editTaskStage.setScene(editTaskScene);
        editTaskScene.getRoot().setStyle("-fx-background-color: #FFC436;");

        editTaskStage.show();
    }

}
