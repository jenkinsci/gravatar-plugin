package org.jenkinsci.plugins.gravatar.cache;

import com.google.common.base.Optional;
import hudson.model.User;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;

public interface GravatarImageResolutionCache {

    Optional<GravatarUrlCreator> urlCreatorFor(User user);

    boolean hasGravatarCreator(User user);

    void loadIfUnknown(User user);
}
