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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import de.bripkens.gravatar.DefaultImage;
import de.bripkens.gravatar.Gravatar;
import hudson.ProxyConfiguration;

/**
 * Class that verifies that a Gravatar exists for an email.
 */
class GravatarImageURLVerifier {

    private final Gravatar gravatar = new Gravatar();

    GravatarImageURLVerifier() {
        gravatar.setSize(50).setStandardDefaultImage(DefaultImage.HTTP_404);
    }
    /**
     * Verifies if the email has an Gravatar
     * @param email email address
     * @return true, if there is a Gravatar for the emails; false, otherwise.
     */
    public boolean verify(String email) {
        String imageURL = gravatar.getUrl(email);
        
        boolean gravtarExistsForEmail = false;
        try {
            URL url = new URL(imageURL);
            HttpURLConnection connection = (HttpURLConnection) ProxyConfiguration.open(url);
            connection.setConnectTimeout(5*1000);
            connection.setReadTimeout(5*1000);
            connection.setRequestMethod("HEAD");
            connection.connect();
            gravtarExistsForEmail = (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
            connection.disconnect();
        } catch (MalformedURLException e) {
            LOGGER.warning("Gravatar URL is malformed, " + imageURL);
        } catch (IOException e) {
            LOGGER.fine("Could not connect to the Gravatar URL, " + e);
        }
        return gravtarExistsForEmail;
    }
    
    private static final Logger LOGGER = Logger.getLogger(GravatarImageURLVerifier.class.getName());
}
