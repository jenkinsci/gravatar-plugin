package org.jenkinsci.plugins.gravatar.cache;

import static org.jenkinsci.plugins.gravatar.model.GravatarUser.gravatarUser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hudson.model.User;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;
import org.jenkinsci.plugins.gravatar.model.GravatarUser;

public class GravatarImageResolutionLoadingCache {

    private static final Logger LOG = Logger.getLogger(GravatarImageResolutionLoadingCache.class.getName());

    private final LoadingCache<GravatarUser, Optional<GravatarUrlCreator>> cache;

    GravatarImageResolutionLoadingCache() {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(2)
                .refreshAfterWrite(30, TimeUnit.MINUTES)
                .initialCapacity(User.getAll().size())
                .build(createUrlForUser());
    }

    @VisibleForTesting
    protected GravatarImageResolutionLoadingCache(LoadingCache<GravatarUser, Optional<GravatarUrlCreator>> cache) {
        this.cache = cache;
    }

    private CacheLoader<GravatarUser, Optional<GravatarUrlCreator>> createUrlForUser() {
        return new GravatarImageResolutionCacheLoader();
    }

    public Optional<GravatarUrlCreator> urlCreatorFor(User user) {
        try {
            return cache.get(keyOf(user));
        } catch (ExecutionException e) {
            return Optional.absent();
        }
    }

    public void loadIfUnknown(User user) {
        GravatarUser gravatarUser = keyOf(user);
        if (!isKnown(gravatarUser)) {
            try {
                cache.get(gravatarUser);
            } catch (ExecutionException e) {
                LOG.info("Failed to load gravatar for user " + gravatarUser);
            }
        }
    }

    boolean isKnown(GravatarUser user) {
        return cache.asMap().containsKey(user);
    }

    public boolean hasGravatarCreator(User user) {
        return hasGravatarCreator(keyOf(user));
    }

    private boolean hasGravatarCreator(GravatarUser user) {
        return isKnown(user) && cache.getUnchecked(user).isPresent();
    }

    private GravatarUser keyOf(User user) {
        return gravatarUser(user);
    }
}
