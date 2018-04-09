package utils

import scala.collection.mutable.HashMap

object InitHashMap {
  def apply[K,T](initial: K => T): HashMap[K,T] = {
    new HashMap[K,T] {
      override def default(k: K): T = {
        this(k) = initial(k)
        this(k)
      }
    }
  }
}
