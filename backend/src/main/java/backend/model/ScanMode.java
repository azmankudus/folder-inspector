package backend.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum ScanMode {
    FULL,  // Store everything
    AUDIT  // Store folders and explicit permissions only
}
