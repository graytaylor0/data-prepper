import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleSourceConfig {

    @JsonProperty("database_name")
    private String databaseName;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
