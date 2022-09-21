package com.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

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

    private File[] pathSort(File[] someArray) {
        Comparator<File> pathComparator = ((o1, o2) -> {
            String[] firstObjectArray = o1.getName().split("[^\\d\\.]+");
            String[] secondObjectArray = o2.getName().split("[^\\d\\.]+");
            ArrayList<Double> firstObjectList = stringArrayToDoubleList(firstObjectArray);
            ArrayList<Double> secondObjectList = stringArrayToDoubleList(secondObjectArray);
            for (int i = 0; i < firstObjectList.size(); i++) {
                Integer firstElement = Math.toIntExact((long) (firstObjectList.get(i) * 1000));
                Integer secondElement = Math.toIntExact((long) (secondObjectList.get(i) * 1000));
                if (firstElement - secondElement != 0) {
                    return firstElement - secondElement;
                }

            }
            return 0;
        });
        Arrays.sort(someArray, pathComparator);
        return someArray;
    }

//    private File[] imageSort(File[] imageFiles) {
//        Arrays.sort(imageFiles, (f1, f2) -> {
//            try {
//                int i1 = Integer.parseInt(String.valueOf(Arrays.stream(f1.getName().
//                        split("\\.")).toList().get(0)));
//                int i2 = Integer.parseInt(String.valueOf(Arrays.stream(f2.getName().split("\\.")).toList().get(0)));
//                return i1 - i2;
//            } catch (NumberFormatException e) {
//                throw new AssertionError(e);
//            }
//        });
//        return imageFiles;
//    }

    @FXML
    void convert(ActionEvent event) {


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Document document = new Document(PageSize.A3, 0, 0, 0, 0);

                document.addAuthor("Some Author");
                if (!nameOfDocumentField.getCharacters().toString().equals(""))
                    document.addTitle(nameOfDocumentField.getCharacters().toString());
                try {
                    if (!nameOfDocumentField.getCharacters().toString().equals(""))
                        PdfWriter.getInstance(document, new FileOutputStream(mainDirectory.getParent() + "/" + nameOfDocumentField.getCharacters().toString() + ".pdf"));
                    else{
                        PdfWriter.getInstance(document, new FileOutputStream(mainDirectory.getParent() + "/" + mainDirectory.getName() + ".pdf"));
                    }
                } catch (DocumentException | FileNotFoundException e) {
                    logLabel.setText("Не найден документ (хотя должен находиться)");
                }
                document.open();
                File[] arrayOfDirectories = mainDirectory.listFiles();
                arrayOfDirectories = pathSort(arrayOfDirectories);
                int i = 0;
                for (File path : arrayOfDirectories) {
                    i++;
                    convertProgressBar.setProgress(((double) i / (double) arrayOfDirectories.length));
//            System.out.println(path.getName());
                    if (path.isDirectory()) {
                        File[] imageFiles = pathSort(path.listFiles());
                        for (File imageFile : imageFiles) {
//                    System.out.println(imageFile.getName());
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
                    }
                }
                document.close();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
