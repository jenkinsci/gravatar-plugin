package org.jenkinsci.plugins.gravatar.factory;

import de.bripkens.gravatar.DefaultImage;
import de.bripkens.gravatar.Gravatar;
import jenkins.model.Jenkins;

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
		return new Gravatar().setHttps(useHttps());
	}

	private boolean useHttps() {
		final Jenkins jenkins = Jenkins.getInstanceOrNull();
		return jenkins != null && jenkins.isRootUrlSecure();
	}
}
