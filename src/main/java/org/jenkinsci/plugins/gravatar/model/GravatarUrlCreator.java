package org.jenkinsci.plugins.gravatar.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import de.bripkens.gravatar.Gravatar;
import org.jenkinsci.plugins.gravatar.factory.GravatarFactory;

public class GravatarUrlCreator {

    private final String email;

    private GravatarUrlCreator(String email) {
        this.email = email;
    }

    public static GravatarUrlCreator of(GravatarUser user) {
        checkNotNull(user);
        checkArgument(user.emailAddress().isPresent(), "Only users with e-mail address are supported");
        return new GravatarUrlCreator(user.emailAddress().get());
    }

    public String buildUrlForSize(int size) {
        checkArgument(size > 0, "Only positive sizes are allowed.");
        return gravatar().setSize(size).getUrl(email);
    }

    @VisibleForTesting
    Gravatar gravatar() {
        return new GravatarFactory().userGravatar();
    }
}
