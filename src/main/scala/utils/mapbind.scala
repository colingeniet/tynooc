package utils

import scalafx.collections._
import scalafx.collections.ObservableBuffer._
import scalafx.event.subscriptions._

import scala.collection.JavaConverters._


/** Binds observable buffers through a function, map like. */
object MapBind {
  def apply[A, B](
    l1: ObservableBuffer[A],
    l2: ObservableBuffer[B],
    f: (A) => B): Subscription = {
    l2.clear()
    l2.appendAll(l1.map(f(_)))

    l1.onChange((_, changes) => {
      changes.foreach(_ match {
        case Add(pos, elts) => {
          l2.insertAll(pos, elts.map(f(_)))
        }
        case Remove(pos, elts) => {
          l2.remove(pos, elts.size)
        }
        case Reorder(from, to, perm) => {
          val tmp = l2.slice(from, to)
          for (i <- from to (to - 1)) l2(perm(i)) = tmp(i - from)

        }
        case Update(from, to) => {
          for (i <- from to (to - 1)) l2(i) = f(l1(i))
        }
      })
    })
  }

  def apply[A, B](
    l1: ObservableBuffer[A],
    l2: javafx.collections.ObservableList[B],
    f: (A) => B): Subscription = {
    l2.setAll(asJavaCollection(l1.map(f(_)).toBuffer))

    l1.onChange((_, changes) => {
      changes.foreach(_ match {
        case Add(pos, elts) => {
          l2.addAll(pos, asJavaCollection(elts.map(f(_)).toBuffer))
        }
        case Remove(pos, elts) => {
          l2.remove(pos, pos + elts.size)
        }
        case Reorder(from, to, perm) => {
          val tmp = l2.subList(from, to)
          for (i <- from to (to - 1)) l2.set(perm(i), tmp.get(i - from))

        }
        case Update(from, to) => {
          for (i <- from to (to - 1)) l2.set(i, f(l1(i)))
        }
      })
    })
  }
}
