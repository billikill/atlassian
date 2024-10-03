
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.onresolve.scriptrunner.db.DatabaseUtil
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.util.thread.JiraThreadLocalUtils
import org.apache.log4j.Logger
import org.apache.log4j.Level

def log = Logger.getLogger("setValueVoiteApprove")

def commentManager = ComponentAccessor.getCommentManager()
def workflowManager = ComponentAccessor.getWorkflowManager()
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def issueManager = ComponentAccessor.getIssueManager()
//def issueService = ComponentAccessor.getIssueService()
def cfm = ComponentAccessor.getCustomFieldManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

def approvers = cfm.getCustomFieldObject(15002L)
List approversValue = approvers.getValue(issue)
def approved = cfm.getCustomFieldObject(28000L)
def declined = cfm.getCustomFieldObject(28001L)
def workflow = workflowManager.getWorkflow(issue)
def change = changeHistoryManager.getChangeItemsWithFieldsForIssues([issue], ['status']).last()//.getAuthor()
def lastAuthor = change.getAuthorKey()
def lastUser = change.getAuthorObject().getUsername()
log.warn("lastAuthor" + lastAuthor)
log.warn("user : " + user.getUsername())
log.warn("last User : " + lastUser)
def timeTransitions = change.getTimePerformed()
def output
int actionId
def actionNme
List usersApproved = (approved.getValue(issue))?:[]
List usersDeclined = (declined.getValue(issue))?:[]

DatabaseUtil.withSql('LocalBase') { sql ->
    output = sql.rows("select ACTION_ID from OS_HISTORYSTEP where CALLER = ? and FINISH_DATE = ?", lastAuthor, timeTransitions)
}
output.each{
    log.warn(it)
    it.values().findAll { value ->
        log.warn("value : " + value)
        actionId = value
    }
}

def wfd = workflow.getDescriptor()
actionNme = wfd.getAction(actionId)//.getName()
def comment = "user :" +change.authorDisplayName + " сделал переход : " + actionNme
log.warn("user :" +change.authorDisplayName + " сделал переход : " + actionNme)
//commentManager.create(issue, user, comment, false)
if (actionNme.toString() == "Approve"){
    usersApproved.add(change.getAuthorObject())
    approversValue.remove(change.getAuthorObject())
    issue.setCustomFieldValue(approved,usersApproved)
    issue.setCustomFieldValue(approvers,approversValue)  
    log.warn("deleted : " + approversValue.remove(change.getAuthorObject()))
}
if(actionNme.toString() == "Decline"){
    usersDeclined.add(change.getAuthorObject())
    approversValue.remove(change.getAuthorObject())
    issue.setCustomFieldValue(declined,usersDeclined)
    issue.setCustomFieldValue(approvers,approversValue)  
    log.warn("deleted : " + approversValue.remove(change.getAuthorObject()))
}

issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false)

if(usersDeclined.isEmpty() && approversValue.isEmpty()){
    //issueTransition(issue, 11, user)
    log.warn("Все проголосавали и нет отклоненных ")
    new Thread(JiraThreadLocalUtils.wrap {            
        issueTransition(issue, 11, user)
    }).start()
}
if(!usersDeclined.isEmpty() && approversValue.isEmpty()){
    //issueTransition(issue, 21, user)
    log.warn("Все проголосовали и есть отклоненные ")
    new Thread(JiraThreadLocalUtils.wrap {            
        issueTransition(issue, 21, user)
    }).start()
} 


def issueTransition(MutableIssue issue, int transition, ApplicationUser user){
    def issueService = ComponentAccessor.getIssueService()
    def issueInputParameters = issueService.newIssueInputParameters()
	def transitionValidationResult = issueService.validateTransition(user, issue.id, transition, issueInputParameters)
    if(transitionValidationResult.isValid()){
			issueService.transition(user,transitionValidationResult)
	}    
}

