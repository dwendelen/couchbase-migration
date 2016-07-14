package com.github.dwendelen.couchbase_spring_migration.impl;

import com.github.dwendelen.couchbase_spring_migration.MigrationStatus;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTraverser {
    private List<MigrationTraverser> dependencies = new ArrayList<>();

    public void addDependency(MigrationTraverser migrationTraverser) {
        this.dependencies.add(migrationTraverser);
    }

    protected abstract MigrationStatus getStatus();
    protected abstract void setStatus(MigrationStatus failed);

    public void traverse(boolean waitForOtherRunningMigrations, boolean failIfNotAllSuccessfull) {
        if(getStatus() == MigrationStatus.SUCCESS) {
            return;
        }

        boolean allDependenciesSucceeded = true;

        for (MigrationTraverser dependency : dependencies) {
            if(dependency.getStatus() == MigrationStatus.NOT_STARTED) {
                dependency.traverse(waitForOtherRunningMigrations, failIfNotAllSuccessfull);
            }
            if(dependency.getStatus() == MigrationStatus.RUNNING) {
                if(waitForOtherRunningMigrations) {
                    dependency.waitForCompletion();
                }
            }
            if(dependency.getStatus() != MigrationStatus.SUCCESS) {
                allDependenciesSucceeded = false;
            }
        }

        if(allDependenciesSucceeded) {
            selfTraverse();
        } else {
            if(failIfNotAllSuccessfull) {
                this.setStatus(MigrationStatus.FAILED);
            }
        }
    }

    protected abstract void selfTraverse();
}