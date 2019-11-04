package io.csra.wily.components.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * An out-of-the-box CORS Filter that takes care of Cross-Site Scripting concerns. You can control the filter behavior
 * using the access.control.* properties on the classpath (methods, maxage, allowheaders, exposeheaders).
 *
 * @author ndimola
 */
public class SimpleCORSFilter extends AbstractFilter {

	@Autowired
	private Environment environment;

	private Map<String, String> origins;

	private boolean hasWildcardOrigin;

	protected void doFilterImpl(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;

		response.setHeader("Access-Control-Allow-Origin", getOrigin(req.getRemoteHost()));
		response.setHeader("Access-Control-Allow-Methods", environment.getRequiredProperty("access.control.methods"));
		response.setHeader("Access-Control-Max-Age", environment.getRequiredProperty("access.control.maxage"));
		response.setHeader("Access-Control-Allow-Headers", environment.getRequiredProperty("access.control.allowheaders"));
		response.setHeader("Access-Control-Expose-Headers", environment.getRequiredProperty("access.control.exposeheaders"));
	}

	private String getOrigin(String host) {
		if (origins == null) {
			buildOriginList();
		}

		if (hasWildcardOrigin) {
			return "*";
		}

		for (String origin : origins.keySet()) {
			if (host.equals(origin)) {
				return origins.get(origin);
			}
		}

		return "";
	}

	private void buildOriginList() {
		origins = new HashMap<String, String>();
		String[] environmentOrigins = environment.getRequiredProperty("access.control.origin").split(",");

		for (String origin : environmentOrigins) {
			String originHost = origin.trim();
			originHost = originHost.replaceAll("http://", "");

			int colonIndex = originHost.indexOf(":");
			if (colonIndex > -1) {
				originHost = originHost.substring(0, colonIndex);
			}

			origins.put(originHost, origin);

			if ("*".equals(originHost)) {
				hasWildcardOrigin = true;
			}
		}
	}

}
