package org.jenkinsci.plugins.gravatar.cache;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import hudson.model.TaskListener;
import hudson.model.User;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PeriodicGravatarImageResolutionCacheFillingWorkerTest {

    private List<User> users = Lists.newArrayList();

    @Spy
    PeriodicGravatarImageResolutionCacheFillingWorker loader = new PeriodicGravatarImageResolutionCacheFillingWorker();

    @Mock
    GravatarImageResolutionCache cache;

    @Mock
    TaskListener taskListener;

    @Before
    public void setUp() {
        for (int i = 0; i < 100; i++) {
            User user = mock(User.class);
            users.add(user);
        }
        doReturn(users).when(loader).getAllUsers();
        doReturn(cache).when(loader).cache();
    }

    @Test
    public void itShouldForceLoadOfAllUsers() throws Exception {
        loader.execute(taskListener);
        for (User user : users) {
            verify(cache, atLeastOnce()).loadIfUnknown(same(user));
        }
    }

    @Test
    public void itShouldNotForceLoadOfAnyOtherUsers() throws Exception {
        loader.execute(taskListener);
        verify(cache, times(users.size())).loadIfUnknown(any(User.class));
    }
}
