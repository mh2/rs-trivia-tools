package dk.reportsoft.trivia.tools

object ConcurrencyTools {
  
  /* As of right now, I have found no way around the problem described here: 
   * http://stackoverflow.com/questions/12774840/scala-passing-function-parameter-which-does-not-have-parameter
  implicit def actionToRunnable(action :  => Unit ) = {
    println("Implicitly")
    new Runnable() {
      override def run() ={
        action
      }
    }
  }*/

  implicit def actionWithEPToRunnable(action : () => Unit ) = {
    new Runnable() {
      override def run() = action()
    }
  }
  
}