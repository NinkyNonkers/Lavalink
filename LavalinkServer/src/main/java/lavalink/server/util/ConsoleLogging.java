package lavalink.server.util;

public final class ConsoleLogging {

    public static void Log(String data) {
        System.out.println(data);
    }

    public static void LogInfo(String data) {
        Log("[~] " + data);
    }

    public static void LogUpdate(String data) {
        Log("[i] " + data);
    }

    public static void LogError(String data) {
        Log("[!] " + data);
    }
}
