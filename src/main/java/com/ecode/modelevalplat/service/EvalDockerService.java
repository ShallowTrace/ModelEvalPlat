package com.ecode.modelevalplat.service;

import java.nio.file.Path;

public interface EvalDockerService {
    void executeDocker(String datasetPath, Path targetDir, Long competitionId, Long submissionId);
}
