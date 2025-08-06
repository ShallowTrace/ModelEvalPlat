import com.ecode.modelevalplat.service.impl.EvalDockerServiceImpl;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class test2 {
    private DockerClient dockerClient;
    private Path tempDir;
    private final Long submissionId =123456L;
    private final String datasetPath = "test-dataset";
    private final String resultDirName = "test-results";

    @Before
    public void setUp() throws Exception {
        // 初始化Docker客户端
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        dockerClient = DockerClientImpl.getInstance(config, httpClient);

        // 创建临时测试目录
        tempDir = Files.createTempDirectory("docker-test");
        Files.createDirectories(tempDir.resolve(datasetPath));
        Files.createDirectories(tempDir.resolve(resultDirName));

        // 创建测试Dockerfile
        Files.write(tempDir.resolve("Dockerfile"), Collections.singletonList(
                "FROM alpine\n" +
                        "CMD echo \"Test container executed\" && touch /app/prediction_result/success.txt"
        ));
    }

    @After
    public void tearDown() throws Exception {
        // 清理测试资源
        dockerClient.close();
        FileUtils.deleteDirectory(tempDir.toFile());
    }

    @Test
    public void testExecuteDockerSuccess() throws Exception {
        // 初始化服务实现
        EvalDockerServiceImpl service = new EvalDockerServiceImpl();

        // 执行测试方法
        service.executeDocker(
                datasetPath,
                tempDir,
                1L,  // competitionId
                submissionId
        );

        // 验证镜像存在
        List<Image> images = dockerClient.listImagesCmd()
                .withFilter("reference", Collections.singleton("model-evaluator-"+submissionId))
                .exec();
        Assert.assertFalse("镜像应该被创建", images.isEmpty());

        // 验证结果文件
        Path resultFile = tempDir.resolve(resultDirName).resolve("success.txt");
        Assert.assertTrue("结果文件应该被创建", Files.exists(resultFile));

        // 验证容器自动清理
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();
        boolean containerExists = containers.stream().anyMatch(c ->
                c.getImage().equals("model-evaluator-"+submissionId));
        Assert.assertFalse("容器应该被自动清理", containerExists);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteDockerWithInvalidDockerfile() throws Exception {
        // 创建无效的Dockerfile
        Files.write(tempDir.resolve("Dockerfile"), Collections.singletonList("INVALID DOCKERFILE"));

        EvalDockerServiceImpl service = new EvalDockerServiceImpl();
        service.executeDocker(
                datasetPath,
                tempDir,
                1L,
                submissionId
        );
    }

    // 修复后的日志回调实现
    private static class LoggingCallback extends ResultCallback.Adapter<Frame> {
        private final StringBuilder log = new StringBuilder();

        @Override
        public void onNext(Frame frame) {
            try {
                String text = new String(frame.getPayload(), "UTF-8");

            } catch (UnsupportedEncodingException e) {
                log.append("[ENCODING ERROR] ").append(e.getMessage());
            }
        }

        @Override
        public String toString() {
            return log.toString();
        }
    }
}