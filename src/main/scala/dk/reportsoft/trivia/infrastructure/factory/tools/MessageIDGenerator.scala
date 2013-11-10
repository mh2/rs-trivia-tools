package dk.reportsoft.trivia.infrastructure.factory.tools

trait MessageIDGenerator {
  
  private var currentID = 0L
  def nextID = this.synchronized{currentID += 1; currentID}

}