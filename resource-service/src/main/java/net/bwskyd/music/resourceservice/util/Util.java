package net.bwskyd.music.resourceservice.util;

public abstract class Util {
    public static String secondsToMMSS(int seconds) {
        return "%02d:%02d".formatted(
                seconds / 60,
                seconds % 60
        );
    }
}
