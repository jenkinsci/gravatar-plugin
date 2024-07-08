package org.jenkinsci.plugins.gravatar.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import org.jenkinsci.plugins.gravatar.factory.GravatarFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GravatarUrlCreatorTest {

    @Mock
    GravatarUser user;

    GravatarUrlCreator creator;

    @Before
    public void setUp() {
        when(user.emailAddress()).thenReturn(Optional.of("eramfelt@gmail.com"));
        creator = spy(GravatarUrlCreator.of(user));
        doReturn(new GravatarFactory().testGravatar()).when(creator).gravatar();
    }

    @Test(expected = NullPointerException.class)
    public void itDoesNotAcceptNullUsers() {
        GravatarUrlCreator.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void itDoesNotAcceptUsersWithoutEMailAddresses() {
        when(user.emailAddress()).thenReturn(Optional.absent());
        GravatarUrlCreator.of(user);
    }

    @Test
    public void itAcceptsUsersWithEmailAddress() {
        assertThat(GravatarUrlCreator.of(user), is(not(nullValue())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void itDoesNotAcceptNegativeSizes() {
        GravatarUrlCreator creator = creator();
        creator.buildUrlForSize(-2);
    }

    @Test
    public void itBuildsAUrlForPositiveSizes() {
        final String url = creator().buildUrlForSize(48);
        assertThat(url, containsString("48"));
        assertThat(url, containsString("gravatar.com"));
    }

    private GravatarUrlCreator creator() {
        return this.creator;
    }
}
