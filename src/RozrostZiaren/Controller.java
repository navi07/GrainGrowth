package RozrostZiaren;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public TextField tf_rows;

    @FXML
    public TextField tf_columns;

    @FXML
    public Button button_draw;

    @FXML
    public TextField tf_cells;

    @FXML
    public Button button_pause;

    @FXML
    public ComboBox cb_method;

    @FXML
    public ComboBox cb_generatorType;

    @FXML
    public ComboBox cb_boundaryCondition;

    @FXML
    public Canvas canvas;

    @FXML
    public TextField tf_generations;

    @FXML
    public Button button_set;

    @FXML
    public TextField tf_animationLength;

    private GraphicsContext gc;

    private Timeline timeline;

    private static int rows;
    private static int columns;
    private int cells;

    private double animationLength;
    private static double width;
    private static double height;

    private static boolean isBoundaryCondition;

    private static Cell[][] cell;

    private int method_num;
    private int generatorType_num;
    private int generations;


    private ObservableList<String> methodList = FXCollections.observableArrayList("Von Neumann",
            "Moore", "Hexagonal left", "Hexagonal right", "Hexagonal random", "Pentagonal random");

    private ObservableList<String> boundaryConditionsList = FXCollections.observableArrayList("Periodic",
            "Non periodic");

    private ObservableList<String> generatorTypeList = FXCollections.observableArrayList("Random",
            "Evenly", "By click");

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        cb_method.setItems(methodList);
        cb_boundaryCondition.setItems(boundaryConditionsList);
        cb_generatorType.setItems(generatorTypeList);

        tf_rows.setText("200");
        tf_columns.setText("200");
        tf_cells.setText("2000");
        tf_generations.setText("100");
        tf_animationLength.setText("0.1");

        cb_method.getSelectionModel().select(0);
        cb_boundaryCondition.getSelectionModel().select(0);
        cb_generatorType.getSelectionModel().select(0);

        isBoundaryCondition = false;
        method_num = 0;
        generatorType_num = 0;
    }

    @FXML
    public void handleButtonSet(ActionEvent actionEvent) {
        setData();
        cell = new Cell[columns][rows];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                cell[i][j] = new Cell();
            }
        }

        gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawMesh(gc);

        if (generatorType_num == 1) {
            cells = Integer.parseInt(tf_cells.getText());
            generateRandom(cells);
            colourCells(gc);
        } else if (generatorType_num == 2) {
            generateEvenly();
        } else if (generatorType_num == 3) {
            generateByClick();
        }
    }

    @FXML
    public void handleButtonDraw(ActionEvent actionEvent) {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(animationLength), e -> {
            nextGeneration();
            colourCells(gc);
        });

        timeline = new Timeline(keyFrame);
        timeline.setCycleCount(generations);
        timeline.play();
    }

    @FXML
    public void handleButtonPause(ActionEvent actionEvent) {
        timeline.stop();
    }

    private void nextGeneration() {
        Cell[][] tmpCells = new Cell[columns][rows];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                tmpCells[i][j] = new Cell();
            }
        }

        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (cell[i][j].isAlive()) {
                    if (method_num == 1) {
                        vonNeumann(i, j, tmpCells);
                    } else if (method_num == 2) {
                        moore(i, j, tmpCells);
                    } else if (method_num == 3) {
                        hexagonalLeft(i, j, tmpCells);
                    } else if (method_num == 4) {
                        hexagonalRight(i, j, tmpCells);
                    } else if (method_num == 5) {
                        Random random = new Random();
                        if (random.nextBoolean()) {
                            hexagonalRight(i, j, tmpCells);
                        } else {
                            hexagonalLeft(i, j, tmpCells);
                        }
                    } else if (method_num == 6) {
                        Random rand = new Random();
                        Integer randomNumber = rand.nextInt(4) + 1;
                        if (randomNumber.equals(1)) {
                            pentagonalUp(i, j, tmpCells);
                        } else if (randomNumber.equals(2)) {
                            pentagonalDown(i, j, tmpCells);
                        } else if (randomNumber.equals(3)) {
                            pentagonalLeft(i, j, tmpCells);
                        } else if (randomNumber.equals(4)) {
                            pentagonalRight(i, j, tmpCells);
                        }
                    }
                }
            }
        }

        cell = tmpCells;
    }

    private void pentagonalRight(int x, int y, Cell[][] tmp) {
        tmp[x][y].setAlive();
        tmp[x][y].setColor(cell[x][y].getColor());
        int tmp_i, tmp_j;
        for (int i = x - 1; i < (x + 2); i++) {
            for (int j = y - 1; j < (y + 2); j++) {
                BoundaryConditionProcessor boundaryConditionProcessor = new BoundaryConditionProcessor(i, j).invoke();
                tmp_i = boundaryConditionProcessor.getTmp_i();
                tmp_j = boundaryConditionProcessor.getTmp_j();
                try {
                    if (tmp_i == x && tmp_j == y) continue;
                    if (i == x + 1 && j == y || i == x + 1 && j == y - 1 || i == x + 1 && j == y + 1 || i == x && j == y - 1 || i == x && j == y + 1) {
                        if (!tmp[tmp_i][tmp_j].isAlive()) {
                            tmp[tmp_i][tmp_j].setAlive();
                            tmp[tmp_i][tmp_j].setColor(tmp[x][y].getColor());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void pentagonalLeft(int x, int y, Cell[][] tmp) {
        tmp[x][y].setAlive();
        tmp[x][y].setColor(cell[x][y].getColor());
        int tmp_i, tmp_j;
        for (int i = x - 1; i < (x + 2); i++) {
            for (int j = y - 1; j < (y + 2); j++) {
                BoundaryConditionProcessor boundaryConditionProcessor = new BoundaryConditionProcessor(i, j).invoke();
                tmp_i = boundaryConditionProcessor.getTmp_i();
                tmp_j = boundaryConditionProcessor.getTmp_j();
                try {
                    if (tmp_i == x && tmp_j == y) continue;
                    if (i == x - 1 && j == y || i == x - 1 && j == y - 1 || i == x - 1 && j == y + 1 || i == x && j == y - 1 || i == x && j == y + 1) {
                        if (!tmp[tmp_i][tmp_j].isAlive()) {
                            tmp[tmp_i][tmp_j].setAlive();
                            tmp[tmp_i][tmp_j].setColor(tmp[x][y].getColor());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void pentagonalDown(int x, int y, Cell[][] tmp) {
        tmp[x][y].setAlive();
        tmp[x][y].setColor(cell[x][y].getColor());
        int tmp_i, tmp_j;
        for (int i = x - 1; i < (x + 2); i++) {
            for (int j = y - 1; j < (y + 2); j++) {
                BoundaryConditionProcessor boundaryConditionProcessor = new BoundaryConditionProcessor(i, j).invoke();
                tmp_i = boundaryConditionProcessor.getTmp_i();
                tmp_j = boundaryConditionProcessor.getTmp_j();
                try {
                    if (tmp_i == x && tmp_j == y) continue;
                    if (i == x + 1 && j == y || i == x - 1 && j == y || i == x && j == y - 1 || i == x + 1 && j == y - 1 || i == x - 1 && j == y - 1) {
                        if (!tmp[tmp_i][tmp_j].isAlive()) {
                            tmp[tmp_i][tmp_j].setAlive();
                            tmp[tmp_i][tmp_j].setColor(tmp[x][y].getColor());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void pentagonalUp(int x, int y, Cell[][] tmp) {
        tmp[x][y].setAlive();
        tmp[x][y].setColor(cell[x][y].getColor());
        int tmp_i, tmp_j;
        for (int i = x - 1; i < (x + 2); i++) {
            for (int j = y - 1; j < (y + 2); j++) {
                BoundaryConditionProcessor boundaryConditionProcessor = new BoundaryConditionProcessor(i, j).invoke();
                tmp_i = boundaryConditionProcessor.getTmp_i();
                tmp_j = boundaryConditionProcessor.getTmp_j();
                try {
                    if (tmp_i == x && tmp_j == y) continue;
                    if (i == x + 1 && j == y || i == x - 1 && j == y || i == x && j == y + 1 || i == x + 1 && j == y + 1 || i == x - 1 && j == y + 1) {
                        if (!tmp[tmp_i][tmp_j].isAlive()) {
                            tmp[tmp_i][tmp_j].setAlive();
                            tmp[tmp_i][tmp_j].setColor(tmp[x][y].getColor());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void hexagonalLeft(int x, int y, Cell[][] tmp) {
        tmp[x][y].setAlive();
        tmp[x][y].setColor(cell[x][y].getColor());
        int tmp_i, tmp_j;
        for (int i = x - 1; i < (x + 2); i++) {
            for (int j = y - 1; j < (y + 2); j++) {
                BoundaryConditionProcessor boundaryConditionProcessor = new BoundaryConditionProcessor(i, j).invoke();
                tmp_i = boundaryConditionProcessor.getTmp_i();
                tmp_j = boundaryConditionProcessor.getTmp_j();
                try {
                    if (tmp_i == x && tmp_j == y) continue;
                    if (i == x && j == y - 1 || i == x && j == y + 1 || i == x + 1 && j == y || i == x - 1 && j == y || i == x - 1 && j == y - 1 || i == x + 1 && j == y + 1) {
                        if (!tmp[tmp_i][tmp_j].isAlive()) {
                            tmp[tmp_i][tmp_j].setAlive();
                            tmp[tmp_i][tmp_j].setColor(tmp[x][y].getColor());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void hexagonalRight(int x, int y, Cell[][] tmp) {
        tmp[x][y].setAlive();
        tmp[x][y].setColor(cell[x][y].getColor());
        int tmp_i, tmp_j;
        for (int i = x - 1; i < (x + 2); i++) {
            for (int j = y - 1; j < (y + 2); j++) {
                BoundaryConditionProcessor boundaryConditionProcessor = new BoundaryConditionProcessor(i, j).invoke();
                tmp_i = boundaryConditionProcessor.getTmp_i();
                tmp_j = boundaryConditionProcessor.getTmp_j();
                try {
                    if (tmp_i == x && tmp_j == y) continue;
                    if (i == x && j == y - 1 || i == x && j == y + 1 || i == x + 1 && j == y || i == x - 1 && j == y || i == x - 1 && j == y + 1 || i == x + 1 && j == y - 1) {
                        if (!tmp[tmp_i][tmp_j].isAlive()) {
                            tmp[tmp_i][tmp_j].setAlive();
                            tmp[tmp_i][tmp_j].setColor(tmp[x][y].getColor());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void moore(int x, int y, Cell[][] tmp) {
        tmp[x][y].setAlive();
        tmp[x][y].setColor(cell[x][y].getColor());
        int tmp_i, tmp_j;
        for (int i = x - 1; i < (x + 2); i++) {
            for (int j = y - 1; j < (y + 2); j++) {
                BoundaryConditionProcessor boundaryConditionProcessor = new BoundaryConditionProcessor(i, j).invoke();
                tmp_i = boundaryConditionProcessor.getTmp_i();
                tmp_j = boundaryConditionProcessor.getTmp_j();
                try {
                    if (tmp_i == x && tmp_j == y) continue;
                    if (!tmp[tmp_i][tmp_j].isAlive()) {
                        tmp[tmp_i][tmp_j].setAlive();
                        tmp[tmp_i][tmp_j].setColor(tmp[x][y].getColor());
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void vonNeumann(int x, int y, Cell[][] tmp) {
        tmp[x][y].setAlive();
        tmp[x][y].setColor(cell[x][y].getColor());
        int tmp_i, tmp_j;
        for (int i = x - 1; i < (x + 2); i++) {
            for (int j = y - 1; j < (y + 2); j++) {
                BoundaryConditionProcessor boundaryConditionProcessor = new BoundaryConditionProcessor(i, j).invoke();
                tmp_i = boundaryConditionProcessor.getTmp_i();
                tmp_j = boundaryConditionProcessor.getTmp_j();
                try {
                    if (tmp_i == x && tmp_j == y) continue;
                    if (i == x + 1 && j == y || i == x - 1 && j == y || i == x && j == y - 1 || i == x && j == y + 1) {
                        if (!tmp[tmp_i][tmp_j].isAlive()) {
                            tmp[tmp_i][tmp_j].setAlive();
                            tmp[tmp_i][tmp_j].setColor(tmp[x][y].getColor());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void generateByClick() {
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            double x = event.getX();
            double y = event.getY();
            printCell(x, y);
        });
    }

    private void printCell(double x_pos, double y_pos) {
        Cell[][] tmp = new Cell[columns][rows];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                tmp[i][j] = new Cell();
            }
        }
        int index_i = -2;
        int index_j = -2;
        for (int i = 0; i <= columns; i++) {
            if (x_pos < width * i) {
                for (int j = 0; j <= rows; j++) {
                    if (y_pos < height * j) {
                        double r = Math.random();
                        double g = Math.random();
                        double b = Math.random();

                        tmp[i - 1][j - 1].setAlive();
                        tmp[i - 1][j - 1].setColor(Color.color(r, g, b));
                        index_i = i - 1;
                        index_j = j - 1;
                        break;
                    }
                }
                break;
            }
        }
        if ((index_i > -2) && (index_j > -2)) {
            cell[index_i][index_j] = tmp[index_i][index_j];
            colourCells(gc);
        }
    }

    private void generateEvenly() {
        int step = cells;

        for (int i = 0; i < columns; i += step + 1) {
            for (int j = 0; j < rows; j += step + 1) {
                if (!cell[i][j].isAlive()) {
                    double r = Math.random();
                    double g = Math.random();
                    double b = Math.random();
                    cell[i][j].setAlive();
                    cell[i][j].setColor(Color.color(r, g, b));
                }
            }
        }
        colourCells(gc);
    }

    private void colourCells(GraphicsContext gc) {
        drawMesh(gc);
        gc.beginPath();
        gc.setLineWidth(1);
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (cell[i][j].isAlive()) {
                    gc.setFill(cell[i][j].getColor());
                    gc.fillRect(width * i + 1, height * j + 1, width - 1, height - 1);
                } else {
                    gc.setFill(Color.WHITE);
                    gc.fillRect(width * i + 1, height * j + 1, width - 1, height - 1);
                }
            }
        }
        gc.fill();
        gc.closePath();
    }

    private void generateRandom(int cells) {
        Random random = new Random();
        int x_pos;
        int y_pos;

        for (int iterator = 0; iterator < cells; iterator++) {
            double r = Math.random();
            double g = Math.random();
            double b = Math.random();
            x_pos = random.nextInt(columns);
            y_pos = random.nextInt(rows);
            cell[x_pos][y_pos].setAlive();
            cell[x_pos][y_pos].setColor(Color.color(r, g, b));
        }
    }

    private void setData() {
        if (!validInputData()) {
            showErrorAlert();
        } else {
            String method = cb_method.getSelectionModel().getSelectedItem().toString();
            String boundaryCondition = cb_boundaryCondition.getSelectionModel().getSelectedItem().toString();
            String generatorType = cb_generatorType.getSelectionModel().getSelectedItem().toString();

            if (boundaryCondition.equals("Periodic")) {
                isBoundaryCondition = true;
            } else if (boundaryCondition.equals("Non periodic")) {
                isBoundaryCondition = false;
            }

            rows = Integer.parseInt(tf_rows.getText());
            columns = Integer.parseInt(tf_columns.getText());
            cells = Integer.parseInt(tf_cells.getText());
            generations = Integer.parseInt(tf_generations.getText());
            animationLength = Double.parseDouble(tf_animationLength.getText());

            switch (method) {
                case "Von Neumann":
                    method_num = 1;
                    break;
                case "Moore":
                    method_num = 2;
                    break;
                case "Hexagonal left":
                    method_num = 3;
                    break;
                case "Hexagonal right":
                    method_num = 4;
                    break;
                case "Hexagonal random":
                    method_num = 5;
                    break;
                case "Pentagonal random":
                    method_num = 6;
                    break;
            }

            switch (generatorType) {
                case "Random":
                    generatorType_num = 1;
                    break;
                case "Evenly":
                    generatorType_num = 2;
                    break;
                case "By click":
                    generatorType_num = 3;
                    break;
            }
        }
    }

    private void drawMesh(GraphicsContext gc) {
        width = gc.getCanvas().getWidth() / columns;
        height = gc.getCanvas().getHeight() / rows;
        gc.beginPath();
        gc.setLineWidth(1);
        gc.setStroke(Color.BLACK);

        for (int i = 0; i < columns; i++) {
            gc.moveTo(width * i, 0);
            gc.lineTo(width * i, gc.getCanvas().getHeight());
        }
        for (int i = 0; i < rows; i++) {
            gc.moveTo(0, height * i);
            gc.lineTo(gc.getCanvas().getWidth(), height * i);
        }
        gc.stroke();
        gc.closePath();
    }

    private Boolean validInputData() {
        return !cb_method.getSelectionModel().isEmpty() &&
                !cb_boundaryCondition.getSelectionModel().isEmpty() &&
                !cb_generatorType.getSelectionModel().isEmpty() &&
                !tf_rows.getText().equals("") && !tf_columns.getText().equals("") &&
                !tf_cells.getText().equals("") && !tf_generations.getText().equals("") &&
                !tf_animationLength.getText().equals("");
    }

    private void showErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Empty input data");
        alert.setHeaderText(null);
        alert.setContentText("Input all data !");

        alert.showAndWait();
    }

    static class BoundaryConditionProcessor {
        private int i;
        private int j;
        private int tmp_i;
        private int tmp_j;

        BoundaryConditionProcessor(int i, int j) {
            this.i = i;
            this.j = j;
        }

        int getTmp_i() {
            return tmp_i;
        }

        int getTmp_j() {
            return tmp_j;
        }

        BoundaryConditionProcessor invoke() {
            tmp_i = i;
            tmp_j = j;
            if (isBoundaryCondition) {
                if (i == -1) tmp_i = columns - 1;
                if (i == columns) tmp_i = 0;
                if (j == -1) tmp_j = rows - 1;
                if (j == rows) tmp_j = 0;
            } else {
                if (i == -1) tmp_i = 0;
                if (i == columns) tmp_i = columns;
                if (j == -1) tmp_j = 0;
                if (j == rows) tmp_j = rows;
            }
            return this;
        }
    }
}
