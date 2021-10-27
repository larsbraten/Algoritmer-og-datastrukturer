package Client

import java.io.{BufferedInputStream, FileOutputStream}
import java.net.URL

import Compression.{HuffmanCodec, LZCompressor}

object CompressTest extends App {

  //Select filename here.
  val filename = "diverse.lyx"



  val source = new URL("http://www.iie.ntnu.no/fag/_alg/kompr/".concat(filename))
  val dataStream = new BufferedInputStream(source.openStream())

  val extractedData = dataStream
    .readAllBytes()
  dataStream.close()
  println(s"\nFilename: $filename\nOriginal byte size: ${extractedData.length}")

  val originalOutputStream = new FileOutputStream("./TestData/".concat("original_").concat(filename))
  originalOutputStream.write(extractedData)
  originalOutputStream.close()

  val lz77Code = LZCompressor.compress(extractedData)
  println(s"LZ77 byte size: ${lz77Code.length}")

  val huffmanCode = HuffmanCodec.construct(lz77Code).encode(lz77Code)
  println(s"Huffman compressed byte size (on top of LZ77): ${huffmanCode.length}")

  val fileOutputStream = new FileOutputStream("./TestData/".concat(filename).concat(".lgh"))
  fileOutputStream.write(huffmanCode)
  fileOutputStream.close()
}
