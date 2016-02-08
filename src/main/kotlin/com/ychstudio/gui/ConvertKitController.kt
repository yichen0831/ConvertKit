package com.ychstudio.gui

import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.FileChooser
import opencc.OpenCC
import org.mozilla.universalchardet.UniversalDetector
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.util.*

class ConvertKitController : Initializable {

    @FXML
    internal var quitMenuItem: MenuItem? = null

    @FXML
    internal var selectedFileLabel: Label? = null

    @FXML
    internal var gbkRadioButton: RadioButton? = null

    @FXML
    internal var big5RadioButton: RadioButton? = null

    @FXML
    internal var utf8RadioButton: RadioButton? = null

    @FXML
    internal var encodingComboBox: ComboBox<String>? = null

    @FXML
    internal var conversionComboBox: ComboBox<String>? = null

    @FXML
    internal var applyConversionButton: Button? = null

    @FXML
    internal var inputTextArea: TextArea? = null

    @FXML
    internal var outputTextArea: TextArea? = null

    @FXML
    internal var targetEncodingComboBox: ComboBox<String>? = null

    @FXML
    internal var saveAsRadioButton: RadioButton? = null

    @FXML
    internal var overwriteRadioButton: RadioButton? = null

    @FXML
    internal var saveFileButton: Button? = null


    private var selectedFile: File? = null
    private val openCC = OpenCC()

    internal var changeSourceEncodingHandler = { event: ActionEvent ->
        when (event.source) {
            gbkRadioButton -> encodingComboBox?.value = "GBK"
            big5RadioButton -> encodingComboBox?.value = "Big5"
            utf8RadioButton -> encodingComboBox?.value = "UTF-8"
            encodingComboBox -> {
                when (encodingComboBox?.value) {
                    "GBK" -> gbkRadioButton?.isSelected = true
                    "Big5" -> big5RadioButton?.isSelected = true
                    "UTF-8" -> utf8RadioButton?.isSelected = true
                    else -> {
                        gbkRadioButton?.isSelected = false
                        big5RadioButton?.isSelected = false
                        utf8RadioButton?.isSelected = false
                    }
                }
                changeInputEncoding(encodingComboBox!!.value)
            }
            else  -> {}
        }
    }

    internal var conversionActionHandler = { event: ActionEvent -> applyConversion() }

    internal var applyConversionHandler = { event: ActionEvent -> applyConversion() }

    protected fun applyConversion() {
        for (entry in OpenCC.CONVERSIONS.entries) {
            if (entry.value == conversionComboBox?.value) {
                convertInputToOutput(entry.key)
                saveFileButton?.isDisable = false
                break
            }
        }
    }

    fun changeInputEncoding(encoding: String) {
        if (selectedFile != null) {
            loadFileWithEncoding(selectedFile as File, encoding)
            applyConversion()
        }
    }

    fun detectFileEncoding(file: File): String {
        val universalDetector = UniversalDetector(null)

        val buffer = ByteArray(2048)

        try {
            val bis = BufferedInputStream(FileInputStream(file))
            var nread = bis.read(buffer)
            while (nread > 0 && !universalDetector.isDone) {
                universalDetector.handleData(buffer, 0, nread)
                nread = bis.read(buffer)
            }
            universalDetector.dataEnd()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val encoding = universalDetector.detectedCharset
        return encoding ?: "UTF-8"
    }

    protected fun loadFileWithEncoding(file: File, encoding: String) {
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(file), encoding))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                stringBuilder.append("\n")
                line = bufferedReader.readLine()
            }

            inputTextArea?.clear()
            inputTextArea?.text = stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    protected fun convertInputToOutput(conversion: String) {
        val input = inputTextArea?.text
        openCC.setConversion(conversion)
        val output = openCC.convert(input)
        outputTextArea?.clear()
        outputTextArea?.text = output
    }

    fun chooseFile() {

        val fileChooser = FileChooser()
        selectedFile = fileChooser.showOpenDialog(null)
        if (selectedFile != null) {
            selectedFileLabel?.text = (selectedFile as File).name

            val encoding = detectFileEncoding(selectedFile as File)

            when (encoding) {
                "GBK" -> gbkRadioButton?.isSelected = true
                "Big5" -> big5RadioButton?.isSelected = true
                "UTF-8" -> utf8RadioButton?.isSelected = true
                else -> {
                    gbkRadioButton?.isSelected = false
                    big5RadioButton?.isSelected = false
                    utf8RadioButton?.isSelected = false
                }
            }
            encodingComboBox?.value = encoding

            loadFileWithEncoding(selectedFile as File, encoding)
            saveFileButton?.isDisable = true
            applyConversion()
        }
    }

    fun saveFile() {

        when {
            saveAsRadioButton!!.isSelected -> {
                val fileChooser = FileChooser()
                fileChooser.initialFileName = selectedFile!!.name
                fileChooser.initialDirectory = selectedFile!!.parentFile
                val chosen: File? = fileChooser.showSaveDialog(null)

                if (chosen != null) {
                    writeToFile(chosen.absolutePath, outputTextArea!!.text, targetEncodingComboBox!!.value)
                }
            }

            overwriteRadioButton!!.isSelected -> {
                writeToFile(selectedFile!!.absolutePath, outputTextArea!!.text, targetEncodingComboBox!!.value)
            }
        }
    }

    protected fun writeToFile(filename: String, output: String, encoding: String) {
        if (output.isEmpty()) {
            return
        }

        val outputFile = File(filename)

        val bufferedWriter= BufferedWriter(OutputStreamWriter(FileOutputStream(outputFile), encoding))
        bufferedWriter.write(output)
        bufferedWriter.flush()
        bufferedWriter.close()
    }

    protected fun buildConversionComboBox() {
        val conversionOptions = FXCollections.observableArrayList<String>()
        conversionComboBox?.items = conversionOptions

        val tmpArrayList = ArrayList<String>()
        for (value in OpenCC.CONVERSIONS.values) {
            tmpArrayList.add(value)
        }
        tmpArrayList.sort(String::compareTo)
        tmpArrayList.forEach { conversionOptions.add(it) }
    }


    override fun initialize(location: URL?, resources: ResourceBundle?) {

        quitMenuItem?.setOnAction({ e -> System.exit(0) })

        val encodingChoiceList = FXCollections.observableArrayList<String>()
        encodingChoiceList.addAll(Charset.availableCharsets().keys)
        encodingComboBox?.items = encodingChoiceList
        targetEncodingComboBox?.items = encodingChoiceList
        targetEncodingComboBox?.value = "UTF-8"

        gbkRadioButton?.setOnAction(changeSourceEncodingHandler)
        big5RadioButton?.setOnAction(changeSourceEncodingHandler)
        utf8RadioButton?.setOnAction(changeSourceEncodingHandler)
        encodingComboBox?.setOnAction(changeSourceEncodingHandler)

        conversionComboBox?.setOnAction(conversionActionHandler)
        buildConversionComboBox()

        applyConversionButton?.setOnAction(applyConversionHandler)

        inputTextArea?.isEditable = false
//        outputTextArea?.isEditable = false
    }
}
