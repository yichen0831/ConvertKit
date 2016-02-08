package com.ychstudio

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

val version: String
    get() = "v0.1"

class MainWindow: Application() {
    override fun start(primaryStage: Stage) {

        val fxmlRoot = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("ConvertKitApplication.fxml"))

        val scene = Scene(fxmlRoot)

        primaryStage.scene = scene
        primaryStage.title = "ConvertKit " + version
        primaryStage.show()
    }
}


fun main(args: Array<String>) {
    Application.launch(MainWindow::class.java, *args)
}