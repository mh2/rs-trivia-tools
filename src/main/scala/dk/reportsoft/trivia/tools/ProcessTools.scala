package dk.reportsoft.trivia.tools

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.management.ManagementFactory
import java.net.Socket
import java.util.ArrayList
import java.util.regex.Pattern
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

object OperatingSystems {
  sealed abstract class OperatingSystem
  case object Windows7 extends OperatingSystem
  case object Linux extends OperatingSystem
}

object ProcessTools {
  import OperatingSystems._
  
  private val logger = LoggerFactory.getLogger(ProcessTools.this.getClass())

  private val idExtractor = Pattern.compile("[0-9]+")

  implicit val operatingSystem = {
    System.getProperty("os.name") match {
      case anything if anything.toLowerCase().contains("windows") => Windows7
      case "Linux" => Linux
    }
  }
    
  def getRawProcessID = {
    def default = {
      val runtimeName = ManagementFactory.getRuntimeMXBean.getName
      val matcher = idExtractor.matcher(runtimeName)
      if (matcher.find)
        Some(matcher.group)
      else
        None
    }
    operatingSystem match {
      case Windows7 => default
      case Linux => default
    }
  }
  private val numberPattern = Pattern.compile("[0-9]+")
  def getProcessID = getRawProcessID.map(pString => {val m = numberPattern.matcher(pString); if (m.find()) m.group() else ""})
  
  

  def isProcessRunning(processID: String) = {
    var returnee = false
    operatingSystem match {
      case Windows7 =>
        var proc: Process = null
        try {
          proc = Runtime.getRuntime().exec("""tasklist.exe /fi "PID eq """ + processID + "\" /fo LIST")
          val reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))
          var line: String = null
          val matchPattern = Pattern.compile(" *PID: *" + processID)
          do {
            line = reader.readLine()
            if (line != null) {
              val matcher = matchPattern.matcher(line)
              if (matcher.find())
                returnee = true
            }
          } while (line != null)
          reader.close()
        } catch {
          case e: Throwable =>
        }
        proc match {
          case null =>
          case p => p.destroy()
        }
      case Linux =>
        val p = Runtime.getRuntime().exec(s"ps -p $processID")
        p.waitFor()
        val theAnswer = p.exitValue == 0
        p.destroy()
        returnee = theAnswer
    }
    returnee
  }

  implicit def listConversions[A](array: Array[A]) = new {
    def toArrayList = {
      val returnee = new ArrayList[A]()
      array.foreach(elem => returnee.add(elem))
      returnee
    }

  }

  type OC = String => Unit
  private def defaultOutputConsumer(str: String) = {}

  import dk.reportsoft.trivia.tools.ConcurrencyTools._
  private def consumeWithConsumer(inputStream: InputStream, oc: OC) = {
    threadPoolExecutorService.execute(() => {
      try {
        val reader = new BufferedReader(new InputStreamReader(inputStream))
        var read = ""
        while (read != null) {
          read = reader.readLine()
          oc(read)
        }
        reader.close()
      } catch {
        case e: Throwable => { e.printStackTrace(); logger.debug("Error occured while getting output/error", e) }
      }
    })
  }


  private val threadPoolExecutorService = Executors.newCachedThreadPool()

  def launchProcess(environmentVariables: Map[String, String], commandsToExecute: List[Array[String]], outputConsumer: OC = defaultOutputConsumer, inputProvider: Option[Seq[String]] = None, errorConsumer: OC = defaultOutputConsumer, startFolder: File = new File(".")) = {
    var returnee: Option[Process] = None
    val processBuilder = new ProcessBuilder
    environmentVariables.foreach(varPair => processBuilder.environment().put(varPair._1, varPair._2))
    processBuilder.directory(startFolder)

    val actions = List((file: File) => processBuilder.redirectInput(file), (file: File) => processBuilder.redirectOutput(file), (file: File) => processBuilder.redirectError(file))

    commandsToExecute.foreach(commands => processBuilder.command(commands.toArrayList))
    val process = processBuilder.start()

    consumeWithConsumer(process.getInputStream, outputConsumer)
    
    consumeWithConsumer(process.getErrorStream, errorConsumer)

    inputProvider match {
      case None =>
      case Some(inputs) => {
        threadPoolExecutorService.execute { () => inputs.foreach(str => process.getOutputStream().write(str.getBytes())) }
      }
    }

    returnee = Some(process)
    returnee
  }

  def killProcess(processID: String) = {
    val command = operatingSystem match {
      case Windows7 => "TASKKILL /PID " + processID + " /F /T"
      case Linux => s"kill $processID && kill -9 $processID"
    }
    val process = Runtime.getRuntime().exec(command)
    process.waitFor()
  }

}