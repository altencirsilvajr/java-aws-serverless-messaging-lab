package com.altencir.serverless.application;

import com.altencir.serverless.domain.ProcessingRecord;

public record SubmissionResult(ProcessingRecord record, boolean duplicate) { }
