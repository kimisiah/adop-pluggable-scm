
package pluggable.scm.gitlab;

import pluggable.scm.SCMProvider;

import pluggable.configuration.EnvVarProperty;
import pluggable.scm.helpers.ExecuteShellCommand;
import pluggable.scm.helpers.Logger;

/**
* This class implements the Gitlab SCM Provider.
*/
public class GitlabSCMProvider implements SCMProvider {

  private final String scmHost = "";
  private final int scmPort = 0;
  private final GitlabSCMProtocol scmProtocol = null;

  private final String scmGitlabCloneUser = "";
  private final String scmGitlabServerProfile = "";
  private final String scmCodeReviewEnabled = "";

  private final String gitlabEndpoint = "";
  private final String gitlabUser = "";
  private final int gitlabPort = 0;

  private final String gitlabPermissions = "";
  private final String gitlabPermissionsWithReview = "";

  /**
  * Constructor for class GitlabSCMProvider.
  *
  * @param scmHost scm url e.g. 10.0.0.1, gitlab.adop.example
  * @param scmPort scm port
  * @param scmProtocol scm clone protocol
  * @param scmGitlabProfile scm Gitlab profile
  * @param scmGitlabCloneUser scm gitlab clone user. Must be set of the SCM protocol is set to SSH.
  * @param scmCodeReviewEnabled true if code reviewed enabled else false.
  * @param gitlabEndpoint gitlab host endpoint.
  * @param gitlabUser gitlab API user.
  * @param gitlabPort gitlab API port.
  * @param gitlabPermissions gitlab permissions repository name.
  * @param gitlabPermissionsWithReview gitlab permissions with review repository name.
  *
  * @throws IllegalArgumentException
  *         If SCM protocol is equal to GitlabSCMProtocol.SSH and the Gitlab clone user has not been provided.
  *         If Gitlab server profile is not set.
  */
  public GitlabSCMProvider(String scmHost, int scmPort, GitlabSCMProtocol scmProtocol,
    String scmGitlabServerProfile, String scmGitlabCloneUser, String scmCodeReviewEnabled,
    String gitlabEndpoint, String gitlabUser, int gitlabPort,
    String gitlabPermissions, String gitlabPermissionsWithReview){

      this.scmHost = scmHost;
      this.scmPort = scmPort;
      this.scmProtocol = scmProtocol;
      this.scmCodeReviewEnabled = scmCodeReviewEnabled;

      this.gitlabEndpoint = gitlabEndpoint;
      this.gitlabUser = gitlabUser;
      this.gitlabPort = gitlabPort;
      this.gitlabPermissions = gitlabPermissions;
      this.gitlabPermissionsWithReview = gitlabPermissionsWithReview;

      if (scmProtocol == GitlabSCMProtocol.SSH
            && ( scmGitlabCloneUser == null || scmGitlabCloneUser.equals(""))){
        throw new IllegalArgumentException("The Gitlab SCM clone user must be set when using the SSH protocol.");
      }else{
        this.scmGitlabCloneUser = scmGitlabCloneUser;
      }

      if(scmGitlabServerProfile.equals("") || scmGitlabServerProfile.equals(null) ){
        throw new IllegalArgumentException("The Gitlab SCM profile name must be set to use this SCM provider.");
      } else{
        this.scmGitlabServerProfile = scmGitlabServerProfile;
      }
  }

  /**
  * Returns a String representation of the Gitlab server profile name.
  * @return a String representation of the Gitlab server profile name.
  */
  def String getScmGitlabProfile(){
    return this.scmGitlabServerProfile;
  }


  /**
  * Return Gitlab SCM URL.
  * @return SCM url for the provider.
  *     e.g. Gitlab-SSH  ssh://jenkins@10.0.0.0:22/
  *          Gitlab-HTTP http://10.0.0.0:80/
  *
  * @throws IllegalArgumentException
  *           If the SCM protocol type is not supported.
  **/
  public String getScmUrl(){

      StringBuffer url = new StringBuffer("")

      url.append(this.scmProtocol);
      url.append("://");

      switch(this.scmProtocol){
        case GitlabSCMProtocol.SSH:
          url.append(this.scmGitlabCloneUser);
          url.append("@");
          break;

        case GitlabSCMProtocol.HTTP:
        case GitlabSCMProtocol.HTTPS:
          //do nothing
          break;

        default:
          throw new IllegalArgumentException("SCM Protocol type not supported.");
          break;
      }

      url.append(this.scmHost);
      url.append(":");
      url.append(this.scmPort);
      url.append("/");

      return url;
  }

