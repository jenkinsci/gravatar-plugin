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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import de.bripkens.gravatar.Gravatar;
import hudson.ProxyConfiguration;
import org.jenkinsci.plugins.gravatar.factory.GravatarFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class that verifies that a Gravatar exists for an email.
 */
public class GravatarImageURLVerifier {

	private static final Logger LOG = Logger.getLogger(GravatarImageURLVerifier.class.getName());

	/**
	 * Verifies if the email has an Gravatar
	 * 
	 * @param email email address
	 * @return true, if there is a Gravatar for the emails; false, otherwise.
	 */
	public boolean verify(String email) {
		checkNotNull(email);
		String imageURL = gravatar().getUrl(email);

		boolean gravatarExistsForEmail = false;
		try {
			URI url = new URI(imageURL);
			HttpClient client = ProxyConfiguration.newHttpClientBuilder().followRedirects(HttpClient.Redirect.NORMAL)
					.connectTimeout(Duration.ofSeconds(5)).build();
			HttpRequest request = ProxyConfiguration.newHttpRequestBuilder(url).timeout(Duration.ofSeconds(5))
					.method("HEAD", HttpRequest.BodyPublishers.noBody()).build();

			HttpResponse<?> resp = client.send(request, HttpResponse.BodyHandlers.discarding());

			int gravatarResponseCode = resp.statusCode();
			gravatarExistsForEmail = responseCodeIsOK(gravatarResponseCode);

			LOG.finer("Resolved gravatar for " + email + ". Found: " + gravatarExistsForEmail);
		} catch (URISyntaxException e) {
			LOG.warning("Gravatar URL is malformed, " + imageURL);
		} catch (IOException e) {
			LOG.fine("Could not connect to the Gravatar URL, " + e);
		} catch (InterruptedException e) {
			LOG.fine("Could not connect to the Gravatar URL, " + e);
		}
		return gravatarExistsForEmail;
	}

	@VisibleForTesting
	protected Gravatar gravatar() {
		return new GravatarFactory().verifyingGravatar();
	}

	private boolean responseCodeIsOK(int gravatarResponseCode) {
		System.err.println("gravatarResponseCode: " + gravatarResponseCode);
		return (gravatarResponseCode == HttpURLConnection.HTTP_OK)
				|| (gravatarResponseCode == HttpURLConnection.HTTP_NOT_MODIFIED);
	}

}
