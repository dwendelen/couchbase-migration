package com.github.dwendelen.couchbase_spring_migration.impl;

import com.github.dwendelen.couchbase_spring_migration.MigrationStatus;

public class RootTraverser extends AbstractTraverser {
    private MigrationStatus status = MigrationStatus.NOT_STARTED;

    @Override
    protected MigrationStatus getStatus() {
        return status;
    }

    @Override
    public void markAsFailed() {
        status = MigrationStatus.FAILED;
    }

    @Override
    protected void selfTraverse() {
        status = MigrationStatus.SUCCESS;
    }
}
