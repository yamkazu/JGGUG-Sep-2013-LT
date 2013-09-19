package spock

import grails.util.Holders
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.hibernate.cfg.Settings
import org.hibernate.jdbc.util.SQLStatementLogger
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation

class SqlLogInterceptor implements IMethodInterceptor {

    static String SQL_LOG_PACKAGE = "org.hibernate.SQL"
    static String SQL_PARAMETER_LOG_PACKAGE = "org.hibernate.type.descriptor.sql.BasicBinder"

    SqlLog sqlLog

    SqlLogInterceptor(SqlLog sqlLog) {
        this.sqlLog = sqlLog
    }

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
        // save
        Logger sqlLogger = Logger.getLogger(SQL_LOG_PACKAGE)
        def savedSqlLogLebel = sqlLogger.level
        Logger parameterLogger = Logger.getLogger(SQL_PARAMETER_LOG_PACKAGE)
        def savedSqlParameterLogLebel = parameterLogger.level

        Settings settings = Holders.applicationContext.sessionFactory.settings
        def savedCommentsEnabled = settings.commentsEnabled

        SQLStatementLogger sqlStatementLogger = settings.sqlStatementLogger
        def savedSqlStatementLogger = sqlStatementLogger.formatSql

        // change
        sqlLogger.level = Level.DEBUG

        if (sqlLog.parameter()) {
            parameterLogger.level = Level.TRACE
        }
        if (sqlLog.comment()) {
            settings.commentsEnabled = true
        }
        if (sqlLog.format()) {
            sqlStatementLogger.formatSql = true
        }

        try {
            invocation.proceed()
        } finally {
            // rollback
            sqlLogger.level = savedSqlLogLebel
            parameterLogger.level = savedSqlParameterLogLebel
            settings.commentsEnabled = savedCommentsEnabled
            sqlStatementLogger.formatSql = savedSqlStatementLogger
        }

    }

}
