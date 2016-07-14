package com.github.dwendelen.couchbase_spring_migration.impl;

import com.github.dwendelen.couchbase_spring_migration.Migration;
import com.github.dwendelen.couchbase_spring_migration.MigrationStatus;
import com.github.dwendelen.couchbase_spring_migration.impl.changelog.Changelog;
import com.github.dwendelen.couchbase_spring_migration.impl.changelog.MigrationEntry;

public class MigrationTraverser extends AbstractTraverser {
    private Changelog changelog;
    private Migration migration;

    public MigrationTraverser(Migration migration, Changelog changelog) {
        this.migration = migration;
        this.changelog = changelog;
    }

    public Migration getMigration() {
        return migration;
    }

    public MigrationStatus getStatus() {
        return getMigrationEntry().getStatus();
    }

    public void setStatus(MigrationStatus status) {
        getMigrationEntry().setStatus(status);
    }

    protected void selfTraverse() {
        migrate();
    }

    private void migrate() {
        boolean shouldStart = false;

        this.changelog.fetchAndlock();
        if(getStatus() == MigrationStatus.NOT_STARTED) {
            shouldStart = true;
            markStart();
        }
        this.changelog.saveAndReleaseLock();

        if(!shouldStart) {
            return;
        }
        migration.migrate();

        this.changelog.fetchAndlock();
        MigrationStatus status = migration.getStatus();
        markStop(status);
        this.changelog.saveAndReleaseLock();
    }

    private void markStart() {
        getMigrationEntry().markStart();
    }

    private void markStop(MigrationStatus status) {
        getMigrationEntry().markStop(status);
    }

    private MigrationEntry getMigrationEntry() {
        return changelog.getEntry(migration.getIdentifier());
    }

    public void waitForCompletion() {
        changelog.fetch();
        while(getStatus() == MigrationStatus.RUNNING) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            changelog.fetch();
        }
    }
}
