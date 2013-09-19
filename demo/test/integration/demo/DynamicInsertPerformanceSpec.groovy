package demo

import grails.test.spock.IntegrationSpec
import org.hibernate.Session
import org.springframework.util.StopWatch

class DynamicInsertPerformanceSpec extends IntegrationSpec {

    def sessionFactory

    def "ダイナミックインサートなし"() {
        given:
        def iterations = 10000

        when:
        withStopWatch() {
            iterations.times {
                new FatDomain().save()
                if (it % 50 == 0) {
                    session.flush()
                }
            }
            session.flush()
        }

        then:
        FatDomain.count() == iterations
    }

    def "ダイナミックインサートあり"() {
        given:
        def iterations = 10000

        when:
        session.clear()
        withStopWatch("ダイナミックインサートあり") {
            iterations.times {
                new DynamicInsertFatDomain().save()
                if (it % 50 == 0) {
                    session.flush()
                }
            }
            session.flush()
        }

        then:
        DynamicInsertFatDomain.count() == iterations
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
