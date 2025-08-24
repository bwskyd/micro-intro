package util;

import java.util.Arrays;
import java.util.List;

public abstract class Util {
    public static List<Long> parseIdsFromString(String idsString, String delimiter) {
        return Arrays.stream(idsString.split(delimiter))
                .map(Long::parseLong)
                .toList();
    }
    public static String secondsToMMSS(int seconds) {
        return "%02d:%02d".formatted(
                seconds / 60,
                seconds % 60
        );
    }
}
