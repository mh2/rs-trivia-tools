package dk.reportsoft.trivia.infrastructure.factory.tools

import java.io.InputStream
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.jar.{Manifest => JarManifest}

import scala.collection.JavaConversions._

object JARTools {
  
  def describeJar(jarInputStream : InputStream) = {
    val jarIn = new JarInputStream(jarInputStream)
    var manifest = jarIn.getManifest()
    var returnee = new JarFileDescription
    if(manifest != null)
       setAttributes(returnee, manifest)
    
    var nextEntry : ZipEntry = null
    var allClasses = List.empty[String]
    do {
      nextEntry = jarIn.getNextEntry()
      if(nextEntry != null && nextEntry.getName() != null && nextEntry.getName().trim.toLowerCase.endsWith(".class")) {
        val className = nextEntry.getName.substring(0, nextEntry.getName().length()-6).replace("\\", ".").replace("/",".")
        allClasses ::= className
      }
    }
    while(nextEntry != null)
    
    returnee.classes = allClasses.reverse
    returnee
  }
  
  private def setAttributes(jarFileDescription : JarFileDescription, manifest : JarManifest) {
    val mainAttributes = manifest.getMainAttributes() match {
      case null => Seq.empty
      case _ =>manifest.getMainAttributes().entrySet().filter(e => e.getKey() != null && e.getValue() != null).map(e => e.getKey().toString() -> e.getValue().toString())
    }  
    val otherAttributes = manifest.getEntries() match {
      case null => Seq.empty
      case _ => manifest.getEntries().filter(_._1 != null).flatMap(e => e._2.entrySet().filter(inner => inner.getKey() != null && inner.getValue() != null).map(inner => inner.getKey().toString -> inner.getValue().toString))
    }
    
    val allAttributes = mainAttributes ++ otherAttributes
      
      
    allAttributes.foreach(entry => {
      if(entry._1.trim.toLowerCase=="bundle-name") 
        jarFileDescription.implementationName = Some(entry._2)
      if(entry._1.trim.toLowerCase == "bundle-version")
        jarFileDescription.implementationVersion = Some(entry._2)
    })
    jarFileDescription.attributes = allAttributes toMap
  }
  
  case class JarFileDescription(
     var implementationName : Option[String] = None,
     var implementationVersion : Option[String] = None,
     var attributes : Map[String,String] = Map.empty,
     var classes : Seq[String] = Seq.empty
  ) {
    override def equals(other : Any) = {
      if(other == null) false
      else other match {
        case jarFile : JarFileDescription => isSameJar(jarFile)
        case _ => false
      }
    }
    
    def isSameJar(jarFile : JarFileDescription) = {
      val JarFileDescription(otherImplName, otherImplVersion, otherAttributes, otherClasses) = jarFile
      (implementationName, otherImplName, implementationVersion, otherImplVersion) match {
            case (Some(n1), Some(n2), Some(v1), Some(v2)) if(n1 == n2 && v1 == v2)=> true
            case _ => false    
      }
    }
    
    def sharesSomeClasses(otherJar : JarFileDescription) = classes.exists(clazz => otherJar.classes.contains(clazz))
    
    def sharesAllClass(otherJar : JarFileDescription) = classes.toSet == otherJar.classes.toSet
    
  }
  

}