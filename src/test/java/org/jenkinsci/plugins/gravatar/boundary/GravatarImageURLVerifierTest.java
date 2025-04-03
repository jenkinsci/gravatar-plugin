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

package org.jenkinsci.plugins.gravatar.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import org.jenkinsci.plugins.gravatar.factory.GravatarFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GravatarImageURLVerifierTest {

    @Spy
    private GravatarImageURLVerifier urlVerifier = new GravatarImageURLVerifier();

    @BeforeEach
    public void setUp() {
        doReturn(new GravatarFactory().testGravatar()).when(urlVerifier).gravatar();
    }

    @Test
    public void assertVerifierReturnsThatGravatarExists() {
        assertThat(urlVerifier.verify("eramfelt@gmail.com"), is(true));
    }

    @Test
    public void assertVerifierReturnsThatGravatarDoesNotExist() {
        assertThat(urlVerifier.verify("MyEmailAddressABCDE@example.com"), is(false));
    }

    @Test
    public void doesNotAllowNullEMails() {
        assertThrows(NullPointerException.class, () -> urlVerifier.verify(null));
    }
}
