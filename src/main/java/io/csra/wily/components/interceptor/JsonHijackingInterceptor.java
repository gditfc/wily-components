package io.csra.wily.components.interceptor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;

/**
 * The Class JsonHijackingInterceptor.
 * <p>
 * Extends HandlerInterceptorAdapter to prefix the response with an Angular prefix to mitigate the JSON hijacking vulnerability.
 * Handling controller responses for List, Set, and Arrays.
 *
 * @author Mike Ringrose
 * @author Nick DiMola
 * <p>
 * <a href="https://www.owasp.org/index.php/AJAX_Security_Cheat_Sheet#Always_return_JSON_with_an_Object_on_the_outside">...</a>
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#mvc-ann-controller-advice">...</a>
 * <a href="http://blog.codeleak.pl/2013/11/controlleradvice-improvements-in-spring.html">...</a>
 * <a href="http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/ResponseBodyAdvice.html">...</a>
 */
@Component
public class JsonHijackingInterceptor implements HandlerInterceptor {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonHijackingInterceptor.class);

    /**
     * The Angular JSON prefix to apply to the response.
     */
    private static final String ANGULAR_JSON_PREFIX = ")]}',\n";
    private static final String OWASP_PREFIX = "{\"data\":";
    private static final String OWASP_POSTFIX = "}";

    private static final String JSON_CONFIG_PROPERTY = "${json.hijacking.config}";

    @Value(JSON_CONFIG_PROPERTY)
    private String jsonHijackingConfig;

    /**
     * @param request  Request
     * @param response Response
     * @param handler  Handler
     * @return true always
     * @throws Exception exception
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {
        if (requiresJsonFix(handler)) {
            LOGGER.debug("Is list class.  Adding configured prefix.");

            response.getOutputStream().write(getPrefix().getBytes());
        }

        // Return true - the execution chain should proceed with the next interceptor.
        return true;
    }

    /**
     * @param request      Request
     * @param response     Response
     * @param handler      Handler
     * @param modelAndView ModelAndView
     * @throws Exception exception
     */
    @Override
    public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                           @NotNull Object handler, ModelAndView modelAndView) throws Exception {
        if (requiresJsonFix(handler)) {
            LOGGER.debug("Is list class.  Adding configured postfix.");

            response.getOutputStream().write(getPostfix().getBytes());
        }
    }

    /**
     * @return Prefix
     */
    private String getPrefix() {
        if (jsonHijackingConfig != null && jsonHijackingConfig.equalsIgnoreCase("angular")) {
            return ANGULAR_JSON_PREFIX;
        } else {
            return OWASP_PREFIX;
        }
    }

    /**
     * @return Postfix
     */
    private String getPostfix() {
        if (jsonHijackingConfig != null && (jsonHijackingConfig.equalsIgnoreCase("owasp") || JSON_CONFIG_PROPERTY.equalsIgnoreCase(jsonHijackingConfig))) {
            return OWASP_POSTFIX;
        }

        return "";
    }

    /**
     * Requires JSON fix.
     *
     * @param handler the handler method
     * @return true if the return type is a List, Set, or Array.
     */
    private boolean requiresJsonFix(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            LOGGER.debug("JsonHijackingInterceptor.preHandle handlerMethod: {}", handlerMethod.getMethod());

            Class<?> returnTypeClass = handlerMethod.getMethod().getReturnType();
            LOGGER.debug("returnTypeClass: {}", returnTypeClass);

            return returnTypeClass.equals(List.class) || returnTypeClass.equals(Set.class) || returnTypeClass.equals(Array.class) || returnTypeClass.isArray();
        }

        return false;
    }
}
