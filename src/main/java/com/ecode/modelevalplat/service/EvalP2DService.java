package com.ecode.modelevalplat.service;

import java.io.IOException;
import java.nio.file.Path;

public interface EvalP2DService {
    void generateDockerfile(Path projectPath) throws IOException;
}
