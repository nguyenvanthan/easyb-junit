package org.easyb.junit4;

import org.easyb.domain.Behavior;
import org.junit.internal.runners.ClassRoadie;
import org.junit.internal.runners.CompositeRunner;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.TestClass;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Using <code>Suite</code> as a runner allows you to manually
 * build a suite containing tests from many classes. It is the JUnit 4 equivalent of the JUnit 3.8.x
 * static {@link junit.framework.Test} <code>suite()</code> method. To use it, annotate a class
 * with <code>@RunWith(Suite.class)</code> and <code>@SuiteClasses(TestClass1.class, ...)</code>.
 * When you run this class, it will run all the tests in all the suite classes.
 */
public class EasybJunit4SuiteRunner extends CompositeRunner {
    /**
     * The <code>SuiteClasses</code> annotation specifies the classes to be run when a class
     * annotated with <code>@RunWith(Suite.class)</code> is run.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SuiteClasses {
        public Class<?>[] value();
    }

    /**
     * Internal use only.
     */
    public EasybJunit4SuiteRunner(Class<?> klass) throws InitializationError {
        this(klass, null);
    }

    // This won't work correctly in the face of concurrency. For that we need to
    // add parameters to getRunner(), which would be much more complicated.
    private static Set<Class<?>> parents = new HashSet<Class<?>>();
    private TestClass fTestClass;

    protected EasybJunit4SuiteRunner(Class<?> klass, List<Behavior> behaviors) throws InitializationError {
        // we need to add parent be
        super(klass.getName());

        addParent(klass);
        for (Behavior each : behaviors) {
            Runner childRunner = Request.aClass(each.getClass()).getRunner();
            if (childRunner != null)
                add(childRunner);
        }
        removeParent(klass);

        fTestClass = new TestClass(klass);

    }

    private Class<?> addParent(Class<?> parent) throws InitializationError {
        if (!parents.add(parent))
            throw new InitializationError(String.format("class '%s' (possibly indirectly) contains itself as a SuiteClass", parent.getName()));
        return parent;
    }

    private void removeParent(Class<?> klass) {
        parents.remove(klass);
    }


    @Override
    public void run(final RunNotifier notifier) {
        new ClassRoadie(notifier, fTestClass, getDescription(), new Runnable() {
            public void run() {
                runChildren(notifier);
            }
        }).runProtected();
    }
}