import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.link.IssueLink
import com.opensymphony.workflow.InvalidInputException
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.MutableIssue

def issueManager = ComponentAccessor.getIssueManager()
def issueService = ComponentAccessor.getIssueService()
def commentManager = ComponentAccessor.getCommentManager()
def linkManager = ComponentAccessor.getIssueLinkManager() 
def cfm = ComponentAccessor.getCustomFieldManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def cf = cfm.getCustomFieldObject(10002L)
MutableIssue cf1 = cf.getValue(issue)
log.warn("Задача эпик: " + cf1)

def issues = []
def issueTypes = ["Story"]
def statusesTask = ["Готово"]
  
linkManager.getOutwardLinks(cf1.id).each{
    if(it.getIssueLinkType().getName() == "Epic-Story Link"){
    	issue = it.getDestinationObject()
            if(issue.getIssueType().find{it.name in issueTypes}){
                issues.add(issue)
            } 
    }   
}
log.warn(issues)

def passesCondition = true
def issueInputParameters = issueService.newIssueInputParameters()
def transitionValidationResult = issueService.validateTransition(user, cf1.id, 31, issueInputParameters)
for(String i in issues){
    MutableIssue issue2 = issueManager.getIssueObject(i)
    log.warn(issue2)
    if(!issue2.getStatus().find{it.name in statusesTask}){
    	//throw new InvalidInputException("" + i +" находится не в нужном статусе ")
        log.warn("Задача " + issue2 +" находится не в нужном статусе ")
        passesCondition = false
    } else {
        log.warn("Задача " + issue2 +" находится в нужном статусе ")
    }
}

if(passesCondition){
	if(transitionValidationResult.isValid()){
        issueService.transition(user,transitionValidationResult)
    }
} 
