/*
* ============LICENSE_START=======================================================
* ONAP : DCAE
* ================================================================================
* Copyright 2019 China Mobile
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.onap.datalake.feeder.util.pm;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * converting Performance Measurement XML to VES JSON
 * 
 * @author Guobiao Mo
 *
 */
public class PerformanceMeasurement {

	static Map<String, Function> functionMap = new HashMap<>();

	static {
		Function f = new Function("UPF");
		f.measurements.put("9", "UPF.N3IncPkt._Dnn");
		f.measurements.put("11", "UPF.N3OgPkt._Dnn");
		functionMap.put(f.type, f);

		f = new Function("AMF");
		f.measurements.put("46", "AMF.RegSub._NS");
		functionMap.put(f.type, f);
	}

	Element pmFile;
	Element fileHeader;

	Function function;
	TimeZone timeZone;
	SimpleDateFormat dateFormat;

	public PerformanceMeasurement(String xmlContent) throws JDOMException, IOException {
		Reader in = new StringReader(xmlContent);
		SAXBuilder jdomBuilder = new SAXBuilder();
		Document jdomDocument = jdomBuilder.build(in);

		pmFile = jdomDocument.getRootElement();

		fileHeader = pmFile.getChild("FileHeader");

		String type = fileHeader.getChildText("ElementType");

		function = functionMap.get(type);

		String tz = fileHeader.getChildText("TimeZone");
		tz = tz.replace("UTC", "GMT");
		ZoneId zoneId = ZoneId.of(tz);
		timeZone = TimeZone.getTimeZone(zoneId);

		TimeZone.setDefault(timeZone);

		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	}

	public List<String> toVes() throws ParseException {
		return toVes(false);
	}

	public List<String> toVes(boolean formatted) throws ParseException {

		Element measurements = pmFile.getChild("Measurements");
		Element pmData = measurements.getChild("PmData");

		List<Element> objects = pmData.getChildren("Object");

		List<Slice> slices = new ArrayList<>();
		for (Element object : objects) {
			slices.addAll(getSlices(function, object));
		}

		return constructVes(slices, formatted);
	}

	private Collection<Slice> getSlices(Function function, Element object) {
		Map<String, String> measurements = function.measurements;

		List<Element> cvs = object.getChildren("CV");

		Map<String, Slice> sliceMap = new HashMap<>();

		for (Element cv : cvs) {
			String i = cv.getAttributeValue("i");//"46"
			String name = measurements.get(i); //"AMF.RegSub._NS"
			if (name != null) {
				List<Element> values = cv.getChildren();
				for (int j = 0; j < values.size(); j++) {
					Element key = values.get(j++);//<SN>AMF.RegSub.460_08_010101</SN>
					Element value = values.get(j);//<SV>0</SV>
					String sliceId = extractSliceId(name, key.getText());//"460_08_010101"
					Slice slice = sliceMap.computeIfAbsent(sliceId, k -> new Slice(k));
					//resource = object
					slice.measurements.put(name, Integer.parseInt(value.getText()));
					slice.object = object;
				}
			}
		}

		return sliceMap.values();
	}

	private List<String> constructVes(List<Slice> slices, boolean formatted) throws ParseException {
		List<String> ret = new ArrayList<>();
		for (Slice slice : slices) {
			JSONObject json = constructVes(slice);
			ret.add(formatted ? json.toString(4) : json.toString());
		}
		return ret;
	}

	//VES JSON schema https://docs.onap.org/en/latest/_downloads/0aeaf8d76deac9e0094770dee896041d/CommonEventFormat_30.1_ONAP.json
	private JSONObject constructVes(Slice slice) throws ParseException {
		JSONObject ves = new JSONObject();

		JSONObject commonEventHeader = new JSONObject();
		commonEventHeader.put("domain", "measurement");
		commonEventHeader.put("eventId", "");
		commonEventHeader.put("eventName", function.type);
		commonEventHeader.put("eventType", function.type);
		commonEventHeader.put("lastEpochMicrosec", dateFormat.parse(fileHeader.getChildText("TimeStamp")).getTime());//2019-09-26T21:54:27
		commonEventHeader.put("nfVendorName", fileHeader.getChildText("VendorName"));
		commonEventHeader.put("priority", "High");
		commonEventHeader.put("reportingEntityId", slice.object.getAttributeValue("rmUID"));
		commonEventHeader.put("reportingEntityName", slice.object.getAttributeValue("UserLabel"));
		commonEventHeader.put("sequence", "");
		commonEventHeader.put("sourceId", slice.id);
		commonEventHeader.put("sourceName", slice.id);
		commonEventHeader.put("startEpochMicrosec", dateFormat.parse(fileHeader.getChildText("StartTime")).getTime()); 
		commonEventHeader.put("timeZoneOffset", timeZone.getID());//
		commonEventHeader.put("version", fileHeader.getChildText("PmVersion"));
		commonEventHeader.put("vesEventListenerVersion", "7.1");

		ves.put("commonEventHeader", commonEventHeader);

		JSONObject measurementFields = new JSONObject();

		JSONObject additionalFields = new JSONObject();
		Set<Entry<String, Integer>> entries = slice.measurements.entrySet();
		for (Entry<String, Integer> entry : entries) {
			additionalFields.put(entry.getKey(), entry.getValue());
		}

		measurementFields.put("additionalFields", additionalFields);
		ves.put("measurementFields", measurementFields);

		return ves;
	}

	private static String extractSliceId(String name, String key) {//"AMF.RegSub._NS" : AMF.RegSub.460_08_010101
		int index = name.lastIndexOf('.');
		return key.substring(index + 1);
	}

	//for testing
	public static void main(String[] args) throws JDOMException, IOException, JSONException, ParseException {
		String xmlPath = "C:\\Work\\5G-use-case\\KPI\\spec\\5GC_PM\\JS-PM-AMF-AMFFUNCTION-02-V0.4.0-20190926213000-15.xml\\JS-PM-AMF-AMFFUNCTION-02-V0.4.0-20190926213000-15.xml";
		//String xmlPath = "C:\\Work\\5G-use-case\\KPI\\spec\\5GC_PM\\JS-PM-UPF-UPFFUNCTION-02-V0.4.0-20190926213000-15.xml\\JS-PM-UPF-UPFFUNCTION-02-V0.4.0-20190926213000-15.xml";

		File file = new File(xmlPath);
		String xml = FileUtils.readFileToString(file, "UTF-8");

		PerformanceMeasurement pm = new PerformanceMeasurement(xml);
		List<String> vess = pm.toVes();
		vess.forEach(System.out::println);

	}
}
