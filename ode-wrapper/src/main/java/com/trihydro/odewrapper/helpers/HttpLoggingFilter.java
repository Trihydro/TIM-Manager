package com.trihydro.odewrapper.helpers;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.HttpLoggingModel;
import com.trihydro.library.service.LoggingService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.model.BufferedRequestWrapper;
import com.trihydro.odewrapper.model.BufferedResponseWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Adapted from https://stackoverflow.com/a/39137815
@Component
public class HttpLoggingFilter implements Filter {

    private LoggingService loggingService;
    private BasicConfiguration basicConfiguration;
    private Utility utility;
    private BufferedResponseWrapperFactory buffRespWrapFac;
    private BufferedRequestWrapperFactory buffReqWrapFac;

    @Autowired
    public void InjectDependencies(LoggingService _loggingService, BasicConfiguration _basicConfiguration,
            Utility _utility, BufferedResponseWrapperFactory _buffRespWrapFac,
            BufferedRequestWrapperFactory _buffReqWrapFac) {
        loggingService = _loggingService;
        basicConfiguration = _basicConfiguration;
        utility = _utility;
        buffRespWrapFac = _buffRespWrapFac;
        buffReqWrapFac = _buffReqWrapFac;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Timestamp requestTime = new Timestamp(System.currentTimeMillis());
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;

            Map<String, String> requestMap = this.getTypesafeRequestMap(httpServletRequest);
            BufferedRequestWrapper bufferedRequest = buffReqWrapFac.getBufferedRequestWrapper(httpServletRequest);
            BufferedResponseWrapper bufferedResponse = buffRespWrapFac.getBufferedResponseWrapper(httpServletResponse);

            String servletPath = httpServletRequest.getServletPath();
            chain.doFilter(bufferedRequest, bufferedResponse);
            if (servletPath.contains("swagger") || servletPath.contains("api-docs")) {
                return;
            }

            // request portion
            final StringBuilder logMessage = new StringBuilder("REST Request - ").append("[HTTP METHOD:")
                    .append(httpServletRequest.getMethod()).append("] [PATH INFO:").append(servletPath)
                    .append("] [REQUEST PARAMETERS:").append(requestMap).append("] [REQUEST BODY:");

            // adjust for max length
            var threeQuarterSize = (int) (0.75 * basicConfiguration.getHttpLoggingMaxSize())
                    - (logMessage.length() + 1);
            var reqBody = bufferedRequest.getRequestBody();
            if (reqBody != null && reqBody.length() >= threeQuarterSize) {
                reqBody = reqBody.substring(0, threeQuarterSize - 3);
                reqBody += "...";
            }
            logMessage.append(reqBody).append("]");

            // response portion
            logMessage.append(" [RESPONSE CODE:").append(bufferedResponse.getStatus()).append("]");
            // before adding the response, check that it wont go beyond our max size
            String respContent = bufferedResponse.getContent();
            if (logMessage.length() + respContent.length() < (basicConfiguration.getHttpLoggingMaxSize() - 12)) {
                logMessage.append(" [RESPONSE:").append(respContent).append("]");
            } else {
                // truncate the response...
                int maxResponseSize = basicConfiguration.getHttpLoggingMaxSize() - 12 - logMessage.length();
                if (maxResponseSize > 3) {//we add in '...'
                    String serverResponse = respContent.substring(0, maxResponseSize - 3);
                    serverResponse += "...";
                    logMessage.append(" [RESPONSE:").append(serverResponse).append("]");
                }
            }
            utility.logWithDate(logMessage.toString());
            HttpLoggingModel httpLoggingModel = new HttpLoggingModel();
            httpLoggingModel.setRequest(logMessage.toString());
            httpLoggingModel.setRequestTime(requestTime);
            httpLoggingModel.setResponseTime(new Timestamp(System.currentTimeMillis()));
            loggingService.LogHttpRequest(httpLoggingModel);
        } catch (Throwable a) {
            utility.logWithDate(a.getMessage());
        }
    }

    private Map<String, String> getTypesafeRequestMap(HttpServletRequest request) {
        Map<String, String> typesafeRequestMap = new HashMap<String, String>();
        Enumeration<?> requestParamNames = request.getParameterNames();
        while (requestParamNames.hasMoreElements()) {
            String requestParamName = (String) requestParamNames.nextElement();
            String requestParamValue;
            if (requestParamName.equalsIgnoreCase("password")) {
                requestParamValue = "********";
            } else {
                requestParamValue = request.getParameter(requestParamName);
            }
            typesafeRequestMap.put(requestParamName, requestParamValue);
        }
        return typesafeRequestMap;
    }

    @Override
    public void destroy() {
    }
}