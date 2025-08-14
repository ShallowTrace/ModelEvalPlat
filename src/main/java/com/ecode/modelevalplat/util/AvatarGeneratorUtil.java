package com.ecode.modelevalplat.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AvatarGeneratorUtil {

    // 所有可选的头像URL
    private static final List<String> AVATAR_URLS = Arrays.asList(
            "https://api.dicebear.com/9.x/personas/svg?seed=Emery",
            "https://api.dicebear.com/9.x/personas/svg?seed=Leah",
            "https://api.dicebear.com/9.x/personas/svg?seed=Brian",
            "https://api.dicebear.com/9.x/personas/svg?seed=Vivian",
            "https://api.dicebear.com/9.x/personas/svg?seed=Eliza",
            "https://api.dicebear.com/9.x/personas/svg?seed=Sophia",
            "https://api.dicebear.com/9.x/personas/svg?seed=Kingston",
            "https://api.dicebear.com/9.x/miniavs/svg?seed=Katherine",
            "https://api.dicebear.com/9.x/miniavs/svg?seed=Jessica",
            "https://api.dicebear.com/9.x/miniavs/svg?seed=Maria",
            "https://api.dicebear.com/9.x/miniavs/svg?seed=Jack",
            "https://api.dicebear.com/9.x/miniavs/svg?seed=Alexander",
            "https://api.dicebear.com/9.x/miniavs/svg?seed=Jude",
            "https://api.dicebear.com/9.x/miniavs/svg?seed=Kingston",
            "https://api.dicebear.com/9.x/miniavs/svg?seed=Jade"
    );

    private static final Random RANDOM = new Random();

    /**
     * 获取一个随机头像URL
     */
    public static String getRandomAvatarUrl() {
        int index = RANDOM.nextInt(AVATAR_URLS.size());
        return AVATAR_URLS.get(index);
    }
}
