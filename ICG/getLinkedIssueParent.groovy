import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.link.IssueLink

def issueService = ComponentAccessor.getIssueService()
def linkManager = ComponentAccessor.getIssueLinkManager() 
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

def liIssue =[]
def parentIssue
def passesCondition = true
linkManager.getOutwardLinks(issue.id).each{ issueLink ->
    log.warn("Parent issue : " + issueLink.getDestinationObject())
    parentIssue = issueLink.getDestinationObject()
    linkManager.getInwardLinks(parentIssue.id).each{
        log.warn("Linked issue parentIssue : " + it.getSourceObject())
        def linkedIssueParrent = it.getSourceObject()
        //log.warn("Resolurion : " + linkedIssueParrent.getResolution())
        if(!linkedIssueParrent.getResolution()){
            log.warn("issue not closed : " + linkedIssueParrent.getKey())
            liIssue.add(linkedIssueParrent)
            passesCondition = false
        }
    } 
}
if(passesCondition){
def issueInputParameters = issueService.newIssueInputParameters()
def transitionValidationResult = issueService.validateTransition(user, parentIssue.getId(), 331, issueInputParameters)
  log.warn("all issue closed")
	if(transitionValidationResult.isValid()){
         issueService.transition(user,transitionValidationResult)
   }
} 
