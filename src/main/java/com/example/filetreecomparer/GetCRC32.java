package com.example.filetreecomparer;

import java.io.*;
import java.util.zip.CRC32;

class GetCRC32 {
    private CRC32  crc = new CRC32();

    long calc(String fpath) throws IOException {
        /**
         * uses https://examples.javacodegeeks.com/core-java/security/calculate-the-crc-sum-of-a-file/
         * @return returns the crc32 of a file
         * @param the path the file whose crc is to be calculated
         * */

        // I am not sure if this is necessary but I think so
        crc.reset();
        // this is a temporary line to see how reset works
        System.out.println("crc reset value is" + crc.getValue());
        BufferedInputStream ins;
        try {
            ins = new BufferedInputStream(new FileInputStream(fpath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
        int i;

        while ((i=ins.read()) != -1) {
            crc.update(i);
        }
        return crc.getValue();
    }
}
