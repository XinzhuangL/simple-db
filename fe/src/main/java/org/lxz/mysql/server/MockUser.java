package org.lxz.mysql.server;

import com.google.common.collect.ImmutableMap;

public class MockUser {

    private static final ImmutableMap<String, String> user2Password = ImmutableMap.of(
            "root", "root123456"
    );

    public static boolean checkPassword(String remoteUser, String remoteHost, byte[] remotePasswd, byte[] randomString) {
        if (!user2Password.containsKey(remoteUser)) {
            return false;
        }
        byte[] saltPassword = MysqlPassword.getSlatFromPassword(MysqlPassword.makeScrambledPassword(user2Password.get(remoteUser)));
        if (saltPassword.length != remotePasswd.length) {
            throw new RuntimeException("password length mismatch!");
        }

        if (remotePasswd.length > 0 &&
        !MysqlPassword.checkScramble(remotePasswd, randomString, saltPassword)) {
            throw new RuntimeException("password mismatch!");
        }
        return true;
    }
}
