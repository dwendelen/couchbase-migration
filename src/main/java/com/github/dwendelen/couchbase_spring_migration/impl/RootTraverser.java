package com.github.dwendelen.couchbase_spring_migration.impl;

import com.github.dwendelen.couchbase_spring_migration.MigrationStatus;

public class RootTraverser extends AbstractTraverser {
    private MigrationStatus status = MigrationStatus.NOT_STARTED;

    @Override
    protected MigrationStatus getStatus() {
        return status;
    }

    @Override
    protected void setStatus(MigrationStatus status) {
        this.status = status;
    }

    @Override
    protected void selfTraverse() {
        setStatus(MigrationStatus.SUCCESS);
    }
}
