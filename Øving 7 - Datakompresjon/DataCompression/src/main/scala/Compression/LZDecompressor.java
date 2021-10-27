package Compression;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class LZDecompressor {

    public static short joinBytesToShort(byte b1, byte b2){
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(b1);
        bb.put(b2);
        return bb.getShort(0);
    }

    private static byte[] restore(byte[] compressed){
        // reads the length of the original file from the first four bytes
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put(Arrays.copyOf(compressed, 4));

        byte[] restored = new byte[bb.getInt(0)];

        int i = 0; // index in restored
        int j = 4; // index in compressed
        int x; // temporary index storage
        short noUncomp;
        short l;
        short refDist;
        short refLen;

        while(j < compressed.length - 1){
            // reads number of following uncompressed bytes and copies up to that point
            noUncomp = joinBytesToShort(compressed[j++], compressed[j++]);
            while(noUncomp-- > 0){
                if(i >= restored.length - 1 || j >= compressed.length - 1) break;
                restored[i++] = compressed[j++];
            }

            if(j >= compressed.length - 4) break;
            refDist = joinBytesToShort(compressed[j++], compressed[j++]);
            refLen = joinBytesToShort(compressed[j++], compressed[j++]);

            x = i - refDist;
            l = 0;

            while(l < refLen){
                if(i < restored.length && x + l < restored.length){
                    restored[i++] = restored[x + l++];
                } else break;
            }
        }
        restored[restored.length - 1] = compressed[compressed.length - 1];
        return restored;
    }

    public static byte [] decompress(byte[] zipped) throws IOException {
        return restore(zipped);
    }
}
