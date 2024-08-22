package com.example.usermanagementapp.data;

import java.util.Random;

public class UserUtils {

    private static final String BASE_AVATAR_URL = "https://xsgames.co/randomusers/assets/avatars/%s/%d.jpg";
    private static final int MAX_AVATAR_ID = 78;
    private static final Random RANDOM = new Random(); // Reusable instance

    // Method to generate a random gender
    private static String getRandomGender() {
        return RANDOM.nextBoolean() ? "male" : "female";
    }

    // Method to generate the avatar URL
    public static String generateAvatarUrl(long userId) {
        int avatarId = (int) (userId % MAX_AVATAR_ID);
        String gender = getRandomGender();

        return String.format(BASE_AVATAR_URL, gender, avatarId);
    }
}
