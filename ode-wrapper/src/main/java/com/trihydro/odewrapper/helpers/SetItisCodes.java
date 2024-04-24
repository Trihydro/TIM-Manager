package com.trihydro.odewrapper.helpers;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.CustomItisEnum;
import com.trihydro.library.model.IncidentChoice;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.odewrapper.model.WydotTimBowr;
import com.trihydro.odewrapper.model.WydotTimIncident;
import com.trihydro.odewrapper.model.WydotTimParking;
import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.odewrapper.model.WydotTimVsl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetItisCodes {
    private List<IncidentChoice> incidentProblems;
    private List<IncidentChoice> incidentEffects;
    private List<IncidentChoice> incidentActions;
    private IncidentChoicesService incidentChoicesService;
    private ItisCodeService itisCodeService;
    private Utility utility;

    private List<ItisCode> itisCodes;

    @Autowired
    public void InjectDependencies(ItisCodeService _itisCodeService, IncidentChoicesService _incidentChoicesService,
            Utility _utility) {
        itisCodeService = _itisCodeService;
        incidentChoicesService = _incidentChoicesService;
        utility = _utility;
    }

    public List<ItisCode> getItisCodes() {
        if (itisCodes != null)
            return itisCodes;
        else {
            itisCodes = itisCodeService.selectAll();
            return itisCodes;
        }
    }

    public List<String> setItisCodesFromAdvisoryArray(WydotTimRc wydotTim) {

        // check to see if code exists

        List<String> items = new ArrayList<String>();
        for (Integer item : wydotTim.getAdvisory()) {

            ItisCode code = getItisCodes().stream().filter(x -> x.getItisCode().equals(item)).findFirst().orElse(null);

            if (code != null)
                items.add(item.toString());
        }
        return items;
    }

    public List<String> setItisCodesRc(WydotTimRc wydotTim) {

        List<String> items = new ArrayList<String>();

        if (wydotTim.getAdvisory() == null) {
            return items;
        }

        ItisCode code = null;

        for (Integer item : wydotTim.getAdvisory()) {

            var alphaItis = getCustomAlphabetic(item);
            if (alphaItis != null) {
                items.add(alphaItis);
                continue;
            }
            // map "closed" itis code
            if (item == 769) {
                code = getItisCodes().stream().filter(x -> x.getItisCode().equals(770)).findFirst().orElse(null);
            } else {
                code = getItisCodes().stream().filter(x -> x.getItisCode().equals(item)).findFirst().orElse(null);
            }

            if (code != null)
                items.add(code.getItisCode().toString());
        }

        return items;
    }

    public String getCustomAlphabetic(Integer itisCode) {
        String text = null;
        var en = CustomItisEnum.valueOf(itisCode);
        if (en != null) {
            text = en.getStringValue();
        }
        return text;
    }

    public List<String> setItisCodesVsl(WydotTimVsl wydotTim) {

        List<String> items = new ArrayList<String>();

        // speed limit itis code
        ItisCode speedLimit = getItisCodes().stream().filter(x -> x.getDescription().equals("speed limit")).findFirst()
                .orElse(null);
        if (speedLimit != null) {
            items.add(speedLimit.getItisCode().toString());
        }

        // number e.g 50, convert to ITIS code
        Integer speed = wydotTim.getSpeed() + 12544;
        items.add(speed.toString());

        // mph itis code
        ItisCode mph = getItisCodes().stream().filter(x -> x.getDescription().equals("mph")).findFirst().orElse(null);
        if (mph != null) {
            items.add(mph.getItisCode().toString());
        }

        return items;
    }

    public List<String> setItisCodesParking(WydotTimParking wydotTim) {

        // check to see if code exists
        List<String> items = new ArrayList<String>();

        ItisCode code = getItisCodes().stream().filter(x -> x.getItisCode().equals(wydotTim.getAvailability()))
                .findFirst().orElse(null);

        utility.logWithDate("Availablity : " + wydotTim.getAvailability(), this.getClass());
        utility.logWithDate("Exit : " + wydotTim.getExit(), this.getClass());

        if (code != null)
            items.add(wydotTim.getAvailability().toString());

        // for parking TIM, content=exitService, and includes additional itis codes
        // depending on if rest area or exit number
        if (wydotTim.getExit() != null) {
            // if exit, the exit number should be a text value.
            // This has some strange implications as seen here
            // https://github.com/usdot-jpo-ode/jpo-ode/blob/540b79f1697f4d6464e8c4b8491666ec9cf08d8d/jpo-ode-plugins/src/main/java/us/dot/its/jpo/ode/plugin/j2735/builders/TravelerMessageFromHumanToAsnConverter.java#L337
            // the ODE translates a text value only if we start with a single quote to
            // denote this. No ending quote is used
            items.add("11794");// Exit Number
            if (wydotTim.getExit().toLowerCase().equals("turnout")
                    || wydotTim.getExit().toLowerCase().equals("parking")) {
                items.add("'" + String.valueOf(((int) Math.round(wydotTim.getMileMarker()))));
            } else {
                items.add("'" + wydotTim.getExit());
            }
        } else {
            items.add("7986");// Rest Area
            utility.logWithDate("rest area", this.getClass());
        }

        return items;
    }

    public List<String> splitExitNumberFromLetter(String exit) {

        List<String> list = new ArrayList<String>();
        String exitNumber = "";
        String exitLetter = "";
        for (int i = 0; i < exit.length(); i++) {
            if (StringUtils.isNumeric(String.valueOf(exit.charAt(i)))) {
                exitNumber += exit.charAt(i);
            } else {
                exitLetter += exit.charAt(i);
            }
        }

        list.add(exitNumber);
        if (exitLetter.length() > 0)
            list.add(exitLetter);

        return list;
    }

    public List<String> setItisCodesFromAvailability(WydotTimParking wydotTim) {

        // check to see if code exists
        List<String> items = new ArrayList<String>();

        utility.logWithDate("availability:" + wydotTim.getAvailability(), this.getClass());

        ItisCode code = getItisCodes().stream().filter(x -> x.getItisCode().equals(wydotTim.getAvailability()))
                .findFirst().orElse(null);

        if (code != null)
            items.add(wydotTim.getAvailability().toString());

        if (wydotTim.getExit() != null) {
            items.add("11794");
            items.add(wydotTim.getExit());
        } else {
            items.add("7986");
        }

        return items;
    }

    public List<String> setItisCodesIncident(WydotTimIncident wydotTim) {
        List<String> items = new ArrayList<String>();

        // action
        IncidentChoice incidentAction = getIncidentActions().stream()
                .filter(x -> x.getCode().equals(wydotTim.getAction())).findFirst().orElse(null);

        // if action is not null and action itis code exists
        if (incidentAction != null && incidentAction.getItisCodeId() != null) {
            ItisCode actionItisCode = getItisCodes().stream()
                    .filter(x -> x.getItisCodeId().equals(incidentAction.getItisCodeId())).findFirst().orElse(null);
            if (actionItisCode != null) {
                items.add(actionItisCode.getItisCode().toString());
            }
        }

        // effect
        IncidentChoice incidentEffect = getIncidentEffects().stream()
                .filter(x -> x.getCode().equals(wydotTim.getEffect())).findFirst().orElse(null);

        // if effect is not null and effect itis code exists
        if (incidentEffect != null && incidentEffect.getItisCodeId() != null) {
            ItisCode effectItisCode = getItisCodes().stream()
                    .filter(x -> x.getItisCodeId().equals(incidentEffect.getItisCodeId())).findFirst().orElse(null);
            if (effectItisCode != null) {
                items.add(effectItisCode.getItisCode().toString());
            }
        }

        // problem
        IncidentChoice incidentProblem = getIncidentProblems().stream()
                .filter(x -> x.getCode().equals(wydotTim.getProblem())).findFirst().orElse(null);

        // if problem is not null and problem itis code exists
        if (incidentProblem != null && incidentProblem.getItisCodeId() != null) {
            ItisCode problemItisCode = getItisCodes().stream()
                    .filter(x -> x.getItisCodeId().equals(incidentProblem.getItisCodeId())).findFirst().orElse(null);
            if (problemItisCode != null) {
                items.add(problemItisCode.getItisCode().toString());
            }
        }

        if (items.size() == 0)
            items.add("531");// 531 is "Incident"

        return items;
    }

    public List<IncidentChoice> getIncidentProblems() {
        if (incidentProblems != null)
            return incidentProblems;
        else {
            incidentProblems = incidentChoicesService.selectAllIncidentProblems();
            return incidentProblems;
        }
    }

    public List<IncidentChoice> getIncidentEffects() {
        if (incidentEffects != null)
            return incidentEffects;
        else {
            incidentEffects = incidentChoicesService.selectAllIncidentEffects();
            return incidentEffects;
        }
    }

    public List<IncidentChoice> getIncidentActions() {
        if (incidentActions != null)
            return incidentActions;
        else {
            incidentActions = incidentChoicesService.selectAllIncidentActions();
            return incidentActions;
        }
    }

    public List<String> setItisCodesRw(WydotTim wydotTim) {

        List<String> items = new ArrayList<String>();

        items.add("1025");

        return items;
    }

    public List<String> setItisCodesBowr(WydotTimBowr tim) throws WeightNotSupportedException {
        List<String> itisCodes = new ArrayList<String>();

        int weightInPounds = tim.getData();

        itisCodes.add("5127");
        itisCodes.add("2563");
        itisCodes.add("2569");
        itisCodes.add("7682");
        itisCodes.add("2557");
        itisCodes.add(translateWeightToItisCode(weightInPounds));
        itisCodes.add("8739");

        return itisCodes;
    }

    /**
     * Supported weights are 20000 to 30000 in increments of 1000 and 30000 to 70000 in increments of 5000
     * @throws WeightNotSupportedException 
     */
    private String translateWeightToItisCode(int weightInPounds) throws WeightNotSupportedException {
        switch(weightInPounds) {
            case 20000:
                return "11589";
            case 21000:
                return "11590";
            case 22000:
                return "11591";
            case 23000:
                return "11592";
            case 24000:
                return "11593";
            case 25000:
                return "11594";
            case 26000:
                return "11595";
            case 27000:
                return "11596";
            case 28000:
                return "11597";
            case 29000:
                return "11598";
            case 30000:
                return "11599";
            case 35000:
                return "11600";
            case 40000:
                return "11601";
            case 45000:
                return "11602";
            case 50000:
                return "11603";
            case 55000:
                return "11604";
            case 60000:
                return "11605";
            case 65000:
                return "11606";
            case 70000:
                return "11607";
            default:
                throw new WeightNotSupportedException("Weight " + weightInPounds + " is not supported");
        }
    }
    
    public class WeightNotSupportedException extends Exception {
        public WeightNotSupportedException(String message) {
            super(message);
        }
    }

}