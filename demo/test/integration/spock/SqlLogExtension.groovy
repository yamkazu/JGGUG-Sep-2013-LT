package spock

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo

class SqlLogExtension extends AbstractAnnotationDrivenExtension<SqlLog> {

    @Override
    void visitSpecAnnotation(SqlLog sqlLog, SpecInfo spec) {
        spec.features.each { FeatureInfo feature ->
            if (!feature.getFeatureMethod().getReflection().isAnnotationPresent(SqlLog.class)) {
                visitFeatureAnnotation(sqlLog, feature);
            }
        }
        spec.fixtureMethods.each { MethodInfo fixtureMethod ->
            if (!fixtureMethod.getReflection().isAnnotationPresent(SqlLog.class)) {
                visitFixtureAnnotation(sqlLog, fixtureMethod);
            }
        }
    }

    @Override
    void visitFeatureAnnotation(SqlLog sqlLog, FeatureInfo feature) {
        feature.getFeatureMethod().addInterceptor(new SqlLogInterceptor(sqlLog))
    }

    @Override
    void visitFixtureAnnotation(SqlLog sqlLog, MethodInfo fixtureMethod) {
        fixtureMethod.addInterceptor(new SqlLogInterceptor(sqlLog))
    }
}
