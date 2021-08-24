package org.jenkinsci.plugins.gravatar.cache;

import com.google.common.base.Optional;
import hudson.model.User;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;

public enum GravatarImageResolutionCacheInstance implements GravatarImageResolutionCache {
	 INSTANCE;

	final GravatarImageResolutionLoadingCache cache = new GravatarImageResolutionLoadingCache();

	public Optional<GravatarUrlCreator> urlCreatorFor(User user) {
		return cache.urlCreatorFor(user);
	}

	public void loadIfUnknown(User user) {
		cache.loadIfUnknown(user);
	}

	public boolean hasGravatarCreator(User user) {
		return cache.hasGravatarCreator(user);
	}

}
