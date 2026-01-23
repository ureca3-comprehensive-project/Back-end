package org.backend.port;

public interface BatchTriggerPort {
    void trigger(BatchCommand command);
}
