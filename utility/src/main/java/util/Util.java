package util;

import java.util.Arrays;
import java.util.List;

public abstract class Util {
    public static List<Long> parseIdsFromString(String idsString, String delimiter) {
        return Arrays.stream(idsString.split(delimiter))
                .map(Long::parseLong)
                .toList();
    }
}
