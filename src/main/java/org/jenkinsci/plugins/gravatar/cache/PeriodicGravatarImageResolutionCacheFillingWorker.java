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
package org.jenkinsci.plugins.gravatar.cache;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.PeriodicWork;
import hudson.model.TaskListener;
import hudson.model.User;
import java.io.IOException;
import java.util.Collection;

/**
 * Async periodic worker that updates the cached map in {@link org.jenkinsci.plugins.gravatar.UserGravatarResolver}
 * It will run at startup and every 30 minutes to check if any user has set a gravatar
 * since last run. The {@link org.jenkinsci.plugins.gravatar.UserGravatarResolver} will cache the check for gravatars
 * so the time required when showing the People pages will be as short as possible. This
 * worker task makes sure that the cache is updated every 30 minutes.
 *
 * @author Erik Ramfelt
 */
@Extension
public class PeriodicGravatarImageResolutionCacheFillingWorker extends AsyncPeriodicWork {

    public PeriodicGravatarImageResolutionCacheFillingWorker() {
        super("Gravatar periodic lookup");
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        for (User user : getAllUsers()) {
            cache().loadIfUnknown(user);
        }
    }

    @VisibleForTesting
    Collection<User> getAllUsers() {
        return User.getAll();
    }

    @Override
    public long getRecurrencePeriod() {
        return PeriodicWork.MIN * 30;
    }

    @Override
    public long getInitialDelay() {
        return PeriodicWork.MIN;
    }

    @VisibleForTesting
    GravatarImageResolutionCache cache() {
        return GravatarImageResolutionCacheInstance.INSTANCE;
    }
}
