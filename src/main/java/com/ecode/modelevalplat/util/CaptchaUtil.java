package com.ecode.modelevalplat.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码工具类
 * 生成6位数字和大小写字母的验证码字符串和验证码图片Base64
 */
public class CaptchaUtil {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int WIDTH = 160;  // 验证码图片宽度
    private static final int HEIGHT = 60;  // 验证码图片高度
    private static final int CODE_LENGTH = 6;  // 验证码长度

    private static final Random random = new Random();

    /**
     * 生成验证码字符串（6位数字和大小写字母）
     * @return 验证码字符串
     */
    public static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片Base64字符串
     * @param code 验证码字符串
     * @return Base64字符串（不含前缀"data:image/png;base64,"）
     */
    public static String generateImageBase64(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 画背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 画干扰线
        for (int i = 0; i < 20; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.setColor(getRandomColor(100, 255));
            g.drawLine(x1, y1, x2, y2);
        }

        // 画验证码字符串
        g.setFont(new Font("Arial", Font.BOLD, 40));
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g.setColor(getRandomColor(0, 150));
            // 旋转角度
            int degree = random.nextInt(21) - 10; // -10到10度
            double theta = Math.toRadians(degree);
            g.rotate(theta, 25 * i + 20, 45);
            g.drawString(String.valueOf(code.charAt(i)), 25 * i + 15, 45);
            g.rotate(-theta, 25 * i + 20, 45);
        }

        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    /**
     * 获取指定范围内随机颜色
     * @param min 最小值
     * @param max 最大值
     * @return Color对象
     */
    private static Color getRandomColor(int min, int max) {
        if (min > 255) min = 255;
        if (max > 255) max = 255;
        if (min < 0) min = 0;
        if (max < 0) max = 0;
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }
}


