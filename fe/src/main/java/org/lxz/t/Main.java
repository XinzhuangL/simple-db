package org.lxz.t;

import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        byte[] bytes = new byte[]{67, 117, 115, 116, 111, 109, 101, 114, 35, 48, 48, 48, 48, 48, 48, 48, 49, 48};

        byte[] bytes2 = new byte[]{18, 67, 117, 115, 116, 111, 109, 101, 114, 35, 48, 48, 48, 48, 48, 48, 48, 49, 48};

        byte[] bytes3=  new byte[] {19, 50, 48, 49, 52, 45, 48, 55, 45, 50, 56, 32, 49, 48, 58, 53, 48, 58, 48, 48};

        String s = new String(bytes, StandardCharsets.UTF_8);

        String s2 = new String(bytes2, StandardCharsets.UTF_8);

        String s3 = new String(bytes3, StandardCharsets.UTF_8);

        System.out.println(s);

        System.out.println(s2);

        System.out.println(s3);

        System.out.println("end");
    }
}
