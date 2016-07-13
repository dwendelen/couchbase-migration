package migration;

import java.util.List;

public interface Migration {
    String getIdentifier();
    MigrationTiming getMigrationTiming();
    List<String> getDependencies();
    void start();

    /**
     * Must be determined stateless
     */
    MigrationStatus getStatus();
}
