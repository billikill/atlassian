package com.onresolve.jira.groovy.jql
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.util.MessageSetImpl
import com.atlassian.jira.JiraDataType
import com.atlassian.jira.JiraDataTypes
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.jql.operand.QueryLiteral
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.permission.ProjectPermissions
import com.atlassian.jira.project.version.VersionManager
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import com.atlassian.jira.issue.link.IssueLink
import groovy.json.JsonSlurper
import is.origo.jira.plugin.common.TempoWorklogManager
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.tempoplugin.team.api.event.TeamRoleEvent
@WithPlugin("is.origo.jira.tempo-plugin")
@PluginModule
TempoWorklogManager tempoWorklogManager

class inRole extends AbstractScriptedJqlFunction implements JqlFunction {

    @Override
    String getDescription() {
        "Get users in Role in Team "
    }


    @Override
    List<Map> getArguments() {
        [
                [
                        description: "Name Team",
                        optional: false,
                ],
                [
                        description: "Name Role",
                        optional: false,
                ],
        ]
    }


    @Override
    String getFunctionName() {
        "inRole"
    }


    @Override
    JiraDataType getDataType() {
        JiraDataTypes.ISSUE
    }

    @Override
    MessageSet validate(ApplicationUser user, FunctionOperand operand, TerminalClause terminalClause) {
        Issue issue = ComponentAccessor.getIssueManager().getIssueObject(operand.args.first())
        MessageSet messages = new MessageSetImpl()
        if (issue == null) {
            messages.addErrorMessage ("Issue not found")
        }
        return messages
    }


    @Override
    List<QueryLiteral> getValues( QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {

        List<QueryLiteral> out = []
        def name_team = null
        def name_role = null
        def conf = null
        authenticatedRequestFactory
                .createRequest(Request.MethodType.GET, "https://jira.xxx.ru/rest/tempo-teams/2/team/" + name_team + "/member" )
                .addHeader("Content-Type", "application/json")
                .execute(new ResponseHandler<Response>() {
            @Override
            void handle(Response response) throws ResponseException {
                conf = new JsonSlurper().parseText(response.getResponseBodyAsString())
            }
        })
        log.debug("Список участников команды с ролями :" + conf.member)

        def params =[
                member: name,
                membership: [
                        role: [
                                name: name_role,
                        ]
                ]
        ]



    }
}

