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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.model.User;
import hudson.tasks.Mailer;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UserGravatarResolverTest {

    User user;

    Mailer.UserProperty mailPropertyOfUser;

    GravatarUrlCreator urlCreator;

    UserGravatarResolver resolver;

    @Before
    public void setUp() {
        user = Mockito.mock(User.class);
        mailPropertyOfUser = Mockito.mock(Mailer.UserProperty.class);
        urlCreator = Mockito.mock(GravatarUrlCreator.class);
        resolver = Mockito.spy(new UserGravatarResolver());

        when(user.getId()).thenReturn("user");
        when(user.getProperty(same(Mailer.UserProperty.class))).thenReturn(mailPropertyOfUser);
    }

    @Test
    public void resolverShouldNotFindAnythingForAnUnknownUser() {
        makeUserUnknown();
        assertThat(resolver.findAvatarFor(user, 48, 48), is(nullValue()));
    }

    @Test
    public void resolverShouldNotLookupAnUnknowUser() {
        makeUserUnknown();
        verify(resolver, never()).urlCreatorFor(any(User.class));
    }

    @Test
    public void aKnownUserIsResolved() {
        makeUserKnown();
        assertThat(resolver.findAvatarFor(user, 48, 48), is(not(nullValue())));
        verify(resolver, atLeastOnce()).urlCreatorFor(same(user));
    }

    private void makeUserKnown() {
        doReturn(true).when(resolver).isGravatarUser(any(User.class));
        doReturn(urlCreator).when(resolver).urlCreatorFor(same(user));
        when(urlCreator.buildUrlForSize(anyInt())).thenReturn("http://my.image.com/123123123");
    }

    private void makeUserUnknown() {
        doReturn(false).when(resolver).isGravatarUser(any(User.class));
    }

    /*

    @Test
    public void assertResolverVerifiesThatGravatarExists() {
        UserGravatarResolver resolver = new UserGravatarResolver(urlVerifier);
        when(urlVerifier.verify(anyString())).thenReturn(Boolean.TRUE);
        assertThat(resolver.checkIfGravatarExistsFor("eramfelt@gmail.com"), is(true));
    }

    @Test
    public void assertResolverReturnsNullForNonExistingGravatar() {
        UserGravatarResolver resolver = new UserGravatarResolver(urlVerifier);
        when(urlVerifier.verify(anyString())).thenReturn(Boolean.FALSE);
        assertThat(resolver.checkIfGravatarExistsFor("eramfelt@gmail.com"), is(false));
    }

    @Test
    public void assertResolverOnlyVerifiesAnImageUrlOnce() {
        UserGravatarResolver resolver = new UserGravatarResolver(urlVerifier);
        when(urlVerifier.verify(anyString())).thenReturn(Boolean.TRUE);
        resolver.checkIfGravatarExistsFor("eramfelt@gmail.com");
        resolver.checkIfGravatarExistsFor("eramfelt@gmail.com");
        resolver.checkIfGravatarExistsFor("eramfelt@gmail.com");
        verify(urlVerifier, times(1)).verify("eramfelt@gmail.com");
    }

    */
}
