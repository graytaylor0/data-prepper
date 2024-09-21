package partitions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LeaderProgressState {

    @JsonProperty("initialized")
    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
