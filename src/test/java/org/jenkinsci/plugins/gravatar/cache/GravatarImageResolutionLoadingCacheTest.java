package org.jenkinsci.plugins.gravatar.cache;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import hudson.model.User;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;
import org.jenkinsci.plugins.gravatar.model.GravatarUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GravatarImageResolutionLoadingCacheTest {

	private static final String USER_ID = "userId";

	@Mock
	LoadingCache<GravatarUser, Optional<GravatarUrlCreator>> innerCache;

	@Mock
	GravatarUrlCreator cachedKnownCreator;

	@Mock
	User cachedKnownUser;

	@Mock
	User cachedUnknownUser;

	@Mock
	User uncachedUser;

	GravatarImageResolutionLoadingCache cache;

	@Before
	public void setUp() throws Exception {
		when(cachedKnownUser.getId()).thenReturn(USER_ID);
		when(cachedUnknownUser.getId()).thenReturn("EFG");
		when(uncachedUser.getId()).thenReturn("ABD");
		cache = spy(new GravatarImageResolutionLoadingCache(innerCache));

		doReturn(new ConcurrentHashMap<>(mapOfCachedUsers())).when(innerCache).asMap();

		doReturn(Optional.absent()).when(innerCache).getUnchecked(any(GravatarUser.class));
		doReturn(Optional.absent()).when(innerCache).get(any(GravatarUser.class));
		when(innerCache.getUnchecked(eq(GravatarUser.gravatarUser(cachedKnownUser)))).thenReturn(Optional.of(cachedKnownCreator));
		when(innerCache.get(eq(GravatarUser.gravatarUser(cachedKnownUser)))).thenReturn(Optional.of(cachedKnownCreator));
	}

	private ImmutableMap<GravatarUser, Optional<GravatarUrlCreator>> mapOfCachedUsers() {
		GravatarUser cachedGravatarUser = GravatarUser.gravatarUser(cachedKnownUser);
		GravatarUser cachedUnknownGravatarUser = GravatarUser.gravatarUser(cachedUnknownUser);
		return ImmutableMap.of(cachedGravatarUser, Optional.of(cachedKnownCreator), cachedUnknownGravatarUser, Optional.absent());
	}

	@Test
	public void urlCreatorAlwaysAsksTheCache() throws Exception {
		cache.urlCreatorFor(cachedKnownUser);
		verify(innerCache, times(1)).get(any(GravatarUser.class));
	}

	@Test
	public void loadIfPresentDoesNotLoadIntoCacheIfAlreadyThere() throws Exception {
		cache.loadIfUnknown(cachedKnownUser);
		verify(innerCache, never()).get(any(GravatarUser.class));
	}

	@Test
	public void loadIfPresentIsInvokedWhenNonCachedUserIsRequested() throws Exception {
		cache.loadIfUnknown(uncachedUser);
		verify(innerCache, times(1)).get(eq(GravatarUser.gravatarUser(uncachedUser)));
	}

	@Test
	public void itShouldNotHaveAvatarCreatorForUncachedUser() {
		assertThat(cache.hasGravatarCreator(uncachedUser), is(false));
	}

	@Test
	public void itShouldNotHaveGravatarCreatorForUnknownUser() {
		assertThat(cache.hasGravatarCreator(cachedUnknownUser), is(false));
	}

	@Test
	public void itShouldHaveGravatarCreatorForCachedKnownUser() {
		assertThat(cache.hasGravatarCreator(cachedKnownUser), is(true));
	}

	@Test
	public void itShouldNotThrowExceptionIfLoadingFails() throws Exception {
		doThrow(new ExecutionException(new RuntimeException())).when(innerCache).get(any(GravatarUser.class));
		cache.loadIfUnknown(uncachedUser);
	}

	@Test
	public void itShouldReturnAnUnknownUrlCreatorIfLoadingFails() throws Exception {
		doThrow(new ExecutionException(new RuntimeException())).when(innerCache).get(any(GravatarUser.class));
		assertThat(cache.urlCreatorFor(uncachedUser), is(equalTo(Optional.<GravatarUrlCreator>absent())));

	}
}
