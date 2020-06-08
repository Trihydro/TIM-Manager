package com.trihydro.odewrapper.helpers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.trihydro.odewrapper.model.BufferedRequestWrapper;

import org.springframework.stereotype.Component;

@Component
public class BufferedRequestWrapperFactory {
    public BufferedRequestWrapper getBufferedRequestWrapper(HttpServletRequest httpServletRequest) throws IOException {
        return new BufferedRequestWrapper(httpServletRequest);
    }
}