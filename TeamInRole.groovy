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
        def team_id =  team.getId()
        def name_role = operand.args.get(1)
        def roles = teamService.getTeamRoles().get()
        boolean roleExists = false
        def role_id
        for (role in roles){
            if (name_role == role.getName()){
                roleExists = true
                role_id = role.getId()
                break
            }
        }
        def a = teamService.getTeamMember(team_id).get()
        log.debug("Получаем team member :" + a)
        def s  = a.getTeamId(team_id)
        log.debug("Получаем ID юзеров :" + s)
        boolean userTeam = false
        for (){
            if(s == role_id) {
                userTeam = true
                //break
            }
        }

        if(!userTeam) {messages.addErrorMessage ("User in Team not found")}

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
        log.debug ("Получаем ID роли которую ввел юзер:" + role_id)
        def date = LocalDateRange.oneDay(new org.joda.time.DateTime())
        log.debug("Получаем дату:" + date)
        def mem = teamService.getTeamMembersByRole(date, role_id).get()
        def userid
        List<QueryLiteral> out = []
        for (i in mem){
            out.add(new QueryLiteral(operand, i.userKey))
            userid = i.getId()
            log.debug("Получаем id юзера который в роли:" + userid)
            def username = i.getUserKey()
            log.debug("Получаем кей юзера в роли:" + username)
            out.add(new QueryLiteral(operand, username))
        }
        log.debug("Получаем юзеров в роли:" + mem)

        //def a = teamService.getTeamMember(userid).get()
        //log.debug("Получаем team member :" + a)
        //def s  = a.getTeamId()
        //log.debug("Получаем ID юзеров :" + s)
        return out


    }
}


