/*
 * The MIT License
 *
 * Copyright (c) 2011, Erik Ramfelt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.gravatar;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.model.User;
import hudson.tasks.UserAvatarResolver;
import java.util.logging.Logger;
import org.jenkinsci.plugins.gravatar.cache.GravatarImageResolutionCache;
import org.jenkinsci.plugins.gravatar.cache.GravatarImageResolutionCacheInstance;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;

/**
 * UserAvatarResolver that returns Gravatar image URLs for Jenkins users.
 */
@Extension
public class UserGravatarResolver extends UserAvatarResolver {

    private static final Logger LOG = Logger.getLogger(UserGravatarResolver.class.getName());

    @Override
    public String findAvatarFor(User user, int width, int height) {
        if (isGravatarUser(user)) {
            LOG.finest("Resolving gravatar url for user " + user.getId() + " in size " + width + "x" + height);
            var urlCreator = urlCreatorFor(user);
            return urlCreator.buildUrlForSize(width);
        }
        return null; // we cannot contribute to the avatar resolution for this user
    }

    @VisibleForTesting
    protected GravatarUrlCreator urlCreatorFor(User user) {
        return cache().urlCreatorFor(user).get();
    }

    boolean isGravatarUser(User user) {
        return cache().hasGravatarCreator(user);
    }

    @VisibleForTesting
    GravatarImageResolutionCache cache() {
        return GravatarImageResolutionCacheInstance.INSTANCE;
    }
}
