//https://community.atlassian.com/t5/App-Central-articles/Interacting-with-Insight-objects-from-Scriptrunner-Server-Data/ba-p/1649857

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.MutableIssue
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade
import com.atlassian.jira.bc.issue.properties.IssuePropertyService

import org.apache.log4j.Logger
import org.apache.log4j.Level

@WithPlugin("com.riadalabs.jira.plugins.insight")
def log = Logger.getLogger("createIssueApprove")
//log.setLevel(Level.DEBUG)

def objectTypeAttributeFacade =ComponentAccessor.getOSGiComponentInstanceOfType(ObjectTypeAttributeFacade.class)
def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ObjectFacade.class)
def objectTypeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ObjectTypeFacade.class)
def issueFactory = ComponentAccessor.getIssueFactory()
def cfm = ComponentAccessor.getCustomFieldManager()
def userManager = ComponentAccessor.getUserManager()
def issueManager = ComponentAccessor.getIssueManager()
def linkManager = ComponentAccessor.getIssueLinkManager()
def user = ComponentAccessor.getUserManager().getUserByName("Мастер задач")
def approvers = cfm.getCustomFieldObject(15002L)
def cf = cfm.getCustomFieldObject(27701L).getValue(issue)
def qq = ["SDM", "SRM"]
List selectedUsers = []
log.warn("cf : "+ cf)
cf.each { 
    def objectVal = objectFacade.loadObjectBean(it.getId())
    log.warn("11 : " + objectVal)
    log.warn("11.1 ID " + objectVal.getId())
    log.warn("11.2 Label " + objectVal.getLabel())
    log.warn("11.3 Name " + objectVal.getName())
    def objectType = objectTypeFacade.loadObjectType(objectVal.objectTypeId)
    log.warn("22 : " + objectType)
    for(String approv : qq) { 
        log.warn("it : " + approv)
        //def attributeBean = objectTypeAttributeFacade.findObjectTypeAttributeBeans(objectType.id).find{ it.name == it}
        def attributeBean = objectTypeAttributeFacade.findObjectTypeAttributeBeans(objectType.id).find{ it.name == approv}
        log.warn("33 : " + attributeBean)
        log.warn("33.1 : " + objectVal.objectAttributeBeans.find{it.objectTypeAttributeId == attributeBean.id})
        def attributeValue
        if (objectVal.objectAttributeBeans.find{it.objectTypeAttributeId == attributeBean.id}){
            attributeValue = objectVal.objectAttributeBeans.find{it.objectTypeAttributeId == attributeBean.id}.objectAttributeValueBeans*.value//.objectAttributeValueBeans*.value
            log.warn("44 : " + attributeValue[0])
            log.warn("approve : " + approv + " user : " + attributeValue[0])
            //selectedUsers = [userManager.getUserByKey(attributeValue[0])]         
            selectedUsers.add(userManager.getUserByKey(attributeValue[0]))
        } //else {
          //  log.warn("у команды нет указан проект : " +  objectVal)
        //}
    }
    log.warn("###########################")
    
    //log.warn(selectedUsers)     
    //selectedUsers.removeAll(selectedUsers)
    if(!selectedUsers.isEmpty()){
        MutableIssue newIssue = issueFactory.getIssue()
        newIssue.setProjectId(19100)
        newIssue.setIssueTypeId("10601")
        newIssue.setSummary(issue.getSummary() + " ### " + objectVal)
        newIssue.setCustomFieldValue(approvers,selectedUsers)
        issueManager.createIssueObject(user, newIssue)
        newIssue.entityProperties.setInteger('keyAssetc', objectVal.getId())
        linkManager.createIssueLink(newIssue.id, issue.id, 10800L, 0, user)
        selectedUsers.removeAll(selectedUsers)
    }
    
} 
