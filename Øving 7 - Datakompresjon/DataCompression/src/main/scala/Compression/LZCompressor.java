package Compression;

import java.io.*;
import java.nio.ByteBuffer;

public class LZCompressor {
    public static final int MIN_REF_LEN = 6; // backwards references must at least be this long

    // "left" part of short is in b[0], "right" part in b[1]
    public static byte[] splitShortToBytes(short s){
        byte[] b = new byte[2];

        b[0] = (byte)(s & 0xff);
        b[1] = (byte)((s >> 8) & 0xff);

        return b;
    }

    /*
    Compresses the data into the form (example input: hello_ello)
                +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
        index-> |  0  |  1  |  2  |  3  |  4  |  5  |  6  |  7  |  8  |  9  |  10 |  11 |
                +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
      content-> |     6     |  h  |  e  |  l  |  l  |  o  |  _  |     5     |     4     |
                +-----------+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     */
    private static byte[] toCompressedArray(byte[] data, short[][] references) throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();

        // puts four bytes with the length of the original data at the start, used in decompression
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.putInt(data.length);
        byte[] lenBytes = new byte[4];
        bb.get(0, lenBytes);
        compressed.write(lenBytes);

        // manually places the first number of uncompressed bytes (bad)
        int toNextCounter = 0;
        while (++toNextCounter < references.length && references[toNextCounter][0] == 0);
        compressed.write(splitShortToBytes((short) (toNextCounter))[0]);
        compressed.write(splitShortToBytes((short) (toNextCounter))[1]);

        byte[] b;
        for(int i = 0; i < references.length; i++){
            if(references[i][0] == 0){
                compressed.write(data[i]); // copies from source when no reference present
            }
            else{
                // inserts backwards distance to reference and length of referenced segment
                b = splitShortToBytes(references[i][0]);
                compressed.write(b[0]);
                compressed.write(b[1]);
                b = splitShortToBytes(references[i][1]);
                compressed.write(b[0]);
                compressed.write(b[1]);

                // counts distance to next reference
                toNextCounter = 0;
                while (++toNextCounter + i + references[i][1] < references.length
                        && references[i + references[i][1] + toNextCounter][0] == 0);
                compressed.write(splitShortToBytes((short) (toNextCounter))[0]);
                compressed.write(splitShortToBytes((short) (toNextCounter))[1]);

                i += references[i][1] - 1;
            }
        }

        return compressed.toByteArray();
    }

    // goes through the entire array and returns an array of "references" which are on the following form:
    // if a long enough (>= MIN_REF_LEN) matching segment is found:
    // reference = {distance back to referenced segment, length of referenced segment}
    //
    // if no long enough matching segment is found:
    // reference = {0, 0}
    private static short[][] traverse(byte[] array){
        short[][] references = new short[array.length][2];
        short[] reference;
        for(int i = 0; i < array.length; i += reference[1] + 1) {
            reference = search(array, i);
            if(reference[1] >= MIN_REF_LEN) references[i] = reference;
        }

        return references;
    }

    // finds the longest byte segment which matches the segment starting at i
    private static short[] search(byte[] array, int i){
        short len;
        short[] longest = new short[2];
        for(int j = Math.max(0, i - Short.MAX_VALUE/2); j < i; j += len + 1) {
            len = matchingLength(array, i, j, (short) 0);
            if(len > longest[1]){
                longest[0] = (short) (i - j);
                longest[1] = len;
            }
        }
        return longest;
    }

    // returns the length of two matching segments in an array, starting at i and j
    private static short matchingLength(byte[] array, int i, int j, short count){
        if(array[i] == array[j] && i < array.length - 1 && count < Short.MAX_VALUE) return matchingLength(array, ++i, ++j, ++count);
        else return count;
    }

    // reads a file and puts the data into a byte array
    private static byte[] read(String path) throws IOException {
        var inFile = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
        int size = (int) new File(path).length();
        byte[] data = new byte[size];

        inFile.readFully(data);
        return data;
    }

    // takes file from pathIn, compresses it, and puts it in pathOut
    public static byte[] compress(byte[] raw) throws IOException {
        return toCompressedArray(raw, traverse(raw));
    }
}
