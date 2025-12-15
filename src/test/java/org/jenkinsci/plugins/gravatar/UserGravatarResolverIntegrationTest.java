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

import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.startsWith;

import hudson.model.User;
import hudson.tasks.Mailer;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlPage;
import org.jenkinsci.plugins.gravatar.cache.GravatarImageResolutionCacheInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.xml.sax.SAXException;

@WithJenkins
public class UserGravatarResolverIntegrationTest {

    private JenkinsRule j = new JenkinsRule();

    private JenkinsRule.WebClient wc;

    @BeforeEach
    public void setUp(JenkinsRule j) {
        this.j = j;
        wc = j.createWebClient();
    }

    @Test
    public void testUserWithoutEmailAddressUsesDefaultImage() throws Exception {
        newUser("user-no-email");
        j.assertAllImageLoadSuccessfully(goAndWaitForLoadOfPeople());
        j.assertAllImageLoadSuccessfully(wc.goTo("user/user-no-email"));
    }

    @Test
    public void testNonExistingGravatarUsesDefaultImage() throws Exception {
        var user = newUser("user");
        user.addProperty(new Mailer.UserProperty("MyEmailAddress@example.com"));

        j.assertAllImageLoadSuccessfully(goAndWaitForLoadOfPeople());
        j.assertAllImageLoadSuccessfully(wc.goTo("user/user"));

        assertThat(
                ((DomElement) wc.goTo("user/user").querySelector(".jenkins-app-bar--with-icon"))
                        .getElementsByTagName("img"),
                empty());
    }

    @Test
    public void testGravatarIsUsedForUser() throws Exception {
        var user = newUser("user-e");
        user.addProperty(new Mailer.UserProperty("eramfelt@gmail.com"));
        prefetchImage(user);

        j.assertAllImageLoadSuccessfully(goAndWaitForLoadOfPeople());
        j.assertAllImageLoadSuccessfully(wc.goTo("user/user-e"));

        var images = ((DomElement) wc.goTo("user/user-e").querySelector(".jenkins-app-bar--with-icon"))
                .getElementsByTagName("img");
        assertThat(images.getLength(), greaterThan(0));
        var element = images.get(0);
        assertThat(element.getAttribute("src"), startsWith("https://secure.gravatar.com"));
    }

    private void prefetchImage(User user) {
        GravatarImageResolutionCacheInstance.INSTANCE.urlCreatorFor(user);
    }

    @Test
    public void testManyManyUsersWillNotBlockLoadingOfUsersPage() throws Exception {
        final int howMany = 1000;

        var c = new Callable<HtmlPage>() {
            public HtmlPage call() throws Exception {
                createManyManyUsers(howMany);
                return goAndWaitForLoadOfPeople();
            }
        };
        var executorService = Executors.newSingleThreadExecutor();
        var pageFuture = executorService.submit(c);
        var page = pageFuture.get(60, TimeUnit.SECONDS); // if it takes longer than this we consider it to BLOCK!
        j.assertAllImageLoadSuccessfully(page);
        assertThatUserCount(page, equalTo(howMany));
    }

    private void assertThatUserCount(HtmlPage page, Matcher<Integer> integerMatcher) {
        List<HtmlAnchor> userLinks = page.getByXPath("//a[contains(@href,'/user/')]");
        var targets = newHashSetWithExpectedSize(userLinks.size());
        for (var userLink : userLinks) {
            targets.add(userLink.getHrefAttribute());
        }
        assertThat(targets.size(), integerMatcher);
    }

    private HtmlPage goAndWaitForLoadOfPeople() throws InterruptedException, IOException, SAXException {
        var htmlPage = wc.goTo("asynchPeople");
        var status = getStatus(htmlPage);
        while (status != null && status.isDisplayed()) {
            // the asynch part has not yet finished, so we wait.
            Thread.sleep(500);
            status = getStatus(htmlPage);
        }
        return htmlPage;
    }

    private DomElement getStatus(HtmlPage htmlPage) {
        final var statusById = htmlPage.getElementById("status");
        if (statusById != null) {
            return statusById;
        }
        for (var next : htmlPage.getElementsByTagName("table")) {
            if ("progress-bar".equalsIgnoreCase(next.getAttribute("class"))) {
                return next;
            }
        }
        return null;
    }

    private void createManyManyUsers(int howMany) throws IOException {
        for (int i = 0; i < howMany; i++) {
            var user = newUser("manymanyuser" + i);
            user.addProperty(new Mailer.UserProperty("user" + i + "@gmail.com"));
        }
    }

    private User newUser(String userId) {
        return User.get(userId, true, Collections.emptyMap());
    }
}
