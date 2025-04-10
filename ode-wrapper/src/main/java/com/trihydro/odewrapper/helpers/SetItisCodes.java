package com.trihydro.odewrapper.helpers;

import java.util.ArrayList;
import java.util.List;

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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SetItisCodes {
  private final IncidentChoicesService incidentChoicesService;
  private final ItisCodeService itisCodeService;

  private List<IncidentChoice> incidentProblems;
  private List<IncidentChoice> incidentEffects;
  private List<IncidentChoice> incidentActions;

  private List<ItisCode> itisCodes;

  @Autowired
  public SetItisCodes(ItisCodeService _itisCodeService, IncidentChoicesService _incidentChoicesService) {
    itisCodeService = _itisCodeService;
    incidentChoicesService = _incidentChoicesService;
  }

  public List<ItisCode> getItisCodes() {
    if (itisCodes != null) {
      return itisCodes;
    } else {
      itisCodes = itisCodeService.selectAll();
      return itisCodes;
    }
  }

  public List<String> setItisCodesFromAdvisoryArray(WydotTimRc wydotTim) {

    // check to see if code exists

    List<String> items = new ArrayList<>();
    for (Integer item : wydotTim.getAdvisory()) {

      getItisCodes().stream().filter(x -> x.getItisCode().equals(item)).findFirst().ifPresent(code -> items.add(item.toString()));

    }
    return items;
  }

  public List<String> setItisCodesRc(WydotTimRc wydotTim) {

    List<String> items = new ArrayList<>();

    if (wydotTim.getAdvisory() == null) {
      return items;
    }

    ItisCode code;

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

      if (code != null) {
        items.add(code.getItisCode().toString());
      }
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

    List<String> items = new ArrayList<>();

    // speed limit itis code
    getItisCodes().stream().filter(x -> x.getDescription().equals("speed limit")).findFirst()
        .ifPresent(speedLimit -> items.add(speedLimit.getItisCode().toString()));

    // number e.g 50, convert to ITIS code
    int speed = wydotTim.getSpeed() + 12544;
    items.add(Integer.toString(speed));

    // mph itis code
    getItisCodes().stream().filter(x -> x.getDescription().equals("mph")).findFirst().ifPresent(mph -> items.add(mph.getItisCode().toString()));

    return items;
  }

  public List<String> setItisCodesParking(WydotTimParking wydotTim) {

    // check to see if code exists
    List<String> items = new ArrayList<>();

    ItisCode code = getItisCodes().stream().filter(x -> x.getItisCode().equals(wydotTim.getAvailability())).findFirst().orElse(null);

    log.info("Availablity : {}", wydotTim.getAvailability());
    log.info("Exit : {}", wydotTim.getExit());

    if (code != null) {
      items.add(wydotTim.getAvailability().toString());
    }

    // for parking TIM, content=exitService, and includes additional itis codes
    // depending on if rest area or exit number
    if (wydotTim.getExit() != null) {
      // if exit, the exit number should be a text value.
      // This has some strange implications as seen here
      // https://github.com/usdot-jpo-ode/jpo-ode/blob/540b79f1697f4d6464e8c4b8491666ec9cf08d8d/jpo-ode-plugins/src/main/java/us/dot/its/jpo/ode/plugin/j2735/builders/TravelerMessageFromHumanToAsnConverter.java#L337
      // the ODE translates a text value only if we start with a single quote to
      // denote this. No ending quote is used
      items.add("11794");// Exit Number
      if (wydotTim.getExit().equalsIgnoreCase("turnout") || wydotTim.getExit().equalsIgnoreCase("parking")) {
        items.add("'" + (int) Math.round(wydotTim.getMileMarker()));
      } else {
        items.add("'" + wydotTim.getExit());
      }
    } else {
      items.add("7986");// Rest Area
      log.info("rest area");
    }

    return items;
  }

  public List<String> setItisCodesIncident(WydotTimIncident wydotTim) {
    List<String> items = new ArrayList<>();

    // action
    IncidentChoice incidentAction = getIncidentActions().stream().filter(x -> x.getCode().equals(wydotTim.getAction())).findFirst().orElse(null);

    // if action is not null and action itis code exists
    if (incidentAction != null && incidentAction.getItisCodeId() != null) {
      getItisCodes().stream().filter(x -> x.getItisCodeId().equals(incidentAction.getItisCodeId())).findFirst()
          .ifPresent(actionItisCode -> items.add(actionItisCode.getItisCode().toString()));
    }

    // effect
    IncidentChoice incidentEffect = getIncidentEffects().stream().filter(x -> x.getCode().equals(wydotTim.getEffect())).findFirst().orElse(null);

    // if effect is not null and effect itis code exists
    if (incidentEffect != null && incidentEffect.getItisCodeId() != null) {
      getItisCodes().stream().filter(x -> x.getItisCodeId().equals(incidentEffect.getItisCodeId())).findFirst()
          .ifPresent(effectItisCode -> items.add(effectItisCode.getItisCode().toString()));
    }

    if (wydotTim.getProblem() != null && !wydotTim.getProblem().equals("other")) {
      // Retrieve the matching incident problem based on the provided code
      IncidentChoice incidentProblem =
          getIncidentProblems().stream().filter(problem -> problem.getCode().equals(wydotTim.getProblem())).findFirst().orElse(null);

      // Add the ITIS code if the incident problem exists and has a valid ITIS code ID
      if (incidentProblem != null) {
        Integer itisCodeId = incidentProblem.getItisCodeId();
        if (itisCodeId != null) {
          getItisCodes().stream().filter(code -> code.getItisCodeId().equals(itisCodeId)).findFirst()
              .ifPresent(problemItisCode -> items.add(problemItisCode.getItisCode().toString()));
        }
      }
    } else {
      items.addAll(handleOtherIncidentProblem(wydotTim));
    }

    // If no incident problem is provided, default to "Incident" (ITIS code 531)
    if (items.isEmpty()) {
      items.add("531"); // 531 is "Incident"
    }

    return items;
  }

  private List<String> handleOtherIncidentProblem(WydotTimIncident wydotTim) {
    List<String> items = new ArrayList<>();
    if (wydotTim.getProblemOtherText() == null) {
      log.warn("problemOtherText is null for 'other' incident problem");
      return items;
    }
    String problemOtherText = wydotTim.getProblemOtherText();

    if (!problemOtherText.contains("GVW")) {
      log.error("Unsupported problemOtherText: {}", problemOtherText);
      return items;
    }

    // Extract the weight limit from the problemOtherText
    String weightLimitInPounds = getWeightLimitFromProblemOtherText(problemOtherText);
    if (weightLimitInPounds == null) {
      log.warn("Weight limit not found in problemOtherText: {}", problemOtherText);
      return items;
    }

    String weightLimitInItisCode = null;
    try {
      // Convert weight limit to ITIS code
      weightLimitInItisCode = translateWeightToItisCode(Integer.parseInt(weightLimitInPounds));
    } catch (WeightNotSupportedException e) {
      log.warn("Weight limit not supported: {}", weightLimitInPounds);
      return items;
    }

    items.add("2563"); // Truck restriction
    items.add("2577"); // Gross-Weight-Limit
    items.add(weightLimitInItisCode); // Weight limit in ITIS code
    items.add("8739"); // Pounds

    return items;
  }

  /**
   * Given a problemOtherText string of the format "Weight limit of 60,000 GVW is in effect",
   * return the weight limit (60000) in pounds as a string.
   */
  private String getWeightLimitFromProblemOtherText(String problemOtherText) {
    problemOtherText = problemOtherText.replaceAll(",", "");
    String[] parts = problemOtherText.split(" ");
    for (String part : parts) {
      if (part.matches("\\d{1,5}")) { // Match a number with 1 to 5 digits
        return part;
      }
    }
    return null; // Return null if no weight limit found
  }

  public List<IncidentChoice> getIncidentProblems() {
    if (incidentProblems != null) {
      return incidentProblems;
    } else {
      incidentProblems = incidentChoicesService.selectAllIncidentProblems();
      return incidentProblems;
    }
  }

  public List<IncidentChoice> getIncidentEffects() {
    if (incidentEffects != null) {
      return incidentEffects;
    } else {
      incidentEffects = incidentChoicesService.selectAllIncidentEffects();
      return incidentEffects;
    }
  }

  public List<IncidentChoice> getIncidentActions() {
    if (incidentActions != null) {
      return incidentActions;
    } else {
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
    List<String> itisCodes = new ArrayList<>();

    int weightInPounds = tim.getData();

    itisCodes.add("5127"); // Strong winds
    itisCodes.add("2563"); // Truck restriction
    itisCodes.add("2569"); // No high profile vehicles
    itisCodes.add("7682"); // Below
    itisCodes.add("2577"); // Gross-Weight-Limit
    itisCodes.add(translateWeightToItisCode(weightInPounds)); // Weight, translated from pounds to ITIS code
    itisCodes.add("8739"); // Pounds

    return itisCodes;
  }

  /**
   * This method translates the weight in pounds to its corresponding ITIS code.
   * These are large number ITIS codes and do not abide by the standard translations used for other numbers such as mph.
   * Supported weights are 20000 to 30000 in increments of 1000 and 30000 to 70000 in increments of 5000
   *
   * @throws WeightNotSupportedException if the weight is not supported
   */
  private String translateWeightToItisCode(int weightInPounds) throws WeightNotSupportedException {
    switch (weightInPounds) {
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

  public static class WeightNotSupportedException extends Exception {
    public WeightNotSupportedException(String message) {
      super(message);
    }
  }

}