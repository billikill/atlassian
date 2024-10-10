import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.servicedesk.api.comment.ServiceDeskCommentService
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
@WithPlugin("com.atlassian.servicedesk")
def serviceDeskCommentService = ComponentAccessor.getOSGiComponentInstanceOfType(ServiceDeskCommentService)
def createCommentParameters = serviceDeskCommentService.newCreateBuilder()
            .author(user)
            .body(text)
            .issue(issue)
            .publicComment(true)
            .build()

    serviceDeskCommentService.createServiceDeskComment(user, createCommentParameters)
