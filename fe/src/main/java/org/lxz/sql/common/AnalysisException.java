package org.lxz.sql.common;

public class AnalysisException extends Exception {
    public AnalysisException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AnalysisException(String msg) {
        super(msg);
    }
}
