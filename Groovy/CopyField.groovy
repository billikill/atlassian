import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.issue.Issue

Issue issue = ComponentAccessor.getIssueManager().getIssueObject('TEST-24')
def issueLinkManager = ComponentAccessor.getIssueLinkManager()

// Получаем связанные заадчи у задачи с типом "Blocks"
//def issueLinks = issueLinkManager.getInwardLinks(issue.getId())
def issueLinks = issueLinkManager.getOutwardLinks(issue.getId())

def links = issueLinks.findAll {
    it.getIssueLinkType().getName() == "Blocks"
}
log.error('Номер связанной задачи ' + links)
for(link in links){
    log.error('Номер связанной задачи ' + link.destinationObject.getKey())
    def epicLink = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName('Epic Link')
    def value = issue.getCustomFieldValue(epicLink)


}
