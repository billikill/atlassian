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
import java.util.logging.Logger



@WithPlugin ("com.tempoplugin.tempo-teams")
class TeamInRole extends AbstractScriptedJqlFunction implements JqlFunction {
    TeamManager teamManager  = ScriptRunnerImpl.getPluginComponent(TeamManager)
    TeamService teamService  = ScriptRunnerImpl.getPluginComponent(TeamService)
    Logger log = Logger.getLogger("com.test.InlineScript")

    @Override
    String getDescription() {
        "Get members in role in team. Example: assignee in teamInRole(\"Name Team\", \"Name Role\")"
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
        def team = teamManager.getTeamByName(operand.args.get(0))
        def name_role = operand.args.get(1)
        def roles = teamService.getTeamRoles().get()
        boolean roleExists = false
        for (TeamRole role in roles){
            if (name_role == role.getName()){
                roleExists = true
                break
            }
        }
        if(!roleExists) {messages.addErrorMessage ("Role not found")}

        if (team == null) {messages.addErrorMessage ("Team not found")}

        return messages
    }


    @Override
    List<QueryLiteral> getValues( QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {

        def team = teamManager.getTeamByName(operand.args.get(0))       // получили название команды
        def name_role = operand.args.get(1)                             // получили какую рольввел юзер
        def roles = teamService.getTeamRoles().get()                    // получили все роил какие есть в команде
        def members= teamManager.getActiveTempoMembers(team)            // получили учатников команды
        def members_role = teamService.getTeamMembersByRole(name_role)  // получили участников команды в роли, которую ввел юзер
        def member_name =  members                                      // получаем логины участников которые находятся в роли
        //List<QueryLiteral> out = []





    }
}


