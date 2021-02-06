package ru.home.linequeue.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
@ToString
public class Message implements Serializable {

    private static final long serialVersionUID = 6313680935421286924L;

    private final Type type;
    private final String body;

    public static enum Type {
        GET, PUT, REGISTER, SHUTDOWN, ACKNOWLEDGE, ERR, DATA, FINISHED
    }
}
