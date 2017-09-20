package com.youmu.maven.springframework.cache.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.youmu.maven.springframework.cache.exception.VariableNotAvailableException;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.Cache;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.util.Assert;

/**
 * @from spring-context
 * @since 3.1
 */
public class CacheOperationExpressionEvaluator extends CachedExpressionEvaluator {

    /**
     * Indicate that there is no result variable.
     */
    public static final Object NO_RESULT = new Object();

    /**
     * Indicate that the result variable cannot be used at all.
     */
    public static final Object RESULT_UNAVAILABLE = new Object();

    /**
     * The name of the variable holding the result object.
     */
    public static final String RESULT_VARIABLE = "result";

    private final Map<ExpressionKey, Expression> keyCache = new ConcurrentHashMap<ExpressionKey, Expression>(
            64);

    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<ExpressionKey, Expression>(
            64);

    private final Map<ExpressionKey, Expression> unlessCache = new ConcurrentHashMap<ExpressionKey, Expression>(
            64);

    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<AnnotatedElementKey, Method>(
            64);

    /**
     * Create an {@link EvaluationContext} without a return value.
     * @see #createEvaluationContext(Collection, Method, Object[], Object,
     *      Class, Object, BeanFactory)
     */
    public EvaluationContext createEvaluationContext(Collection<? extends Cache> caches,
            Method method, Object[] args, Object target, Class<?> targetClass,
            BeanFactory beanFactory) {

        return createEvaluationContext(caches, method, args, target, targetClass, NO_RESULT,
                beanFactory);
    }

    /**
     * Create an {@link EvaluationContext}.
     * @param caches the current caches
     * @param method the method
     * @param args the method arguments
     * @param target the target object
     * @param targetClass the target class
     * @param result the return value (can be {@code null}) or
     *            {@link #NO_RESULT} if there is no return at this time
     * @return the evaluation context
     */
    public EvaluationContext createEvaluationContext(Collection<? extends Cache> caches,
            Method method, Object[] args, Object target, Class<?> targetClass, Object result,
            BeanFactory beanFactory) {

        CacheExpressionRootObject rootObject = new CacheExpressionRootObject(caches, method, args,
                target, targetClass);
        Method targetMethod = getTargetMethod(targetClass, method);
        CacheEvaluationContext evaluationContext = new CacheEvaluationContext(rootObject,
                targetMethod, args, getParameterNameDiscoverer());
        if (result == RESULT_UNAVAILABLE) {
            evaluationContext.addUnavailableVariable(RESULT_VARIABLE);
        } else if (result != NO_RESULT) {
            evaluationContext.setVariable(RESULT_VARIABLE, result);
        }
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    public Object key(String keyExpression, AnnotatedElementKey methodKey,
            EvaluationContext evalContext) {
        return getExpression(this.keyCache, methodKey, keyExpression).getValue(evalContext);
    }

    public boolean condition(String conditionExpression, AnnotatedElementKey methodKey,
            EvaluationContext evalContext) {
        return getExpression(this.conditionCache, methodKey, conditionExpression)
                .getValue(evalContext, boolean.class);
    }

    public boolean unless(String unlessExpression, AnnotatedElementKey methodKey,
            EvaluationContext evalContext) {
        return getExpression(this.unlessCache, methodKey, unlessExpression).getValue(evalContext,
                boolean.class);
    }

    /**
     * Clear all caches.
     */
    void clear() {
        this.keyCache.clear();
        this.conditionCache.clear();
        this.unlessCache.clear();
        this.targetMethodCache.clear();
    }

    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        Method targetMethod = this.targetMethodCache.get(methodKey);
        if (targetMethod == null) {
            targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            if (targetMethod == null) {
                targetMethod = method;
            }
            this.targetMethodCache.put(methodKey, targetMethod);
        }
        return targetMethod;
    }

    public static class CacheExpressionRootObject {

        private final Collection<? extends Cache> caches;

        private final Method method;

        private final Object[] args;

        private final Object target;

        private final Class<?> targetClass;

        public CacheExpressionRootObject(Collection<? extends Cache> caches, Method method,
                Object[] args, Object target, Class<?> targetClass) {

            Assert.notNull(method, "Method is required");
            Assert.notNull(targetClass, "targetClass is required");
            this.method = method;
            this.target = target;
            this.targetClass = targetClass;
            this.args = args;
            this.caches = caches;
        }

        public Collection<? extends Cache> getCaches() {
            return this.caches;
        }

        public Method getMethod() {
            return this.method;
        }

        public String getMethodName() {
            return this.method.getName();
        }

        public Object[] getArgs() {
            return this.args;
        }

        public Object getTarget() {
            return this.target;
        }

        public Class<?> getTargetClass() {
            return this.targetClass;
        }

    }

    class CacheEvaluationContext extends MethodBasedEvaluationContext {

        private final Set<String> unavailableVariables = new HashSet<String>(1);

        CacheEvaluationContext(Object rootObject, Method method, Object[] arguments,
                ParameterNameDiscoverer parameterNameDiscoverer) {

            super(rootObject, method, arguments, parameterNameDiscoverer);
        }

        /**
         * Add the specified variable name as unavailable for that context. Any
         * expression trying to access this variable should lead to an
         * exception.
         * <p>
         * This permits the validation of expressions that could potentially a
         * variable even when such variable isn't available yet. Any expression
         * trying to use that variable should therefore fail to evaluate.
         */
        public void addUnavailableVariable(String name) {
            this.unavailableVariables.add(name);
        }

        /**
         * Load the param information only when needed.
         */
        @Override
        public Object lookupVariable(String name) {
            if (this.unavailableVariables.contains(name)) {
                throw new VariableNotAvailableException(name);
            }
            return super.lookupVariable(name);
        }

    }
}
