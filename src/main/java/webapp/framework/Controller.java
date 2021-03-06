/*  "java-webapp-foundation"(https://github.com/riversun/java-webapp-foundation)
 * 
 *  Easy to develop a micro Web Apps and Web APIs with JSP/Servlet
 * 
 *  Copyright (c) 2006-2017 Tom Misawa, riversun.org@gmail.com
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *  DEALINGS IN THE SOFTWARE.
 *  
 */
package webapp.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.riversun.string_grabber.StringGrabber;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.xml.XmlMapper;

/**
 * Controller (C of MVC)
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public abstract class Controller {

	// implicit objects[begin]============
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected HttpSession session;
	// implicit objects[end]============

	private SecureCookieManager mSecureCookieManager = null;

	private final ObjectMapper mJsonMapper = new ObjectMapper();
	private final XmlMapper mXmlMapper = new XmlMapper();

	public enum HttpMethod {
		POST, //
		GET, //
		OPTIONS, //
		OTHER, //
	}

	public boolean prepareDoService() throws ServletException, IOException {
		return true;
	}

	protected abstract void doService() throws ServletException, IOException;

	protected String getRemoteHost() {
		return request.getRemoteHost();
	}

	protected String asString(String parameterName) {
		return request.getParameter(parameterName);
	}

	protected Long asLong(String parameterName) {
		Long retVal = null;

		try {
			retVal = Long.parseLong(asString(parameterName));
		} catch (Exception e) {

		}
		return retVal;

	}

	protected Integer asInteger(String parameterName) {
		Integer retVal = null;

		try {
			retVal = Integer.parseInt(asString(parameterName));
		} catch (Exception e) {
		}
		return retVal;

	}

	/**
	 * Read request content as JSON object
	 * 
	 * @param valueType
	 * @return
	 * @throws Exception
	 */
	public <T> T asJSON(Class<T> valueType) throws Exception
	{

		final String inJson = asString();
		return mJsonMapper.readValue(inJson, valueType);
	}

	/**
	 * Read request content as String
	 * 
	 * @return
	 */
	public String asString() {

		final StringBuffer sb = new StringBuffer();

		String line = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
		}
		return sb.toString();
	}

	protected void requestScope(String name, Object value) {
		request.setAttribute(name, value);
	}

	protected Object requestScope(String name) {
		return request.getAttribute(name);
	}

	protected void sessionScope(String name, Object value) {
		session.setAttribute(name, value);
	}

	protected Object sessionScope(String name) {
		return session.getAttribute(name);
	}

	public void setContentType(String contentType) {
		response.setContentType(contentType);
	}

	public String remoteHost() {
		return request.getRemoteHost();
	}

	public void setContentTypeTo_HTML_UTF8() {
		setContentType("text/html; charset=UTF-8");
	}

	public void setContentTypeTo_JSON_UTF8() {
		setContentType("application/json; charset=UTF-8");
	}

	public void setContentTypeTo_XML_UTF8() {
		setContentType("application/xml; charset=UTF-8");
	}

	protected void dispatch(String fowardToPath) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher(fowardToPath);
		dispatcher.forward(request, response);
	}

	protected void redirect(String redirectToPath) throws ServletException, IOException {
		StringGrabber sg = new StringGrabber(redirectToPath);
		if (redirectToPath.startsWith("/")) {
			sg.removeHead(1);
		}
		response.sendRedirect(sg.toString());
	}

	/**
	 * Returns object as JSON
	 * 
	 * @param modelObj
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void returnAsJSON(Object modelObj) throws ServletException, IOException {
		setContentTypeTo_JSON_UTF8();

		final PrintWriter out = response.getWriter();
		final String json = mJsonMapper.writeValueAsString(modelObj);
		out.println(json);
		out.close();
	}

	/**
	 * Returns object as JSONP
	 * 
	 * @param callback
	 * @param modelObj
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void returnAsJSONP(String callback, Object modelObj) throws ServletException, IOException {
		setContentTypeTo_JSON_UTF8();

		final PrintWriter out = response.getWriter();

		mJsonMapper.setSerializationInclusion(Include.NON_NULL);
		final String jsonp = callback + "(" + mJsonMapper.writeValueAsString(modelObj) + ");";
		out.println(jsonp);
		out.close();
	}

	/**
	 * Returns object as XML
	 * 
	 * @param modelObj
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void returnAsXML(Object modelObj) throws ServletException, IOException {
		setContentTypeTo_XML_UTF8();

		final PrintWriter out = response.getWriter();

		final String xml = mXmlMapper.writeValueAsString(modelObj);
		out.println(xml);
		out.close();
	}

	/**
	 * Returns nothing
	 * 
	 * @throws IOException
	 */
	protected void returnNothing() throws IOException {
		setContentTypeTo_JSON_UTF8();

		final PrintWriter out = response.getWriter();
		out.close();
	}

	/**
	 * Add cookie to request
	 * 
	 * @param cookieKey
	 * @param cookieValue
	 * @param maxAge
	 */
	protected void addCookie(String cookieKey, String cookieValue, int maxAge) {
		if (response != null) {
			final Cookie cookie = new Cookie(cookieKey, cookieValue);
			cookie.setMaxAge(maxAge);
			response.addCookie(cookie);
		}
	}

	/**
	 * Returns cookie value stored in the browser
	 * 
	 * @param cookieKey
	 * @return
	 */
	protected String getCookieValue(String cookieKey) {
		return getCookieValue(cookieKey, null);

	}

	protected String getCookieValue(String cookieKey, String defaultValue) {
		if (request != null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					if (cookieKey.equals(cookies[i].getName())) {
						return cookies[i].getValue();
					}
				}
			}
			return defaultValue;
		} else {
			return defaultValue;
		}

	}

	/**
	 * Add cookie encrypted into the browser
	 * 
	 * @param cookieKey
	 * @param cookieValue
	 * @param maxAge
	 */
	protected synchronized void addSecureCookie(String cookieKey, String cookieValue, int maxAge) {
		if (mSecureCookieManager == null) {
			mSecureCookieManager = new SecureCookieManager(this);
		}
		mSecureCookieManager.addSecureCookie(cookieKey, cookieValue, maxAge);
	}

	/**
	 * Get encrypted cookie value store in the browser
	 * 
	 * @param cookieKey
	 * @param cookieValue
	 * @param maxAge
	 */
	protected synchronized String getSecureCookie(String cookieKey) {
		return getSecureCookieValue(cookieKey, null);

	}

	/**
	 * Get encrypted cookie value store in the browser
	 * 
	 * @param cookieKey
	 * @param defaultValue
	 * @return
	 */
	protected synchronized String getSecureCookieValue(String cookieKey, String defaultValue) {
		if (mSecureCookieManager == null) {
			mSecureCookieManager = new SecureCookieManager(this);
		}

		final String value = mSecureCookieManager.getSecureCookieValue(cookieKey);

		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * Do execute forward dispatch
	 * 
	 * @param request
	 * @param response
	 * @param fowardToPath
	 * @throws ServletException
	 * @throws IOException
	 */
	private void dispatch(HttpServletRequest request, HttpServletResponse response, String fowardToPath) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher(fowardToPath);
		dispatcher.forward(request, response);
	}

	/**
	 * Send redirect
	 * 
	 * @param request
	 * @param response
	 * @param redirectToPath
	 * @throws ServletException
	 * @throws IOException
	 */
	private void redirect(HttpServletRequest request, HttpServletResponse response, String redirectToPath) throws ServletException, IOException {
		response.sendRedirect(redirectToPath);
	}

	public HttpMethod getHttpMethod() {

		if (request.getMethod().equalsIgnoreCase("GET")) {

			return HttpMethod.GET;

		} else if (request.getMethod().equalsIgnoreCase("POST")) {

			return HttpMethod.POST;
		} else if (request.getMethod().equalsIgnoreCase("OPTIONS")) {

			return HttpMethod.OPTIONS;
		} else {

			return HttpMethod.OTHER;

		}
	}

	/**
	 * Set CORS policy
	 * 
	 * @param value
	 *            specify like "*","https://xxxxxx.com"
	 */
	protected void setAccessControlAllowOrigin(String value) {
		response.addHeader("Access-Control-Allow-Origin", value);
		response.addHeader("Access-Control-Allow-Headers", "Content-Type");
	}

	/**
	 * Enable Cookie while CORS connection
	 * 
	 * @param enabled
	 */
	protected void setAccessControlAllowCredentials(boolean enabled) {
		if (enabled) {
			response.addHeader("Access-Control-Allow-Credentials", String.valueOf(enabled));
		}
	}

}
