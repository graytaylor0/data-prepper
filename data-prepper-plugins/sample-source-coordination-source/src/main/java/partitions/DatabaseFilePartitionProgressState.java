package partitions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseFilePartitionProgressState {

    @JsonProperty("perecent_complete")
    private double percentComplete;

    public double getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(double percentComplete) {
        this.percentComplete = percentComplete;
    }
}
