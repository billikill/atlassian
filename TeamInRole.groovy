package com.onresolve.jira.groovy.jql
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.util.MessageSetImpl
import com.atlassian.jira.JiraDataType
import com.atlassian.jira.JiraDataTypes
import com.atlassian.jira.jql.operand.QueryLiteral
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import com.onresolve.jira.groovy.jql.AbstractScriptedJqlFunction
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.tempoplugin.core.datetime.range.LocalDateRange
import com.tempoplugin.platform.api.user.TempoUser
import com.tempoplugin.team.api.Team
import com.tempoplugin.team.api.TeamManager
import com.tempoplugin.team.api.TeamService
import com.tempoplugin.team.api.member.TeamMember
import com.tempoplugin.team.api.member.TeamMembership
import org.apache.log4j.Logger
import org.apache.log4j.Level



@WithPlugin ("is.origo.jira.tempo-plugin")
class TeamInRole extends AbstractScriptedJqlFunction implements JqlFunction {
    TeamManager teamManager  = ScriptRunnerImpl.getPluginComponent(TeamManager)
    TeamService teamService  = ScriptRunnerImpl.getPluginComponent(TeamService)
    com.tempoplugin.timetracking.team.TeamService teamService1 = ScriptRunnerImpl.getPluginComponent(com.tempoplugin.timetracking.team.TeamService

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
        for (role in roles){
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

        def log = Logger.getLogger("com.tempoplugin.getValues.CreateJQL")
        log.setLevel(Level.DEBUG)

        def team = teamManager.getTeamByName(operand.args.get(0))       // получили название команды, которую ввел юзер
        def team_id =  team.getId()                                     // получаем id команды
        log.debug ("ID команды котоую ввел юзер:" + team_id)
        def name_role = operand.args.get(1)                             // получили название роли, которую ввел юзер
        log.debug ("Название роли которую ввел юзер:" + name_role)
        def roles = teamService.getTeamRoles().get()                          // получили список всех ролей
        log.debug("Получам список всех ролей какие есть в команде:" + roles.collect{it.getName()}.toString())
        def role_id
        for (role in roles) {
            if (name_role == role.getName()) {
                log.debug ("Сравниваем роль что ввел юзер и какая есть в массиве:" + role.getName())
                role_id = role.getId()
                break
            }
        }

        def w = teamService1.getTeamMembers(team)
        log.debug ("Получаем ID роли которую ввел юзер:" + role_id)
        def date = LocalDateRange.oneDay(new org.joda.time.DateTime())
        log.debug("Получаем дату:" + date)
        def mem = teamService.getTeamMembersByRole(date, role_id).get()
        List<QueryLiteral> out = []
        for (i in w){
            def username3 = i.getKey()
            log.debug("Получаем кей юзера в роли 3:" + username3)
            for (ii in mem){
                def username4 = ii.getUserKey()
                log.debug("Получаем кей юзера в роли 3:" + username3)
                if (username3 == username4){
                    out.add(new QueryLiteral(operand, username3 ))
                }
            }
        }
        log.debug("Получаем юзеров в роли:" + mem)
        return out


    }
}


