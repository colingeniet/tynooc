package utils

import scala.collection.mutable.HashMap

/** HashMap with automatic initialization. */
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
