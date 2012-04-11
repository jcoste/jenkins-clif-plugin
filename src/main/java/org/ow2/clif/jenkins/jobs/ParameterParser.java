package org.ow2.clif.jenkins.jobs;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterParser {
	Pattern re = Pattern.compile("^(.*)\\[(.*)\\]$");

	/**
	 * suited to parse hash notation of boolean input values
	 *
	 * @param name
	 * @return
	 */
	public Map<String, String> parse(String name) {
		Matcher matcher = re.matcher(name);
		HashMap<String, String> results = Maps.newHashMapWithExpectedSize(1);
		if (matcher.matches()) {
			results.put(matcher.group(1), matcher.group(2));
		}
		return results;
	}
}
