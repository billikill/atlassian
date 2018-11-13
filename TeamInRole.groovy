package com.onresolve.jira.groovy.jql
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.datetime.LocalDate
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
import com.onresolve.jira.groovy.jql.AbstractScriptedJqlFunction
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.tempoplugin.core.datetime.api.TempoDate
import com.tempoplugin.core.datetime.api.TempoDateTime
import com.tempoplugin.core.datetime.range.LocalDateRange
import com.tempoplugin.platform.api.user.TempoUser
import com.tempoplugin.team.api.TeamManager
import com.tempoplugin.team.api.TeamService
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

        def log = Logger.getLogger("com.tempoplugin.CreateJQL")
        log.setLevel(Level.DEBUG)

        def team = teamManager.getTeamByName(operand.args.get(0))       // получили название команды, которую ввел юзер
        def team_id =  team.getId()                                     // получаем id команды
        log.debug ("ID команды котоую ввел юзер:" + team_id)
        def name_role = operand.args.get(1)                             // получили название роли, которую ввел юзер
        log.debug ("Название роли которую ввел юзер:" + name_role)

        def roles = teamService.getTeamRoles().get()                          // получили список всех ролей
        log.debug("Получам список всех ролей:" + roles.collect{it.getName()}.toString())
        def role_id
        for (role in roles) {
            //log.debug("Название роли которую ввел юзер:" + roles.toString())
            if (name_role == role.getName()) {
                log.debug ("Сравниваем роль что ввел юзер и какая есть в массиве:" + role.getName())
                role_id = role.getId()
                break
            }
        }
        log.debug ("Получаем ID роли которую ввел юзер:" + role_id)
        def members = teamService.getTeamMember(2) // получаем имя пользователя по его id
        def member = members.get().getMemberName()
        log.debug ("Получаем имя пользователя:" + member )

        def memberteam = teamManager.getTempoUserByMemberId(2)  // получаем темпо юзера по его id
        def mt = memberteam.getUsername()
        log.debug("Получаем темпо пользователей:" + mt )
        /*
        List<TempoUser> membrs = teamService2.getTeamMembers(team)
        log.debug("Получаем список темпо пользователей в команде:" + membrs)
        def mem_id
        for (member in membrs){
            mem_id = member.getUsername()
            if (role_id == member.get) {
                log.debug("Получаем список пользователей в команде:" + mem_id)
            }
        }
        def qq = teamService.getGroups(team_id)
        log.debug("Получаем какие то группы:" + qq)
        //def ww = teamService.getTeamMember()
        //log.debug("Получаем группы в какие входит юзер:" + ww)
        */
        def date = LocalDateRange.unbounded()
        def locaDate = TempoDate.now()
        log.debug("Получаем дату:" + date)
        log.debug("Получаем дату 2:" + locaDate)
        log.debug("Получаем teamService:" + teamService)
        //List<TempoUser> mem = teamService.getTeamMembersByRole(date, role_id)
        def mem = teamService.getTeamMembersByRole(date, role_id)
        log.debug("Получаем юзеров в роли:" + mem)


    }
}


