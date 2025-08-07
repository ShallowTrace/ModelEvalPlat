package com.ecode.modelevalplat.controller;

import com.ecode.modelevalplat.service.impl.EvalDockerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.Set;

//https://localhost:8002/api/eval/run/{1}
@RestController
@RequestMapping("/eval")
public class EvaluationController {
    @Autowired
    private final EvalDockerServiceImpl dockerService;

    public EvaluationController(EvalDockerServiceImpl dockerService) {
        this.dockerService = null;
    }

//    @GetMapping("/run/{id}")
//    public void runEvaluation(@PathVariable Long id) {
//        // 配置客户端
//        dockerService.executeDocker(id);
//    }
}