  /**
  * Creates relevant repositories defined by your cartridge in your chosen SCM provider
  * @param workspace Workspace of the cartridge loader job
  * @param namespace Location in your SCM provider where your repositories will be created
  * @param overwriteRepos Whether the contents of your created repositories are over-written or not
  **/
  public void createScmRepos(String workspace, String repoNamespace, String codeReviewEnabled, String overwriteRepos) {

    ExecuteShellCommand com = new ExecuteShellCommand()
    String permissions_repo = null;
    String permissions_repo_temp = null;

    String cartHome = "/cartridge"
    String urlsFile = workspace + cartHome + "/src/urls.txt"

    EnvVarProperty envVarProperty = EnvVarProperty.getInstance();

    // Check if code review has been enabled
    if(codeReviewEnabled.equals("true") && this.scmCodeReviewEnabled.equals("false")){
      throw new IllegalArgumentException("You have tried to use code review however it is not supported for your chosen SCM provider.");
    }

    if (codeReviewEnabled.equals("true")){
      permissions_repo_temp = this.gitlabPermissionsWithReview
    } else {
      permissions_repo_temp = this.gitlabPermissions
    }
    permissions_repo = envVarProperty.returnValue(permissions_repo_temp);

    // Create repositories
    String command1 = "cat " + urlsFile
    List<String> repoList = new ArrayList<String>();
    repoList = (com.executeCommand(command1).split("\\r?\\n"));

    for(String repo: repoList) {
        String repoName = repo.substring(repo.lastIndexOf("/") + 1, repo.indexOf(".git"));
        String target_repo_name= repoNamespace + "/" + repoName
        int repo_exists=0;

        // Check if the repository already exists or not
        String listCommand = "ssh -i " + envVarProperty.getSshPrivateKeyPath() + " -n -o StrictHostKeyChecking=no -p " + this.gitlabPort + " " + this.gitlabUser + "@" + this.gitlabEndpoint + " gitlab ls-projects --type code"
        List<String> gitlabRepoList = (com.executeCommand(listCommand).split("\\r?\\n"));

        for(String gitlabRepo: gitlabRepoList) {
          if(gitlabRepo.trim().contains(target_repo_name)) {
             Logger.info("Found: " + target_repo_name);
             repo_exists=1
             break
          }
        }

        // If not, create it
        if (repo_exists.equals(0)) {
          String createCommand = "ssh -i " + envVarProperty.getSshPrivateKeyPath() + " -n -o StrictHostKeyChecking=no -p " + this.gitlabPort + " " + this.gitlabUser + "@" + this.gitlabEndpoint + " gitlab create-project --parent " + permissions_repo + " " + target_repo_name
          com.executeCommand(createCommand)
          Logger.info("Creating repository in Gitlab: " + target_repo_name);
        } else{
          Logger.info("Repository already exists, skipping create: : " + target_repo_name);
        }

        // Populate repository
        String tempDir = workspace + "/tmp"

        def gitSsh = new File (tempDir + '/git_ssh.sh')
        def tempScript = new File(tempDir + '/shell_script.sh')

        gitSsh << "#!/bin/sh\n"
        gitSsh << "exec ssh -i " + envVarProperty.getSshPrivateKeyPath() + " -o StrictHostKeyChecking=no \"\$@\""

        tempScript << "export GIT_SSH=\""+ tempDir + "/git_ssh.sh\"\n"
        tempScript << "git clone ssh://" + this.gitlabUser + "@" + this.gitlabEndpoint + ":" + this.gitlabPort + "/" + target_repo_name + " " + tempDir + "/" + repoName + "\n"
        def gitDir = "--git-dir=" + tempDir + "/" + repoName + "/.git"
        tempScript << "git " + gitDir + " remote add source " + repo + "\n"
        tempScript << "git " + gitDir + " fetch source" + "\n"

        if (overwriteRepos.equals("true")){
          tempScript << "git " + gitDir + " push origin +refs/remotes/source/*:refs/heads/*\n"
          Logger.info("Repository already exists, overwriting: : " + target_repo_name);
        } else {
          tempScript << "git " + gitDir + " push origin refs/remotes/source/*:refs/heads/*\n"
        }

        com.executeCommand('chmod +x ' + tempDir + '/git_ssh.sh')
        com.executeCommand('chmod +x ' + tempDir + '/shell_script.sh')
        com.executeCommand(tempDir + '/shell_script.sh')

        // delete temp scripts.
        gitSsh.delete()
        tempScript.delete()
    }
  }

  /**
    Return SCM section.

    @param projectName - name of the project.
    @param repoName  - name of the repository to clone.
    @param branchName - name of branch.
    @param credentialId - name of the credential in the Jenkins credential
            manager to use.
    @param extras - extra closures to add to the SCM section.
    @return a closure representation of the SCM providers SCM section.
  **/
  public Closure get(String projectName, String repoName, String branchName, String credentialId, Closure extras){
    if(extras == null) extras = {}
        return {
            git extras >> {
              remote{
                url(this.getScmUrl() + projectName + "/" + repoName)
                credentials(credentialId)
              }
              branch(branchName)
            }
        }
    }

    /**
    * Return a closure representation of the SCM providers trigger SCM section.
    *
    * @param projectName - project name.
    * @param repoName - repository name.
    * @param branchName - branch name to trigger.
    * @return a closure representation of the SCM providers trigger SCM section.
    */
    public Closure trigger(String projectName, String repoName, String branchName) {
        return {
          gitlab {
            events {
                refUpdated()
            }
            project(projectName + '/' + repoName, 'plain:' + branchName)
            configure { node ->
                node / serverName(this.getScmGitlabProfile())
            }
          }
        }
    }
}
