package ru.home.linequeue.worker.queue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@Getter
public class QueueElement {

    private final String body;
    private final long ts;

}
