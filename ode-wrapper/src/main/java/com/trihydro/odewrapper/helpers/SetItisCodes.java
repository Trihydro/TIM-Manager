package com.trihydro.odewrapper.helpers;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.CustomItisEnum;
import com.trihydro.library.model.IncidentChoice;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
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

    private List<ItisCode> itisCodes;

    @Autowired
    public void InjectDependencies(ItisCodeService _itisCodeService, IncidentChoicesService _incidentChoicesService) {
        itisCodeService = _itisCodeService;
        incidentChoicesService = _incidentChoicesService;
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

        System.out.println("Availablity : " + wydotTim.getAvailability());
        System.out.println("Exit : " + wydotTim.getExit());

        if (code != null)
            items.add(wydotTim.getAvailability().toString());

        // for parking TIM, content=exitService, and includes additional itis codes
        // depending on if rest area or exit number
        if (wydotTim.getExit() != null) {
            items.add("11794");// Exit Number
            if (wydotTim.getExit().toLowerCase().equals("turnout")
                    || wydotTim.getExit().toLowerCase().equals("parking")) {
                items.add(String.valueOf(((int) Math.round(wydotTim.getMileMarker()))));
            } else {
                List<String> list = splitExitNumberFromLetter(wydotTim.getExit());
                items.add(String.valueOf(Integer.parseInt(list.get(0))));
                if (list.size() > 1) {
                    items.add(list.get(1));
                    System.out.println("list: " + list.get(1));
                }
            }
        } else {
            items.add("7986");// Rest Area
            System.out.println("rest area");
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

        System.out.println("availability:" + wydotTim.getAvailability());

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

    private int convertNumberToItisCode(int number) {
        int itisCode = number + 12544;
        return itisCode;
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

}