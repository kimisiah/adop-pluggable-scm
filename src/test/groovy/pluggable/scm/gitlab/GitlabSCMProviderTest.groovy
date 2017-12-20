
import pluggable.scm.*;
import pluggable.scm.gitlab.*;

import java.lang.*;
import java.lang.reflect.*;
import java.util.Properties;

public class GitlabSCMProviderTest extends GroovyTestCase {

  public void testGetScmUrlHttp(){
    GitlabSCMProvider scmProvider = new GitlabSCMProvider("10.0.0.1",80,
     GitlabSCMProtocol.HTTP, "ADOP Gitlab", "jenkins", "true", "10.0.0.1",
     "jenkins", 22, "All-Projects/permissions", "All-Projects/permissions-with-review");

    assertEquals scmProvider.getScmUrl(), "http://10.0.0.1:80/"
  }

  public void testGetScmUrlSsh(){
    GitlabSCMProvider scmProvider = new GitlabSCMProvider("10.0.0.1",22,
    GitlabSCMProtocol.SSH, "ADOP Gitlab", "jenkins", "true", "10.0.0.1",
    "jenkins", 22, "All-Projects/permissions", "All-Projects/permissions-with-review");

    assertEquals scmProvider.getScmUrl(), "ssh://jenkins@10.0.0.1:22/"
  }
}
