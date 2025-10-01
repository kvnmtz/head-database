package dev.kvnmtz.headdatabase.config;

import com.google.common.base.Suppliers;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.function.Supplier;

public interface HeadDatabaseServerConfig {

    enum PermissionMode {
        OP_ONLY,
        WHITELIST,
        EVERYONE
    }

    PermissionMode getPermissionMode();

    int getRequiredOpLevel();

    List<String> getWhitelist();

    void reload();

    Supplier<HeadDatabaseServerConfig> INSTANCE = Suppliers.memoize(() -> {
        throw new NotImplementedException("Config not initialized for platform");
    });

    static HeadDatabaseServerConfig get() {
        return INSTANCE.get();
    }

    static void setInstance(HeadDatabaseServerConfig config) {
        try {
            var field = INSTANCE.getClass().getDeclaredField("delegate");
            field.setAccessible(true);
            field.set(INSTANCE, Suppliers.ofInstance(config));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set config instance", e);
        }
    }
}
