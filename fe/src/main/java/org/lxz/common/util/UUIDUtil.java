package org.lxz.common.util;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import java.util.UUID;

public class UUIDUtil {
    private static final EthernetAddress ETHERNET_ADDRESS = EthernetAddress.fromInterface();
    private static final TimeBasedGenerator UUID_GENERATOR = Generators.timeBasedGenerator(ETHERNET_ADDRESS);


    // java.util.UUID.randomUUID() uses SecureRandom to generate random uuid,
    // and SecureRandom hold locks when generate random bytes.
    // This method is faster than java.uti.UUID.randomUUID() in high concurrency environment
    public static UUID genUUID() {
        return UUID_GENERATOR.generate();
    }



}
