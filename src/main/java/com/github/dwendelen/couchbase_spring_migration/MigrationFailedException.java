package com.github.dwendelen.couchbase_spring_migration;

public class MigrationFailedException extends RuntimeException {
    public MigrationFailedException(String s) {
        super(s);
    }
}
