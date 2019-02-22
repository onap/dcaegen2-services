/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
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
package org.onap.datalake.feeder.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;


/*
 * read sample json and output supervisor to  resources\druid\generated
 * need manual edit to be production ready, final versions are in resources\druid
 * 
 * http://druid.io/docs/latest/tutorials/tutorial-ingestion-spec.html
 * http://druid.io/docs/latest/ingestion/flatten-json
 * 
 * 
 * todo:
 * reduce the manual editing
 * path hard coded
 * auto get topics,
 * auto get sample, and for each topic, get multiple samples.
 * make supervisor file names consistent
 * dimension type default is string, in msgrtr.apinode.metrics.dmaap , many are long/double, so need to generate dimensionsSpec, this is done at the end of printFlattenSpec()
 */

public class DruidSupervisorGenerator {

	Template template = null;
	VelocityContext context;

	List<String[]> dimensions;

	public DruidSupervisorGenerator() {
		dimensions = new ArrayList<>();

		Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

		Velocity.init();

		context = new VelocityContext();

		context.put("host", "dl_dmaap_kf");

		template = Velocity.getTemplate("druid/kafka-supervisor-template.vm");
	}

	public void printNode(String prefix, JsonNode node) {

		// lets see what type the node is
		//		System.out.println("NodeType=" + node.getNodeType() + ", isContainerNode=" + node.isContainerNode() + ", " + node); // prints OBJECT

		if (node.isContainerNode()) {

			Iterator<Entry<String, JsonNode>> fields = node.fields();

			while (fields.hasNext()) {
				Entry<String, JsonNode> field = fields.next();
				//				System.out.println("--------"+field.getKey()+"--------");
				printNode(prefix + "." + field.getKey(), field.getValue());
			}

			if (node.isArray()) {
				Iterator<JsonNode> elements = node.elements();
				int i = 0;
				while (elements.hasNext()) {
					JsonNode element = elements.next();
					printNode(prefix + "[" + i + "]", element);
					i++;
				}
			}

		} else {
			printFlattenSpec(node.getNodeType(), prefix);
		}

	}

	public void printFlattenSpec(JsonNodeType type, String path) {
		String name = path.substring(2).replace('.', ':');
		// lets see what type the node is
		System.out.println("{");
		System.out.println("\"type\": \"path\",");
		System.out.println("\"name\": \"" + name + "\",");
		System.out.println("\"expr\": \"" + path + "\"");
		System.out.println("},");

		dimensions.add(new String[] { name, path });
		/*
		 //for  dimensionsSpec
				if (JsonNodeType.NUMBER.equals(type)) {
					System.out.println("{");
					System.out.println("\"type\": \"long\",");
					System.out.println("\"name\": \"" + name + "\","); 
					System.out.println("},");
				} else {
					System.out.println("\"" + name + "\",");
		
				}
		*/
	}

	public void doTopic(String topic) throws IOException {
		dimensions.clear();

		String sampleFileName = "C:\\git\\onap\\datalake\\olap\\src\\main\\resources\\druid\\" + topic + "-sample-format.json";//FIXME hard coded path
		String outputFileName = "C:\\git\\onap\\datalake\\olap\\src\\main\\resources\\druid\\generated\\" + topic + "-kafka-supervisor.json";

		// Get the contents of json as a string using commons IO IOUTils class.
		String sampleJson = Util.getTextFromFile(sampleFileName);

		// create an ObjectMapper instance.
		ObjectMapper mapper = new ObjectMapper();
		// use the ObjectMapper to read the json string and create a tree
		JsonNode root = mapper.readTree(sampleJson);
		printNode("$", root);

		context.put("topic", topic);
		context.put("timestamp", "event-header:timestamp");//FIXME hard coded, should be topic based
		context.put("timestampFormat", "yyyyMMdd-HH:mm:ss:SSS");//FIXME hard coded, should be topic based

		context.put("dimensions", dimensions);

		BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));

		template.merge(context, out);
		out.close();
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		String[] topics = new String[] { "AAI-EVENT", "msgrtr.apinode.metrics.dmaap", "unauthenticated.DCAE_CL_OUTPUT", "unauthenticated.SEC_FAULT_OUTPUT" };//FIXME hard coded

		DruidSupervisorGenerator p = new DruidSupervisorGenerator();

		for (String topic : topics) {
			p.doTopic(topic);
		}
	}
}
