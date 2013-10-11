package archery

import scala.collection.mutable.ArrayBuffer
import scala.math.{min, max}

object RTree {

  /**
   * Construct an empty RTree.
   */
  def empty[A]: RTree[A] = new RTree(Node.empty[A], 0)

  /**
   * Construct an RTree from a sequence of entries.
   */
  def apply[A](entries: Entry[A]*): RTree[A] =
    entries.foldLeft(RTree.empty[A])(_ insert _)
}

/**
 * This is the magnificent RTree, which makes searching ad-hoc
 * geographic data fast and fun.
 * 
 * The RTree wraps a node called 'root' that is the actual root of the
 * tree structure. RTree also keeps track of the total size of the
 * tree (something that individual nodes don't do).
 */
case class RTree[A](root: Node[A], size: Int) {

  /**
   * Insert a value into the tree at (x, y), returning a new tree.
   */
  def insert(x: Float, y: Float, value: A): RTree[A] =
    insert(Entry(Point(x, y), value))

  /**
   * Insert an entry into the tree, returning a new tree.
   */
  def insert(entry: Entry[A]): RTree[A] = {
    val r = root.insert(entry) match {
      case Left(rs) => Branch(rs, rs.foldLeft(Box.empty)(_ expand _.box))
      case Right(r) => r
    }
    RTree(r, size + 1)
  }

  /**
   * Remove a value found at (x, y) from the tree.
   */
  def remove(x: Float, y: Float, value: A): RTree[A] =
    remove(Entry(Point(x, y), value))

  /**
   * Remove an entry from the tree, returning a new tree.
   * 
   * If the entry was not present, this method will throw an error.
   */
  def remove(entry: Entry[A]): RTree[A] =
    root.remove(entry) match {
      case None =>
        sys.error("wat")
      case Some((es, None)) =>
        es.foldLeft(RTree.empty[A])(_ insert _)
      case Some((es, Some(node))) =>
        es.foldLeft(RTree(node, size - 1))(_ insert _)
    }

  /**
   * Performs a search for all entries in the search space.
   * 
   * Points on the boundary of the search space will be included.
   */
  def search(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Seq[A] =
    search(Box(lat1, lon1, lat2, lon2)).map(_.value)

  /**
   * Return a sequence of all entries found in the given search space.
   */
  def search(space: Box): Seq[Entry[A]] =
    root.search(space)

  /**
   * Return a count of all entries found in the given search space.
   */
  def count(space: Box): Int =
    root.count(space)

  /**
   * Return whether or not the value exists in the tree at (x, y).
   */
  def contains(x: Float, y: Float, value: A): Boolean =
    root.contains(Entry(Point(x, y), value))

  /**
   * Return whether or not the given entry exists in the tree.
   */
  def contains(entry: Entry[A]): Boolean =
    root.contains(entry)

  /**
   * Return an iterator over all entries in the tree.
   */
  def entries: Iterator[Entry[A]] =
    root.iterator

  /**
   * Return an iterator over all values in the tree.
   */
  def values: Iterator[A] =
    entries.map(_.value)

  /**
   * Return a nice depiction of the tree.
   * 
   * This method should only be called on small-ish trees! It will
   * print one line for every branch, leaf, and entry, so for a tree
   * with thousands of entries this will result in a very large
   * string!
   */
  def pretty: String = root.pretty
}
