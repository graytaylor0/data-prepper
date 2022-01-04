package com.amazon.dataprepper.model.configuration;

import java.nio.channels.Pipe;

/**
 * Model class for sharing descriptions of a pipeline with a plugin that belongs to that pipeline
 *
 * @since 1.3
 */
public class PipelineDescription {
    private String name;
    private int numProcessWorkers;


    public String getName() {
        return this.name;
    }

    public int getNumProcessWorkers() {
        return this.numProcessWorkers;
    }

    static class Builder {

    }
}
