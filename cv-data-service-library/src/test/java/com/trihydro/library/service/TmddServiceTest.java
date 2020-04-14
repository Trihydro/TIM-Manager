package com.trihydro.library.service;

import com.trihydro.library.model.TmddProps;

import org.junit.Test;

public class TmddServiceTest {
    @Test
    public void tmdd_fetch_success() {
        // Arrange
        TmddService uut = new TmddService();
        uut.InjectDependencies(new TmddConfig());

        // Act
        uut.getTmddEvents();
    }

    private class TmddConfig implements TmddProps {

        public String getTmddUrl() {
            return "https://resdf.wyoroad.info";
        }

        public String getTmddUser() {
            return "cvsupport@trihydro.com";
        }

        public String getTmddPassword() {
            return "zMM+fkDt1kyZ8J0QMRoO5Q==";
        }

    }
}