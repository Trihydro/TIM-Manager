package com.trihydro.odewrapper.helpers;

import javax.servlet.http.HttpServletResponse;

import com.trihydro.odewrapper.model.BufferedResponseWrapper;

import org.springframework.stereotype.Component;

@Component
public class BufferedResponseWrapperFactory {
    public BufferedResponseWrapper getBufferedResponseWrapper(HttpServletResponse httpServletResponse){
        return new BufferedResponseWrapper(httpServletResponse);
    }
}