package dk.reportsoft.trivia.tools

import scala.collection.JavaConversions._
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.ArrayList
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.Await


trait ProcessLauncher {
  
  private def defaultOC(str : String) = {println(str)}
  
  def launchProcess(environmentVariables: Map[String, String], commandsToExecute: List[Array[String]], outputConsumer: String => Unit = defaultOC  , inputProvider: Option[Seq[String]] = None, errorConsumer: String => Unit = defaultOC, startFolder: File = new File(".")) = {
    ProcessTools.launchProcess(environmentVariables, commandsToExecute, outputConsumer, inputProvider, errorConsumer, startFolder)

  }

}