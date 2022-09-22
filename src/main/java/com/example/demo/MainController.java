package com.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainController {

    File mainDirectory;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ProgressBar convertProgressBar;

    @FXML
    private Button directoryChoseButton;

    @FXML
    private Label logLabel;

    @FXML
    private Label pathLabel;

    @FXML
    private TextField nameOfDocumentField;

    @FXML
    private Button startConvertButton;

    @FXML
    void initialize() {
    }

    @FXML
    void choseDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("../../../../"));
        directoryChooser.setTitle("Open directory");
        mainDirectory = directoryChooser.showDialog(new Stage());
        if (mainDirectory != null) {
            pathLabel.setText(mainDirectory.getAbsolutePath());
            startConvertButton.setDisable(false);
        }
    }

    private ArrayList<Double> stringArrayToDoubleList(String[] array) {
        ArrayList<Double> list = new ArrayList<>();
        for (String str : array) {
            if (!str.equals("") && !str.equals(".")) {
                list.add(Double.parseDouble(str));
            }
        }
        return list;
    }

    private File[] fileSort(File[] someArray) {
        Comparator<File> pathComparator = ((o1, o2) -> {
            String[] firstObjectArray = o1.getName().split("[^\\d\\.]+"); //составить массивы номеров томов, глав и тд для сортировки
            String[] secondObjectArray = o2.getName().split("[^\\d\\.]+"); //
            ArrayList<Double> firstObjectList = stringArrayToDoubleList(firstObjectArray); //конвертация массивов строк в лист нецелых чисел
            ArrayList<Double> secondObjectList = stringArrayToDoubleList(secondObjectArray); //
            try {
                for (int i = 0; i < firstObjectList.size(); i++) { //проход по томам, главам и тд
                    Integer firstElement = Math.toIntExact((long) (firstObjectList.get(i) * 1000));  //перевод в целые числа
                    Integer secondElement = Math.toIntExact((long) (secondObjectList.get(i) * 1000)); //
                    if (firstElement - secondElement != 0) {
                        return firstElement - secondElement; //сравнение
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        logLabel.setText("В папке нет нужной структуры директорий и файлов");
                        convertProgressBar.setProgress(0);
                    }
                });
            }
            return 0;
        });
        Arrays.sort(someArray, pathComparator);
        return someArray;
    }

    private File findFirstImage(File[] imageArray) {
        for (File image : imageArray) {
            if (image.getName().equals("0.jpeg")) {
                return image;
            }
        }
        return null;
    }

    private Image fixFirstPage(File[] arrayOfDirectories) {
        try {
            Image firstPage = Image.getInstance(findFirstImage(arrayOfDirectories[0].listFiles()).getAbsolutePath());
            return firstPage;
        } catch (BadElementException | IOException e) {
            logLabel.setText("Ошибка первой страницы");
        }
        return null;
    }

    private void initializeDocument(Document document) {
        document.addAuthor("Some Author");
        if (!nameOfDocumentField.getCharacters().toString().equals(""))
            document.addTitle(nameOfDocumentField.getCharacters().toString());
        try {
            if (!nameOfDocumentField.getCharacters().toString().equals(""))
                PdfWriter.getInstance(document, new FileOutputStream(mainDirectory.getParent() + "/" + nameOfDocumentField.getCharacters().toString() + ".pdf"));
            else {
                PdfWriter.getInstance(document, new FileOutputStream(mainDirectory.getParent() + "/" + mainDirectory.getName() + ".pdf"));
            }
        } catch (DocumentException | FileNotFoundException e) {
            logLabel.setText("Не найден документ (хотя должен находиться)");
        }
    }

    private void addJpegsToDocument(File[] arrayOfDirectories, Document document) {
        int i = 0;
        for (File path : arrayOfDirectories) { //проходимся по папкам с главами
            i++;
            if (path.isDirectory()) {
                File[] imageFiles = fileSort(path.listFiles());
                for (File imageFile : imageFiles) { //проходимся по картинкам
                    try {
                        if (imageFile.getName().contains(".jpeg")) {
                            Image image = Image.getInstance(imageFile.getAbsolutePath());
                            document.add(image);
                            document.setPageSize(new Rectangle(image.getWidth(), image.getHeight()));
                        }
                    } catch (DocumentException | IOException e) {
                        logLabel.setText("Произошло какое-то дерьмо");
                    }
                }
                convertProgressBar.setProgress(((double) i / (double) arrayOfDirectories.length)); //для прогрессбара
            }
        }
    }

    @FXML
    void convert(ActionEvent event) {
        Runnable runnable = () -> {
            File[] arrayOfDirectories = mainDirectory.listFiles();
            arrayOfDirectories = fileSort(arrayOfDirectories);
            Image firstPage = fixFirstPage(arrayOfDirectories);
            Document document = new Document(new Rectangle(firstPage.getWidth(), firstPage.getHeight()), 0, 0, 0, 0);
            initializeDocument(document);
            document.open();
            addJpegsToDocument(arrayOfDirectories, document);
            document.close();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    logLabel.setText("Успешно конвертировано!");
                    convertProgressBar.setProgress(0);
                }
            });
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

}
