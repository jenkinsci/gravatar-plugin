package org.jenkinsci.plugins.gravatar.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.testing.EqualsTester;
import hudson.model.User;
import hudson.tasks.Mailer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class GravatarUserTest {

    private MockedStatic<User> mockedUser;

    public static final String EMAIL = "myid@mail.com";
    public static final String USER_ID = "myid";

    User user;

    Mailer.UserProperty mailProperty;

    @BeforeEach
    public void setUp() {
        user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(USER_ID);

        mailProperty = Mockito.mock(Mailer.UserProperty.class);
        when(mailProperty.getAddress()).thenReturn(EMAIL);
        mockedUser = Mockito.mockStatic(User.class);
        mockedUser.when(() -> User.getById(eq(USER_ID), eq(false))).thenReturn(user);
    }

    @AfterEach
    public void tearDown() {
        mockedUser.closeOnDemand();
    }

    @Test
    public void itShouldNotAllowNullUsers() {
        assertThrows(NullPointerException.class, () -> GravatarUser.gravatarUser(null));
    }

    @Test
    public void itShouldAllowNonNullUsers() {
        GravatarUser.gravatarUser(user);
    }

    @Test
    public void itShouldNotGiveAnEMailAddressIfNoneAvailable() {
        var grUser = GravatarUser.gravatarUser(user);
        assertThat(grUser.emailAddress(), equalTo(Optional.<String>absent()));
    }

    @Test
    public void itShouldReturnEMailAddressReturnedByProperty() {
        when(user.getProperty(Mailer.UserProperty.class)).thenReturn(mailProperty);

        var grUser = GravatarUser.gravatarUser(user);
        final var email = grUser.emailAddress();

        assertThat(email.isPresent(), is(true));
        assertThat(email.get(), is(equalTo(EMAIL)));
    }

    @Test
    public void anEmptyEmailAddressIsTreatedAsNull() {
        when(mailProperty.getAddress()).thenReturn("");
        when(user.getProperty(Mailer.UserProperty.class)).thenReturn(mailProperty);

        var grUser = GravatarUser.gravatarUser(user);
        final var email = grUser.emailAddress();
        assertThat(email, is(equalTo(Optional.<String>absent())));
    }

    @Test
    public void itShouldOnlyCallEMailPropertyOnce() {
        var grUser = GravatarUser.gravatarUser(user);
        grUser.emailAddress();
        grUser.emailAddress();
        grUser.emailAddress();
        Mockito.verify(user, Mockito.times(1)).getProperty(Mailer.UserProperty.class);
    }

    @Test
    public void itShouldBehaveCorrectlyWithEquals() {
        var user1 = user("userid1");
        var user1_2 = user("userid1");
        var user1_3 = user("userid1");
        var user2 = user("userid2");
        var user2_2 = user("userid2");
        var user2_3 = user("userid2");
        new EqualsTester()
                .addEqualityGroup(user1, user1_2, user1_3)
                .addEqualityGroup(user2, user2_2, user2_3)
                .testEquals();
    }

    private GravatarUser user(String userId) {
        var user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(userId);
        return GravatarUser.gravatarUser(user);
    }
}
