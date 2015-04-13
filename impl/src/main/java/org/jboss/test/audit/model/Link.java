package org.jboss.test.audit.model;

/**
 * @author Tomas Remes
 */
public class Link {

    private final Provider provider;
    private final String url;

    public Link(Provider provider, String url) {
        this.provider = provider;
        this.url = url;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getUrl() {
        return url;
    }
    
    public static enum Provider{
        GITHUB, SVN, FISHEYE;
        
    }

}
