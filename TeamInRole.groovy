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
import com.onresolve.jira.groovy.jql.AbstractScriptedJqlFunction
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.tempoplugin.team.api.TeamService
import com.tempoplugin.team.api.role.TeamRole
import com.tempoplugin.team.api.role.TeamRoleService
import org.apache.log4j.Level
import org.apache.log4j.Logger




@WithPlugin ("com.tempoplugin.tempo-teams")
class TeamInRole extends AbstractScriptedJqlFunction implements JqlFunction {
    TeamManager teamManager  = ScriptRunnerImpl.getPluginComponent(TeamManager)
    TeamService teamService  = ScriptRunnerImpl.getPluginComponent(TeamService)

    @Override
    String getDescription() {
        "Get members in role in team "
    }


    @Override
    List<Map> getArguments() {
        [
                [ description: "Name Team", optional: false ],
                [ description: "Name Role", optional: false ]
        ]
    }


    @Override
    String getFunctionName() {
        "teamInRole"
    }


    @Override
     JiraDataType getDataType() {
        JiraDataTypes.ISSUE
    }

    @Override
    MessageSet validate(ApplicationUser user, FunctionOperand operand, TerminalClause terminalClause) {

        MessageSet messages = new MessageSetImpl()
        def name_team = teamManager.getTeamByName(operand.args.get(0))
        //log.error("Получаем что ввел юзер" + name_team)
        def name_role = operand.args.get(1)
        //log.error("Получаем что ввел юзер" + name_role)
        def roles = teamService.getTeamRoles()
        //log.error("Получаем список ролей" + roles)
        //Collection<TeamRole> name_roles = teamService.getTeamRoles().get()
        for (String role in roles){
            if (name_role != role){
                messages.addErrorMessage ("Role not found")
            }
        }

        //MessageSet messages = new MessageSetImpl()
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

        //List<QueryLiteral> out = []




    }
}


