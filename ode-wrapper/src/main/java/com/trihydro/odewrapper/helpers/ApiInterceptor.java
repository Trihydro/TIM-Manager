package com.trihydro.odewrapper.helpers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.trihydro.library.service.*;

@Component
public class ApiInterceptor extends HandlerInterceptorAdapter {
   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      StringBuilder logMessage = new StringBuilder("REST Request - ").append("[HTTP METHOD:")
            .append(request.getMethod()).append("] [PATH INFO:").append(request.getServletPath())
            .append("] [REQUEST PARAMETERS:").append(request.getParameterMap())
            // .append("] [REQUEST BODY:")
            // .append(bufferedRequest.getRequestBody())
            .append("] [REMOTE ADDRESS:").append(request.getRemoteAddr()).append("]");
      System.out.println(logMessage);
      LoggingService.LogHttpRequest(logMessage.toString());
      return true;
   }

   @Override
   public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
         Exception exception) throws Exception {
   }
}