package com.hydroline.beacon.command;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight localization loader for command output. The files live under /lang/messages_{locale}.properties.
 */
public final class TranslationManager {

    private static final String DEFAULT_LOCALE = "en_us";
    private static final String RESOURCE_FORMAT = "lang/messages_%s.properties";

    private final Plugin plugin;
    private final Map<String, Properties> cache = new ConcurrentHashMap<>();

    public TranslationManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public String get(String rawLocale, String key, Object... args) {
        String normalized = normalizeLocale(rawLocale);
        Properties bundle = loadBundle(normalized);
        String pattern = bundle.getProperty(key);
        if (pattern == null) {
            Properties fallback = loadBundle(DEFAULT_LOCALE);
            pattern = fallback.getProperty(key, key);
        }
        if (args == null || args.length == 0) {
            return pattern;
        }
        MessageFormat format = new MessageFormat(pattern, Locale.ROOT);
        return format.format(args);
    }

    private Properties loadBundle(String locale) {
        return cache.computeIfAbsent(locale, this::doLoadBundle);
    }

    private Properties doLoadBundle(String locale) {
        String resourcePath = String.format(Locale.ROOT, RESOURCE_FORMAT, locale);
        try (InputStream stream = plugin.getResource(resourcePath)) {
            if (stream == null) {
                if (!DEFAULT_LOCALE.equals(locale)) {
                    return doLoadBundle(DEFAULT_LOCALE);
                }
                return new Properties();
            }
            Properties props = new Properties();
            props.load(stream);
            return props;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load language bundle " + resourcePath + ": " + e.getMessage());
            return new Properties();
        }
    }

    private String normalizeLocale(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return DEFAULT_LOCALE;
        }
        String normalized = raw.trim().replace('-', '_').toLowerCase(Locale.ROOT);
        if (isSupportedLocale(normalized)) {
            return normalized;
        }
        int underscore = normalized.indexOf('_');
        if (underscore > 0) {
            String languageOnly = normalized.substring(0, underscore);
            if (isSupportedLocale(languageOnly)) {
                return languageOnly;
            }
        }
        return DEFAULT_LOCALE;
    }

    private boolean isSupportedLocale(String candidate) {
        // simple check: try to load bundle without storing
        String resourcePath = String.format(Locale.ROOT, RESOURCE_FORMAT, candidate);
        try (InputStream stream = plugin.getResource(resourcePath)) {
            return stream != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
