package clearing

trait Logger:
  def log(message: String): Unit =
    println(s"[INFO] $message")
