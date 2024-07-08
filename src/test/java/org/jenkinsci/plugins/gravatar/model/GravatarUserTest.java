package org.jenkinsci.plugins.gravatar.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.testing.EqualsTester;
import hudson.model.User;
import hudson.tasks.Mailer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class GravatarUserTest {

    private MockedStatic<User> mockedUser;

    public static final String EMAIL = "myid@mail.com";
    public static final String USER_ID = "myid";

    User user;

    Mailer.UserProperty mailProperty;

    @Before
    public void setUp() {
        user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(USER_ID);

        mailProperty = Mockito.mock(Mailer.UserProperty.class);
        when(mailProperty.getAddress()).thenReturn(EMAIL);
        mockedUser = Mockito.mockStatic(User.class);
        mockedUser.when(() -> User.getById(eq(USER_ID), eq(false))).thenReturn(user);
    }

    @After
    public void tearDown() {
        mockedUser.closeOnDemand();
    }

    @Test(expected = NullPointerException.class)
    public void itShouldNotAllowNullUsers() {
        GravatarUser.gravatarUser(null);
    }

    @Test
    public void itShouldAllowNonNullUsers() {
        GravatarUser.gravatarUser(user);
    }

    @Test
    public void itShouldNotGiveAnEMailAddressIfNoneAvailable() {
        GravatarUser grUser = GravatarUser.gravatarUser(user);
        assertThat(grUser.emailAddress(), equalTo(Optional.<String>absent()));
    }

    @Test
    public void itShouldReturnEMailAddressReturnedByProperty() {
        when(user.getProperty(Mailer.UserProperty.class)).thenReturn(mailProperty);

        GravatarUser grUser = GravatarUser.gravatarUser(user);
        final Optional<String> email = grUser.emailAddress();

        assertThat(email.isPresent(), is(true));
        assertThat(email.get(), is(equalTo(EMAIL)));
    }

    @Test
    public void anEmptyEmailAddressIsTreatedAsNull() {
        when(mailProperty.getAddress()).thenReturn("");
        when(user.getProperty(Mailer.UserProperty.class)).thenReturn(mailProperty);

        GravatarUser grUser = GravatarUser.gravatarUser(user);
        final Optional<String> email = grUser.emailAddress();
        assertThat(email, is(equalTo(Optional.<String>absent())));
    }

    @Test
    public void itShouldOnlyCallEMailPropertyOnce() {
        GravatarUser grUser = GravatarUser.gravatarUser(user);
        grUser.emailAddress();
        grUser.emailAddress();
        grUser.emailAddress();
        Mockito.verify(user, Mockito.times(1)).getProperty(Mailer.UserProperty.class);
    }

    @Test
    public void itShouldBehaveCorrectlyWithEquals() {
        GravatarUser user1 = user("userid1");
        GravatarUser user1_2 = user("userid1");
        GravatarUser user1_3 = user("userid1");
        GravatarUser user2 = user("userid2");
        GravatarUser user2_2 = user("userid2");
        GravatarUser user2_3 = user("userid2");
        new EqualsTester()
                .addEqualityGroup(user1, user1_2, user1_3)
                .addEqualityGroup(user2, user2_2, user2_3)
                .testEquals();
    }

    private GravatarUser user(String userId) {
        User user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(userId);
        return GravatarUser.gravatarUser(user);
    }
}
