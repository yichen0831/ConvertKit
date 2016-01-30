package com.ychstudio.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConvertKitController implements Initializable {

    @FXML
    Label selectedFileLabel;

    @FXML
    RadioButton gbkRadioButton;

    @FXML
    RadioButton big5RadioButton;

    @FXML
    RadioButton utf8RadioButton;

    @FXML
    ComboBox<String> encodingComboBox;

    @FXML
    TextArea inputTextArea;

    @FXML
    TextArea outputTextArea;

    private Optional<File> selectedFile = Optional.empty();
    private String prevEncoding;

    EventHandler<ActionEvent> changeSourceEncodingHandler = event -> {
        if (event.getSource() == gbkRadioButton) {
            encodingComboBox.setValue("GBK");
        }
        else if (event.getSource() == big5RadioButton) {
            encodingComboBox.setValue("Big5");
        }
        else if (event.getSource() == utf8RadioButton) {
            encodingComboBox.setValue("UTF-8");
        }
        else if (event.getSource() == encodingComboBox) {
            switch (encodingComboBox.getValue()) {
                case "GBK":
                    gbkRadioButton.setSelected(true);
                    break;
                case "Big5":
                    big5RadioButton.setSelected(true);
                    break;
                case "UTF-8":
                    utf8RadioButton.setSelected(true);
                    break;
                default:
                    gbkRadioButton.setSelected(false);
                    big5RadioButton.setSelected(false);
                    utf8RadioButton.setSelected(false);
                    break;
            }
            changeInputEncoding(encodingComboBox.getValue());
            prevEncoding = encodingComboBox.getValue();
        }

    };

    public void changeInputEncoding(String encoding) {
        if (selectedFile.isPresent()) {
            loadFileWithEncoding(selectedFile.get(), encoding);
        }
    }

    public String detectFileEncoding(File file) {
        UniversalDetector universalDetector = new UniversalDetector(null);

        int nread;
        byte[] buffer = new byte[2048];

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            while((nread = bis.read(buffer)) > 0 && !universalDetector.isDone()) {
                universalDetector.handleData(buffer, 0, nread);
            }
            universalDetector.dataEnd();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Optional<String> encoding = Optional.ofNullable(universalDetector.getDetectedCharset());
        return encoding.isPresent() ? encoding.get() : "UTF-8";
    }

    protected void loadFileWithEncoding(File file, String encoding) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }

            inputTextArea.clear();
            inputTextArea.setText(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void chooseFile() {

        FileChooser fileChooser = new FileChooser();
        selectedFile = Optional.ofNullable(fileChooser.showOpenDialog(null));
        if (selectedFile.isPresent()) {
            File file = selectedFile.get();
            selectedFileLabel.setText(file.getName());

            String encoding = detectFileEncoding(file);

            switch (encoding) {
                case "GBK":
                    gbkRadioButton.setSelected(true);
                    break;
                case "Big5":
                    big5RadioButton.setSelected(true);
                    break;
                case "UTF-8":
                    utf8RadioButton.setSelected(true);
                    break;
                default:
                    gbkRadioButton.setSelected(false);
                    big5RadioButton.setSelected(false);
                    utf8RadioButton.setSelected(false);
                    break;
            }
            prevEncoding = encoding;
            encodingComboBox.setValue(encoding);

            loadFileWithEncoding(file, encoding);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> encodingChoiceList = FXCollections.observableArrayList();
        encodingChoiceList.addAll(Charset.availableCharsets().keySet());
        encodingComboBox.setItems(encodingChoiceList);

        gbkRadioButton.setOnAction(changeSourceEncodingHandler);
        big5RadioButton.setOnAction(changeSourceEncodingHandler);
        utf8RadioButton.setOnAction(changeSourceEncodingHandler);
        encodingComboBox.setOnAction(changeSourceEncodingHandler);

    }
}
