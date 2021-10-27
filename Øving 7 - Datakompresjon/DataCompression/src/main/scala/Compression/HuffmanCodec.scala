package Compression

import java.nio.ByteBuffer

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class HuffmanCodec private (byteFrequencyTable: Seq[(Byte, Int)]) {

  val root: Node = {
    val huffmanTree = new mutable.PriorityQueue[Node]()((one, two) => two.frequency - one.frequency)

    byteFrequencyTable
      .map(tuple => Node(tuple._2, Left(tuple._1)))
      .foreach(huffmanTree.addOne)

    while(huffmanTree.size > 1){
      val lowestWeight = huffmanTree.dequeue()
      val secondLowest = huffmanTree.dequeue()

      huffmanTree
        .addOne(Node(lowestWeight.frequency + secondLowest.frequency, Right(Some(lowestWeight), Some(secondLowest))))
    }
    huffmanTree.dequeue()
  }

  def encode(raw: Array[Byte]): Array[Byte] = {
    val encodedBuffer = raw.map(byte => byte.toString)
    root.encode("", raw, (path, index) => encodedBuffer(index) = path)

    constructHeader()
      .appendedAll(BigInt(encodedBuffer.mkString(""), 2).toByteArray)
  }

  def decode(zipped: Array[Byte]): Array[Byte] = {
    val prefixes = BigInt(removeHeader(zipped)).toString(2).iterator
    val decodedBuffer = new ListBuffer[Byte]
    var currentNode = root

    prefixes.foreach {
      case '0' => currentNode.pointer match {
        case Left(value) =>
          decodedBuffer.append(value)
          currentNode = root.left().get
        case Right(Some(leftChild) -> _) => currentNode = leftChild
        case _ => throw new UnknownError("Could not parse coding")
      }
      case '1' => currentNode.pointer match {
        case Left(value) =>
          decodedBuffer.append(value)
          currentNode = root.right().get
        case Right(_ -> Some(rightChild)) => currentNode = rightChild
        case _ => throw new UnknownError("Could not parse coding")
      }
    }

    decodedBuffer.append(currentNode.pointer match {
      case Left(value) => value
      case Right(_) => 0
    })

    decodedBuffer.toArray
  }

  def removeHeader(zipped: Array[Byte]): Array[Byte] = zipped
      .drop(ByteBuffer.wrap(zipped.slice(0, 4)).getInt + 4)

  def constructHeader(): Array[Byte] = {
    val frequencyTable = byteFrequencyTable
      .map(tuple => tuple._1 -> ByteBuffer.allocate(4).putInt(tuple._2).array())
      .flatten(tuple => tuple._2.prepended(tuple._1))
      .toArray

    frequencyTable
      .prependedAll(ByteBuffer.allocate(4)
        .putInt(frequencyTable.length)
        .array())
  }

  @deprecated("Using native API instead, i.e. BigInt and ByteBuffer class.")
  def getBit(num: Byte, bit: Byte): Boolean = {
    if((num >> (7 - bit) & 1).toByte == 1) return true
    false
  }

  @deprecated("Using native API instead, i.e. BigInt and ByteBuffer class.")
  def setBit(state: Boolean, num: Byte, bit: Byte): Byte = {
    if(state) return (num | 1 << (7 - bit)).toByte
    (num & ~(1 << bit)).toByte
  }

  case class Node(frequency: Int, pointer: Either[Byte, (Option[Node], Option[Node])]) extends Ordered[Node] {

    override def compare(that: Node): Int = this.frequency - that.frequency

    def left(): Option[Node] = pointer match {
      case Left(_) => None
      case Right(node) => node._1
    }

    def right(): Option[Node] = pointer match {
      case Left(_) => None
      case Right(node) => node._2
    }

    def encode(path: String, raw: Array[Byte], consumer: (String, Int) => ()): Unit =
      pointer match {
        case Left(value) => raw
          .zipWithIndex
          .foreach(tuple => if(tuple._1 == value) consumer(path, tuple._2))
        case Right(Some(leftNode) -> Some(rightNode)) =>
          leftNode.encode(path.concat("0"), raw, consumer)
          rightNode.encode(path.concat("1"), raw, consumer)
        case Right(Some(leftNode) -> None) =>
          leftNode.encode(path.concat("0"), raw, consumer)
        case Right(None -> Some(rightNode)) =>
          rightNode.encode(path.concat("1"), raw, consumer)
      }
  }
}
object HuffmanCodec {

  /**
   * Creates a new codec huffman-codec tailored to this file.
   * @param rawData Any data, but best suited for uncompressed files.
   * @return A huffman codec.
   */

  def construct(rawData: Array[Byte]): HuffmanCodec = new HuffmanCodec(rawData
    .groupBy(byte => byte)
    .map(tuple => tuple._1  -> tuple._2.length)
    .toSeq)

  /**
   * Invoked only if provided data is previously compressed by this huffman-implementation.
   * @param zipped the compressed data.
   * @return A reconstructed codec.
   */

  def reconstruct(zipped: Array[Byte]): HuffmanCodec = {
    val metaDataSpan = ByteBuffer.wrap(zipped.slice(0, 4)).getInt

    new HuffmanCodec(zipped
      .slice(4, metaDataSpan + 4)
      .grouped(5)
      .map(bytes => bytes.head -> ByteBuffer.wrap(bytes.tail).getInt)
      .toSeq)
  }
}