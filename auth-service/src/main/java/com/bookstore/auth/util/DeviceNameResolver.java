package com.bookstore.auth.util;

public final class DeviceNameResolver {

    private DeviceNameResolver() {
    }

    /**
     * Derives a human-readable device label from User-Agent.
     * Never used as a unique device identifier.
     */
    public static String fromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown device";
        }

        String browser = resolveBrowser(userAgent);
        String os = resolveOs(userAgent);
        return browser + " on " + os;
    }

    private static String resolveBrowser(String userAgent) {
        if (userAgent.contains("Edg/")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome/") && !userAgent.contains("Edg/")) {
            return "Chrome";
        }
        if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) {
            return "Safari";
        }
        if (userAgent.contains("Firefox/")) {
            return "Firefox";
        }
        return "Browser";
    }

    private static String resolveOs(String userAgent) {
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        if (userAgent.contains("Android")) {
            return "Android";
        }
        if (userAgent.contains("Mac OS X") || userAgent.contains("Macintosh")) {
            return "macOS";
        }
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        return "Unknown OS";
    }
}
