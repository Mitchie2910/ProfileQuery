package com.hng.nameprocessing.validation;

import jakarta.validation.GroupSequence;

@GroupSequence({BasicChecks.class, FormatChecks.class})
public interface ValidationOrder {
}
