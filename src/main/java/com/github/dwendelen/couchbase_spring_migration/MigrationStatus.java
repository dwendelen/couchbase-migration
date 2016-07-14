package com.github.dwendelen.couchbase_spring_migration;

public enum MigrationStatus {
    NOT_STARTED(true),
    RUNNING(true),
    FAILED(false),
    SUCCESS(false);

    private boolean isNotFinished;

    MigrationStatus(boolean isNotFinished) {
        this.isNotFinished = isNotFinished;
    }

    public boolean isNotFinished() {
        return isNotFinished;
    }
}
