package org.lxz;

import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        byte[] bytes = new byte[]{67, 117, 115, 116, 111, 109, 101, 114, 35, 48, 48, 48, 48, 48, 48, 48, 49, 48};

        byte[] bytes2 = new byte[]{18, 67, 117, 115, 116, 111, 109, 101, 114, 35, 48, 48, 48, 48, 48, 48, 48, 49, 48};

        String s = new String(bytes, StandardCharsets.UTF_8);

        String s2 = new String(bytes2, StandardCharsets.UTF_8);

        System.out.println(s);

        System.out.println(s2);

        System.out.println("end");
    }
}
