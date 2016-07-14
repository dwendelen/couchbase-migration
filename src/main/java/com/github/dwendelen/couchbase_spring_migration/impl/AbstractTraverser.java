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
    public abstract void markAsFailed();

    public void traverse(TraverseConfiguration traverseConfiguration) {
        if(getStatus() == MigrationStatus.SUCCESS) {
            return;
        }

        boolean allDependenciesSucceeded = true;
        boolean hasFailedDependencies = false;

        for (MigrationTraverser dependency : dependencies) {
            if(dependency.getStatus() == MigrationStatus.NOT_STARTED) {
                dependency.traverse(traverseConfiguration);
            }
            //TRAVERSING DEEPER MUST BE DONE BEFORE CHECKING RESULT
            if(dependency.getStatus() == MigrationStatus.RUNNING) {
                traverseConfiguration.onRunningMigration(this, dependency);
            }
            //CHECKING RESULT MUST BE DONE AFTER THE DEPENDENCY HAD A CHANCE TO COMPLETE
            if(dependency.getStatus() != MigrationStatus.SUCCESS) {
                allDependenciesSucceeded = false;
            }
            if(dependency.getStatus() == MigrationStatus.FAILED) {
                hasFailedDependencies = true;
            }
        }

        if(allDependenciesSucceeded) {
            selfTraverse();
        } else {
            if(hasFailedDependencies) {
                markAsFailed();
            }
        }
    }

    protected abstract void selfTraverse();
}