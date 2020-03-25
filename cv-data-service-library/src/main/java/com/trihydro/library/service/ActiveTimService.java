package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.ActiveRsuTimQueryModel;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotTim;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ActiveTimService extends CvDataServiceLibrary {

	public static Long insertActiveTim(ActiveTim activeTim) {
		String url = String.format("%s/active-tim/add", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ActiveTim> entity = new HttpEntity<ActiveTim>(activeTim, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}

	public static Boolean updateActiveTim_SatRecordId(Long activeTimId, String satRecordId) {
		String url = String.format("%s/active-tim/update-sat-record-id/%d/%s", CVRestUrl, activeTimId, satRecordId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Boolean> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.PUT, entity,
				Boolean.class);
		return response.getBody();
	}

	public void addItisCodesToActiveTim(ActiveTim activeTim) {
		String url = String.format("%s/active-tim/itis-codes/%d", CVRestUrl, activeTim.getActiveTimId());
		ResponseEntity<Integer[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url, Integer[].class);
		activeTim.setItisCodes(Arrays.asList(response.getBody()));
	}

	public static boolean deleteActiveTim(Long activeTimId) {
		String url = String.format("%s/active-tim/delete-id/%d", CVRestUrl, activeTimId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Boolean> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.DELETE,
				entity, Boolean.class);
		return response.getBody();
	}

	public static boolean deleteActiveTimsById(List<Long> activeTimIds) {
		String url = String.format("%s/active-tim/delete-ids", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<List<Long>> entity = new HttpEntity<List<Long>>(activeTimIds, headers);
		ResponseEntity<Boolean> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.DELETE,
				entity, Boolean.class);
		return response.getBody();
	}

	// utility
	public static List<Integer> getActiveTimIndicesByRsu(String rsuTarget) {
		String url = String.format("%s/active-tim/indices-rsu/%s", CVRestUrl, rsuTarget);
		ResponseEntity<Integer[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url, Integer[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<ActiveTim> getActiveTimsByWydotTim(List<? extends WydotTim> wydotTims, Long timTypeId) {
		String url = String.format("%s/active-tim/get-by-wydot-tim/%d", CVRestUrl, timTypeId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<List<? extends WydotTim>> entity = new HttpEntity<List<? extends WydotTim>>(wydotTims, headers);
		ResponseEntity<ActiveTim[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.PUT,
				entity, ActiveTim[].class);
		return Arrays.asList(response.getBody());
	}

	// get Active TIMs by client ID direction
	public static List<ActiveTim> getActiveTimsByClientIdDirection(String clientId, Long timTypeId, String direction) {
		String url = String.format("%s/active-tim/client-id-direction/%s/%d/%s", CVRestUrl, clientId, timTypeId,
				direction);
		ResponseEntity<ActiveTim[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				ActiveTim[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<ActiveTim> getExpiredActiveTims() {
		ResponseEntity<TimUpdateModel[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/expired", TimUpdateModel[].class);
		return Arrays.asList(response.getBody());
	}

	// for GETs
	public static List<ActiveTim> getActivesTimByType(Long timTypeId) {
		String url = String.format("%s/active-tim/tim-type-id/%d", CVRestUrl, timTypeId);
		ResponseEntity<ActiveTim[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				ActiveTim[].class);
		return Arrays.asList(response.getBody());
	}

	// get Active TIMs by client ID direction
	public static ActiveTim getActiveRsuTim(ActiveRsuTimQueryModel artqm) {
		String url = String.format("%s/active-tim/active-rsu-tim", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ActiveRsuTimQueryModel> entity = new HttpEntity<ActiveRsuTimQueryModel>(artqm, headers);
		ResponseEntity<ActiveTim> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST,
				entity, ActiveTim.class);
		return response.getBody();
	}

	/**
	 * Fetch all ActiveTims for RSUs from cv-data-controller
	 * @return List of ActiveTims (including RSU address and index)
	 */
	public List<ActiveTim> getActiveRsuTims() {
		ResponseEntity<ActiveTim[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/active-rsu-tims", ActiveTim[].class);
		return Arrays.asList(response.getBody());
	}

	/**
	 * Calls out to the cv-data-controller REST function to fetch expiring TIMs
	 * 
	 * @return List of TimUpdateModel representing all TIMs expiring within 1 day
	 */
	public static List<TimUpdateModel> getExpiringActiveTims() {
		ResponseEntity<TimUpdateModel[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/expiring", TimUpdateModel[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<ActiveTim> getActiveTimsMissingItisCodes() {
		ResponseEntity<TimUpdateModel[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/missing-itis", TimUpdateModel[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<ActiveTim> getActiveTimsNotSent() {
		ResponseEntity<TimUpdateModel[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/not-sent", TimUpdateModel[].class);
		return Arrays.asList(response.getBody());
	}

	public List<ActiveTim> getActiveTimsForSDX() {
		ResponseEntity<ActiveTim[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/all-sdx", ActiveTim[].class);
		return Arrays.asList(response.getBody());
	}

	// public List<PopulatedRsu> getRsusWithActiveTims() {
	// 	ResponseEntity<PopulatedRsu[]> response = RestTemplateProvider.GetRestTemplate()
	// 			.getForEntity(CVRestUrl + "/active-tim/rsus-with-active-tims", PopulatedRsu[].class);
	// 	return Arrays.asList(response.getBody());
	// }
}