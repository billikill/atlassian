package com.onresolve.jira.groovy.jql
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.util.MessageSetImpl
import com.atlassian.jira.JiraDataType
import com.atlassian.jira.JiraDataTypes
import com.atlassian.jira.jql.operand.QueryLiteral
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.permission.ProjectPermissions
import com.atlassian.jira.project.version.VersionManager
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import com.tempoplugin.team.api.TeamManager
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.tempoplugin.team.api.TeamService
import com.tempoplugin.team.api.role.TeamRole

@WithPlugin ("com.tempoplugin.tempo-teams")


class TeamInRole extends AbstractScriptedJqlFunction implements JqlFunction {
    TeamManager teamManager  = ScriptRunnerImpl.getPluginComponent(TeamManager)

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
                //[
                //        description: "Name Role",
                //       optional: false,
                //],
        ]
    }


    @Override
    String getFunctionName() {
        "TeamInRole"
    }


    @Override
    JiraDataType getDataType() {
        JiraDataTypes.ISSUE
    }

    @Override
    MessageSet validate(ApplicationUser user, FunctionOperand operand, TerminalClause terminalClause) {
        def name_team = teamManager.getTeamByName(operand.args.first())
        //def name_role = teamR

        MessageSet messages = new MessageSetImpl()
        if (name_team == null) {
            messages.addErrorMessage ("Team not found")
        }
        //if (name_role == null){
        //    messages.addErrorMessage ("Role not found")
        //}
        return messages
    }


    @Override
    List<QueryLiteral> getValues( QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {

        List<QueryLiteral> out = []




    }
}


