package org.jenkinsci.plugins.gravatar.cache;

import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.jenkinsci.plugins.gravatar.boundary.GravatarImageURLVerifier;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;
import org.jenkinsci.plugins.gravatar.model.GravatarUser;

class GravatarImageResolutionCacheLoader extends CacheLoader<GravatarUser, Optional<GravatarUrlCreator>> {

    private static final Logger LOG = Logger.getLogger(GravatarImageResolutionCacheLoader.class.getName());

    private final ListeningExecutorService reloader =
            MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    @Override
    public ListenableFuture<Optional<GravatarUrlCreator>> reload(
            final GravatarUser gravatarUser, Optional<GravatarUrlCreator> oldValue) throws Exception {
        // optimization: if it was previously known, it is rather safe to suspect, that it is still known (who removes a
        // gravatar?)
        if (oldValue.isPresent()) {
            LOG.finer("Reusing old gravatar result value for " + gravatarUser + " because it has been known before.");
            return Futures.immediateFuture(oldValue);
        }
        // otherwise, we try it again, maybe it's there now?
        LOG.fine("Scheduling " + gravatarUser + " for reloading");
        ListenableFuture<Optional<GravatarUrlCreator>> future =
                reloader.submit(new Callable<Optional<GravatarUrlCreator>>() {
                    public Optional<GravatarUrlCreator> call() throws Exception {
                        return load(gravatarUser);
                    }
                });
        return future;
    }

    @Override
    public Optional<GravatarUrlCreator> load(GravatarUser gravatarUser) throws Exception {
        if (!gravatarUser.emailAddress().isPresent()) {
            LOG.finer("Cannot check for gravatar for user " + gravatarUser + " since no e-mail address is known");
            return creator();
        }
        if (verifier().verify(gravatarUser.emailAddress().get())) {
            LOG.fine("Verified gravatar for " + gravatarUser);
            return creatorFor(gravatarUser);
        }
        return creator();
    }

    private Optional<GravatarUrlCreator> creator() {
        return Optional.absent();
    }

    private Optional<GravatarUrlCreator> creatorFor(GravatarUser user) {
        return Optional.of(GravatarUrlCreator.of(user));
    }

    private GravatarImageURLVerifier verifier() {
        return new GravatarImageURLVerifier();
    }
}
