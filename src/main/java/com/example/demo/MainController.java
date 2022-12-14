package com.example.demo;


import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MainController {

    File mainDirectory;

    @FXML
    private ProgressBar convertProgressBar;
    @FXML
    private Label logLabel;

    @FXML
    private Label pathLabel;

    @FXML
    private TextField nameOfDocumentField;

    @FXML
    private Button startConvertButton;

    private void imageFilter(File imageFile) throws IOException {
        BufferedImage source = ImageIO.read(imageFile);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());

        // Делаем двойной цикл, чтобы обработать каждый пиксель
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                double Amount = 0.999;
                // Получаем цвет текущего пикселя
                Color color = new Color(source.getRGB(x, y));

                // Получаем каналы этого цвета
                int blue = color.getBlue();
                int red = color.getRed();
                int green = color.getGreen();

                int redCoef = (int) ((Math.abs(127 - red) * Amount) / 255);
                int greenCoef = (int) ((Math.abs(127 - green) * Amount) / 255);
                int blueCoef = (int) ((Math.abs(127 - blue) * Amount) / 255);
                int newRed;
                int newGreen;
                int newBlue;
                if (red > 127) {
                    newRed = red + redCoef;
                } else {
                    newRed = red - redCoef;
                }

                if (green > 127) {
                    newGreen = green + greenCoef;
                } else {
                    newGreen = green - greenCoef;
                }
                if (blue > 127) {
                    newBlue = blue + blueCoef;
                } else {
                    newBlue = blue - blueCoef;
                }

                //  Cоздаем новый цвет
                Color newColor = new Color(newRed, newGreen, newBlue);

                // И устанавливаем этот цвет в текущий пиксель результирующего изображения
                result.setRGB(x, y, newColor.getRGB());
            }
        }

        // Созраняем результат в новый файл
        File output = new File(imageFile.getParent() + "/gg_" + imageFile.getName());
        ImageIO.write(result, "png", output);
//        System.out.println(imageFile.getName());
    }

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

    public void setTextLogLabel(String text) {
        Platform.runLater(() -> {
            logLabel.setText(text);
            convertProgressBar.setProgress(0);
        });
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
                setTextLogLabel("В папке нет нужной структуры директорий и файлов");
            }
            return 0;
        });
        Arrays.sort(someArray, pathComparator);
        return someArray;
    }

    private File findFirstImage(File[] imageArray) {
        fileSort(imageArray);
        return imageArray[0];
    }

    private Image fixFirstPage(File[] arrayOfDirectories) {
        try {
            return Image.getInstance(findFirstImage(arrayOfDirectories[0].listFiles()).getAbsolutePath());
        } catch (BadElementException | IOException e) {
            setTextLogLabel("Ошибка первой страницы");
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
            setTextLogLabel("Не найден документ (хотя должен находиться)");
        }
    }

    private void stepConvert(File imageFile, Document document) {
        try {
//            imageFilter(imageFile);
            if (imageFile.getName().contains(".jpeg") || imageFile.getName().contains(".jpg") || imageFile.getName().contains(".png")) {
//                System.out.println(imageFile.getName());
                Image image = Image.getInstance(imageFile.getAbsolutePath());

                image.setInterpolation(true);
                document.add(image);
                document.setPageSize(new Rectangle(image.getWidth(), image.getHeight()));
            }
        } catch (DocumentException | IOException e) {
            setTextLogLabel("Ошибка при конвертировании");
        }
    }

    private void addJpegsToDocument(File[] arrayOfDirectories, Document document) {
        int i = 0;
        for (File path : arrayOfDirectories) { //проходимся по папкам с главами
            i++;
            if (path.isDirectory()) {
                File[] imageFiles = fileSort(path.listFiles());
                for (File imageFile : imageFiles) { //проходимся по картинкам
                    stepConvert(imageFile, document); //добавляем картинку в документ, устанавливаем размеры страницы
                }
                convertProgressBar.setProgress(((double) i / (double) arrayOfDirectories.length)); //для строки прогресса
            }
        }
    }

    @FXML
    void convert(ActionEvent event) {
        Runnable runnable = () -> {
            File[] arrayOfDirectories = mainDirectory.listFiles();
            fileSort(arrayOfDirectories);
            Image firstPage = fixFirstPage(arrayOfDirectories);
            Document document = new Document(new Rectangle(firstPage.getWidth(), firstPage.getHeight()), 0, 0, 0, 0);
            initializeDocument(document);
            document.open();
            addJpegsToDocument(arrayOfDirectories, document);
            document.close();
            setTextLogLabel("Успешно конвертировано!");
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

}
