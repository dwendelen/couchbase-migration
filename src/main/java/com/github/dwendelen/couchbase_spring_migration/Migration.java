package com.github.dwendelen.couchbase_spring_migration;

import java.util.List;

public interface Migration {
    String getIdentifier();
    MigrationTiming getMigrationTiming();
    List<String> getDependencies();
    void migrate();

    /**
     * Must be determined stateless
     */
    MigrationStatus getStatus();
}
