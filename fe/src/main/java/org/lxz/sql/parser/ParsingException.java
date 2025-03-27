package org.lxz.sql.parser;

import static java.lang.String.format;

public class ParsingException extends RuntimeException {
    public ParsingException(String message) {
        super(message, null);
    }

    public ParsingException(String formatString, Object... args) {
        super(format(formatString, args));
    }

    public String getErrorMessage() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        return getErrorMessage();
    }
}
