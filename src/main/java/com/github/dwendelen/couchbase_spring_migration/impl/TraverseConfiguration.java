package com.github.dwendelen.couchbase_spring_migration.impl;

public enum TraverseConfiguration {
    FAST_PATH {
        @Override
        public void onRunningMigration(AbstractTraverser traversing, MigrationTraverser dependency) {
            //DO NOT WAIT FOR THE MIGRATION, TRY THE MIGRATIONS FIRST
        }
    },
    COMPLETE_PATH {
        @Override
        public void onRunningMigration(AbstractTraverser traversing, MigrationTraverser dependency) {
            dependency.waitForCompletion();
        }
    };

    public abstract void onRunningMigration(AbstractTraverser traversing, MigrationTraverser dependency);
}
