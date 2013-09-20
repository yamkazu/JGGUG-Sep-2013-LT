package demo

import grails.test.spock.IntegrationSpec
import groovy.sql.Sql
import org.hibernate.Session
import org.springframework.util.StopWatch

class InsertPerformanceSpec extends IntegrationSpec {

    def dataSource
    def sessionFactory

    def "よくあるドメインの保存"() {
        given:
        def iterations = 10000

        when:
        withStopWatch {
            iterations.times {
                new UniqueDomain(value: "$it").save()
                if (it % 50 == 0) {
                    session.flush()
                }
            }
            session.flush()
        }

        then:
        UniqueDomain.count() == iterations
    }

    def "バリデーション時にデータベースアクセスをしない"() {
        given:
        def iterations = 10000

        when:
        withStopWatch {
            iterations.times {
                new SimpleDomain(value: "$it").save()
                if (it % 50 == 0) {
                    session.flush()
                }
            }
            session.flush()
        }

        then:
        SimpleDomain.count() == iterations
    }

    def "ID自動採番時にデータベースアクセスをしない"() {
        given:
        def iterations = 10000

        when:
        withStopWatch {
            iterations.times {
                new IncrementIdDomain(value: "$it").save()
                if (it % 50 == 0) {
                    session.flush()
                }
            }
            session.flush()
        }

        then:
        IncrementIdDomain.count() == iterations
    }

    def "StatelessSessionを使う"() {
        given:
        def iterations = 10000

        when:
        withStopWatch {
            def session = sessionFactory.openStatelessSession()
            iterations.times {
                session.insert(new IncrementIdDomain(value: "$it"))
            }
            session.managedFlush()
            // see http://koenserneels.blogspot.jp/2013/03/bulk-fetching-with-hibernate.html
            session.close()
        }

        then:
        IncrementIdDomain.count() == iterations
    }

    def "GroovySQLを使う"() {
        given:
        def iterations = 10000

        when:
        withStopWatch {
            Sql sql = new Sql(dataSource)
            sql.withBatch(50) { stat ->
                iterations.times {
                    stat.addBatch("insert into increment_id_domain (id, value, version) values ($it, $it, 0)")
                }
            }
        }

        then:
        IncrementIdDomain.count() == iterations
    }

    def "JDBCを直接使う"() {
        given:
        def iterations = 10000

        when:
        withStopWatch {
            def con = dataSource.connection
            def stat = con.prepareStatement("insert into increment_id_domain (id, value, version) values (?, ?, 0)")
            iterations.times {
                stat.setInt(1, it)
                stat.setString(2, "$it")
                stat.addBatch()
                if (it % 50 == 0) {
                    stat.executeBatch()
                }
            }
            stat.executeBatch()
        }

        then:
        IncrementIdDomain.count() == iterations
    }

    private void withStopWatch(String name, Closure closure) {
        StopWatch stopWatch = new StopWatch(name)
        println "${'>' * 5} start: '${name}'"
        stopWatch.start()
        closure.call()
        stopWatch.stop()
        println "${'<' * 5} finish: '${name}' time = ${stopWatch.lastTaskTimeMillis} (millis)"
    }

    private void withStopWatch(Closure closure) {
        withStopWatch(specificationContext.iterationInfo.name, closure)
    }

    private Session getSession() {
        sessionFactory.currentSession
    }

}
