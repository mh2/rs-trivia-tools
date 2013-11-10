package dk.reportsoft.trivia.tools

trait MessageIDGenerator {
  
  private var currentID = 0L
  def nextID = this.synchronized{currentID += 1; currentID}

}