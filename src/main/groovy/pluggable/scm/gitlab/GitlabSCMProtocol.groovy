
package pluggable.scm.gitlab;

/**
* Set of named constants representing the supported Gitlab SCM protocols.
*/
enum GitlabSCMProtocol {
    SSH("ssh"),
    HTTP("http"),
    HTTPS("https")

    private final String protocol = "";

    /**
    * Constructor for class GitlabSCMProtocol.
    *
    * @param protocal a string representation of the protocol e.g. ssh, https
    */
    public GitlabSCMProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
    * Return a string representation of the SCM protocol.
    * @return a string representation of the SCM protocol.
    */
    @Override
    public String toString(){
      return this.protocol;
    }
}
