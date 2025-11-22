package org.jenkinsci.plugins.gravatar.factory;

import de.bripkens.gravatar.DefaultImage;
import de.bripkens.gravatar.Gravatar;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import jenkins.security.csp.AvatarContributor;

public class GravatarFactory {

    public Gravatar verifyingGravatar() {
        return gravatar().setStandardDefaultImage(DefaultImage.HTTP_404);
    }

    public Gravatar userGravatar() {
        return gravatar().setStandardDefaultImage(DefaultImage.MYSTERY_MAN);
    }

    public Gravatar testGravatar() {
        return new Gravatar().setStandardDefaultImage(DefaultImage.HTTP_404);
    }

    private Gravatar gravatar() {
        return new Gravatar().setHttps(true);
    }

    /**
     * Allow loading images from Gravatar HTTPS URLs in Content Security Policy.
     */
    @Initializer(after = InitMilestone.SYSTEM_CONFIG_ADAPTED)
    public static void allowGravatarDomainForCSP() {
        AvatarContributor.allow(Gravatar.HTTPS_URL);
    }
}
