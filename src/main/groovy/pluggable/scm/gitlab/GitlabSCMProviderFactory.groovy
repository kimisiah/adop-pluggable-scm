
package pluggable.scm.gitlab;

import pluggable.scm.SCMProvider;
import pluggable.scm.SCMProviderFactory;
import pluggable.scm.SCMProviderInfo;

/**
* The Gitlab SCM factory class is responsible for parsing the
* providers properties and instantiating a GitlabSCMProvider.
*/
@SCMProviderInfo(type="gitlab")
public class GitlabSCMProviderFactory implements SCMProviderFactory {

  /**
  * A factory method which return an SCM Provider instantiated with the
  * the provided properties.
  *
  * @param scmProviderProperties - properties for the SCM provider.
  * @return SCMProvider configured from the provided SCM properties.
  **/
  public SCMProvider create(Properties scmProviderProperties){

    GitlabSCMProvider scmProvider = null;

    String scmHost = scmProviderProperties.getProperty("scm.host");
    String scmProtocol = scmProviderProperties.getProperty("scm.protocol");
    int scmPort = Integer.parseInt(scmProviderProperties.getProperty("scm.port"));

    String scmGitlabProfile = scmProviderProperties.getProperty("scm.gitlab.server.profile");
    String scmGitlabCloneUser = scmProviderProperties.getProperty("scm.gitlab.ssh.clone.user");
    String scmCodeReviewEnabled = scmProviderProperties.getProperty("scm.code_review.enabled");

    String gitlabEndpoint = scmProviderProperties.getProperty("gitlab.endpoint");
    String gitlabUser = scmProviderProperties.getProperty("gitlab.user");
    int gitlabPort = Integer.parseInt(scmProviderProperties.getProperty("gitlab.port"));

    String gitlabPermissions = scmProviderProperties.getProperty("gitlab.permissions.path");
    String gitlabPermissionsWithReview = scmProviderProperties.getProperty("gitlab.permissions.with_review.path");

    scmProvider = new GitlabSCMProvider(scmHost, scmPort, GitlabSCMProtocol.valueOf(scmProtocol.toUpperCase()), scmGitlabProfile,
      scmGitlabCloneUser, scmCodeReviewEnabled, gitlabEndpoint, gitlabUser, gitlabPort, gitlabPermissions, gitlabPermissionsWithReview);

    return scmProvider;
  }
}
