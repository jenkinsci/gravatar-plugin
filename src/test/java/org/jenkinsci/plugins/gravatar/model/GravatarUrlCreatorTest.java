package org.jenkinsci.plugins.gravatar.model;

import com.google.common.base.Optional;
import org.jenkinsci.plugins.gravatar.factory.GravatarFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GravatarUrlCreatorTest {

	@Mock
	GravatarUser user;

	GravatarUrlCreator creator;

	@Before
	public void setUp() throws Exception {
		when(user.emailAddress()).thenReturn(Optional.of("eramfelt@gmail.com"));
		creator = spy(GravatarUrlCreator.of(user));
		doReturn(new GravatarFactory().testGravatar()).when(creator).gravatar();
	}

	@Test(expected = NullPointerException.class)
	public void itDoesNotAcceptNullUsers() throws Exception {
		GravatarUrlCreator.of(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void itDoesNotAcceptUsersWithoutEMailAddresses() throws Exception {
		when(user.emailAddress()).thenReturn(Optional.<String>absent());
		GravatarUrlCreator.of(user);
	}

	@Test
	public void itAcceptsUsersWithEmailAddress() throws Exception {
		assertThat(GravatarUrlCreator.of(user), is(not(nullValue())));
	}

	@Test(expected = IllegalArgumentException.class)
	public void itDoesNotAcceptNegativeSizes() throws Exception {
		GravatarUrlCreator creator = creator();
		creator.buildUrlForSize(-2);
	}

	@Test
	public void itBuildsAUrlForPositiveSizes() throws Exception {
		final String url = creator().buildUrlForSize(48);
		assertThat(url, containsString("48"));
		assertThat(url, containsString("gravatar.com"));
	}

	private GravatarUrlCreator creator() {
		return this.creator;
	}
}
