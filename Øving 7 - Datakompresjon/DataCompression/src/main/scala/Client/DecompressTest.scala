package Client

import java.io.{File, FileInputStream, FileOutputStream}

import Compression.{HuffmanCodec, LZDecompressor}

object DecompressTest extends App {

  val file = new File("./TestData/").listFiles().find(file => file.getName.contains(".lgh")).get

  println(s"\nFilename: ${file.getName}")

  val dataStream = new FileInputStream(file)

  val extractedData = dataStream
    .readAllBytes()
  dataStream.close()

  val lz77Code = HuffmanCodec.reconstruct(extractedData).decode(extractedData)
  val decompressedData = LZDecompressor.decompress(lz77Code)

  print(s"Decompressed byte size: ${decompressedData.length}\n")

  val fileOutputStream = new FileOutputStream(file.getPath.split(".lgh").head)
  fileOutputStream.write(decompressedData)
  fileOutputStream.close()
}
