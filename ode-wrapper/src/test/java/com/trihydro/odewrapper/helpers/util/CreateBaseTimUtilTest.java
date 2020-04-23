package com.trihydro.odewrapper.helpers.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.service.MilepostService;
import com.trihydro.odewrapper.config.BasicConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

@RunWith(StrictStubs.class)
public class CreateBaseTimUtilTest {
    @Mock
    BasicConfiguration mockBasicConfiguration;
    @Mock
    Utility mockUtility;
    @Mock
    MilepostService mockMilepostService;

    @InjectMocks
    CreateBaseTimUtil uut;

    private List<Milepost> getMileposts() {
        List<Milepost> mps = new ArrayList<>();
        Milepost mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35108594);
        mp.setLongitude(-105.71982297);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35080826);
        mp.setLongitude(-105.71791726);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.3505391);
        mp.setLongitude(-105.71600946);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35026563);
        mp.setLongitude(-105.71410271);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34998857);
        mp.setLongitude(-105.71219687);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34971931);
        mp.setLongitude(-105.71028907);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34944341);
        mp.setLongitude(-105.70838296);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34917608);
        mp.setLongitude(-105.70647477);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34900264);
        mp.setLongitude(-105.70454866);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34909178);
        mp.setLongitude(-105.70261381);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34942922);
        mp.setLongitude(-105.70074013);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.3498824);
        mp.setLongitude(-105.69890686);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35028683);
        mp.setLongitude(-105.69705483);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35045968);
        mp.setLongitude(-105.69514162);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35046923);
        mp.setLongitude(-105.69321245);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35047214);
        mp.setLongitude(-105.69128312);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35047525);
        mp.setLongitude(-105.6893538);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35048283);
        mp.setLongitude(-105.6874245);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35048667);
        mp.setLongitude(-105.68549518);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.3504909);
        mp.setLongitude(-105.68356586);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35049338);
        mp.setLongitude(-105.68162168);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35050122);
        mp.setLongitude(-105.67967751);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35050295);
        mp.setLongitude(-105.67773333);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35050541);
        mp.setLongitude(-105.67578914);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35050841);
        mp.setLongitude(-105.67384496);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35051591);
        mp.setLongitude(-105.6719008);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35051979);
        mp.setLongitude(-105.66995663);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35052528);
        mp.setLongitude(-105.66801246);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35052552);
        mp.setLongitude(-105.66606832);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35053116);
        mp.setLongitude(-105.66412415);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35053552);
        mp.setLongitude(-105.66221394);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35053841);
        mp.setLongitude(-105.66030372);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35053857);
        mp.setLongitude(-105.65839351);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35054359);
        mp.setLongitude(-105.65648329);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35055043);
        mp.setLongitude(-105.65457309);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35055435);
        mp.setLongitude(-105.65266287);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35055749);
        mp.setLongitude(-105.65075265);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35056059);
        mp.setLongitude(-105.64884242);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35056299);
        mp.setLongitude(-105.6469322);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35055867);
        mp.setLongitude(-105.64502208);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35043367);
        mp.setLongitude(-105.64311038);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.35010638);
        mp.setLongitude(-105.64124167);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34959611);
        mp.setLongitude(-105.63944697);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34890277);
        mp.setLongitude(-105.63776277);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34815556);
        mp.setLongitude(-105.6361185);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34740819);
        mp.setLongitude(-105.63447439);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34666284);
        mp.setLongitude(-105.63282867);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34591768);
        mp.setLongitude(-105.63118282);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34517272);
        mp.setLongitude(-105.62953684);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34442506);
        mp.setLongitude(-105.62789302);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34367193);
        mp.setLongitude(-105.62622947);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34291748);
        mp.setLongitude(-105.62456699);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34215755);
        mp.setLongitude(-105.62290894);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.34140558);
        mp.setLongitude(-105.62124455);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.3405704);
        mp.setLongitude(-105.61965357);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.33950715);
        mp.setLongitude(-105.61832822);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.33823816);
        mp.setLongitude(-105.61737323);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.33683509);
        mp.setLongitude(-105.61683909);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.33537947);
        mp.setLongitude(-105.61665241);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.3339197);
        mp.setLongitude(-105.6165228);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.33253561);
        mp.setLongitude(-105.61640304);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.33115154);
        mp.setLongitude(-105.61628292);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.32976762);
        mp.setLongitude(-105.61615966);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.32838367);
        mp.setLongitude(-105.61603674);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.32699958);
        mp.setLongitude(-105.61591691);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.3256154);
        mp.setLongitude(-105.61579875);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.32423148);
        mp.setLongitude(-105.61567537);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.32284734);
        mp.setLongitude(-105.61555622);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.32146327);
        mp.setLongitude(-105.61543583);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.32007908);
        mp.setLongitude(-105.61531771);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.31861461);
        mp.setLongitude(-105.6151909);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.31715018);
        mp.setLongitude(-105.61506327);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.31568573);
        mp.setLongitude(-105.6149361);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.31422132);
        mp.setLongitude(-105.61480798);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.31275679);
        mp.setLongitude(-105.61468218);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.31129252);
        mp.setLongitude(-105.61455132);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30982806);
        mp.setLongitude(-105.61442426);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30836348);
        mp.setLongitude(-105.61429962);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30689907);
        mp.setLongitude(-105.61417133);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30544029);
        mp.setLongitude(-105.61396925);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30413484);
        mp.setLongitude(-105.61339994);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.3029801);
        mp.setLongitude(-105.61241066);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30205978);
        mp.setLongitude(-105.61105758);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.3014131);
        mp.setLongitude(-105.60944887);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30094584);
        mp.setLongitude(-105.60773014);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30048258);
        mp.setLongitude(-105.60600929);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.30001744);
        mp.setLongitude(-105.60428936);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.2995553);
        mp.setLongitude(-105.60256801);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29909199);
        mp.setLongitude(-105.60084723);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29862816);
        mp.setLongitude(-105.59912671);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29812221);
        mp.setLongitude(-105.59724882);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.2976113);
        mp.setLongitude(-105.59535359);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29710355);
        mp.setLongitude(-105.5934569);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29659485);
        mp.setLongitude(-105.59156067);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29608238);
        mp.setLongitude(-105.58966622);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29556906);
        mp.setLongitude(-105.5877722);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29506105);
        mp.setLongitude(-105.58587568);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29455088);
        mp.setLongitude(-105.58398019);
        mps.add(mp);

        mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setLatitude(41.29404404);
        mp.setLongitude(-105.58208315);
        mps.add(mp);

        return mps;
    }

    @Test
    public void applyMilepostReductionAlorithm_SUCCESS() {
        // Arrange
        List<Milepost> mps = getMileposts();

        // Act
        List<Milepost> reduced = uut.applyMilepostReductionAlorithm(mps, Double.valueOf(327 / 2));

        // Assert
        assertTrue(mps.size() > reduced.size());
    }

    @Test
    public void applyMilepostReductionAlorithm_NULL() {
        // Arrange

        // Act
        List<Milepost> reduced = uut.applyMilepostReductionAlorithm(null, Double.valueOf(327 / 2));

        // Assert
        assertNull(reduced);
    }

    @Test
    public void applyMilepostReductionAlorithm_empty() {
        // Arrange
        List<Milepost> mps = new ArrayList<>();

        // Act
        List<Milepost> reduced = uut.applyMilepostReductionAlorithm(mps, Double.valueOf(327 / 2));

        // Assert
        assertEquals(0, reduced.size());
    }
}