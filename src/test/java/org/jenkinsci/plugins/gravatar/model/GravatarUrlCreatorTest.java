package org.jenkinsci.plugins.gravatar.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import org.jenkinsci.plugins.gravatar.factory.GravatarFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GravatarUrlCreatorTest {

    @Mock
    GravatarUser user;

    GravatarUrlCreator creator;

    @BeforeEach
    public void setUp() {
        when(user.emailAddress()).thenReturn(Optional.of("eramfelt@gmail.com"));
        creator = spy(GravatarUrlCreator.of(user));
        doReturn(new GravatarFactory().testGravatar()).when(creator).gravatar();
    }

    @Test
    public void itDoesNotAcceptNullUsers() {
        assertThrows(NullPointerException.class, () -> GravatarUrlCreator.of(null));
    }

    @Test
    public void itDoesNotAcceptUsersWithoutEMailAddresses() {
        when(user.emailAddress()).thenReturn(Optional.absent());
        assertThrows(IllegalArgumentException.class, () -> GravatarUrlCreator.of(user));
    }

    @Test
    public void itAcceptsUsersWithEmailAddress() {
        assertThat(GravatarUrlCreator.of(user), is(not(nullValue())));
    }

    @Test
    public void itDoesNotAcceptNegativeSizes() {
        var creator = creator();
        assertThrows(IllegalArgumentException.class, () -> creator.buildUrlForSize(-2));
    }

    @Test
    public void itBuildsAUrlForPositiveSizes() {
        final var url = creator().buildUrlForSize(48);
        assertThat(url, containsString("48"));
        assertThat(url, containsString("gravatar.com"));
    }

    private GravatarUrlCreator creator() {
        return this.creator;
    }
}
