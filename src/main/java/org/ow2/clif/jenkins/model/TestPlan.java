package org.ow2.clif.jenkins.model;

import java.util.*;

public class TestPlan {

	private String name;
	private Date date;
	private List<Probe> probes;
	private List<Injector> injectors;
	private List<Measure> aggregatedMeasures;

	private transient boolean initDone = false;
	private transient Map<String, List<Probe>> probesByServer;
	private transient Map<String, List<Injector>> injectorsByServer;

	public TestPlan() {
		super();
	}

	public TestPlan(String name, Date date) {
		this();
		this.name = name;
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Probe> getProbes() {
		return probes;
	}

	public void setProbes(List<Probe> probes) {
		this.probes = probes;
	}

	public void addProbe(Probe probe) {
		if (probes == null) {
			probes = new ArrayList<Probe>();
		}
		probes.add(probe);
	}

	public List<Injector> getInjectors() {
		return injectors;
	}

	public void setInjectors(List<Injector> injectors) {
		this.injectors = injectors;
	}

	public void addInjector(Injector injector) {
		if (injectors == null) {
			injectors = new ArrayList<Injector>();
		}
		injectors.add(injector);
	}


	public List<Measure> getAggregatedMeasures() {
		return aggregatedMeasures;
	}

	public Measure getAggregatedMeasure(String measureName) {
		for (Measure measure : aggregatedMeasures) {
			if (measureName.equals(measure.getName())) {
				return measure;
			}

		}
		return null;
	}

	public void setAggregatedMeasures(List<Measure> aggregatedMeasures) {
		this.aggregatedMeasures = aggregatedMeasures;
	}

	public void addAggregatedMeasure(Measure measure) {
		if (aggregatedMeasures == null) {
			aggregatedMeasures = new ArrayList<Measure>();
		}
		aggregatedMeasures.add(measure);
	}

	public Set<String> getServers() {
		Set<String> servers = new HashSet<String>();
		probesByServer = new HashMap<String, List<Probe>>();
		injectorsByServer = new HashMap<String, List<Injector>>();

		if (getProbes() != null) {
			for (Probe p : getProbes()) {
				servers.add(p.getServer());
				List<Probe> serverProbes = probesByServer.get(p.getServer());
				if (serverProbes == null) {
					serverProbes = new ArrayList<Probe>();
					probesByServer.put(p.getServer(), serverProbes);
				}
				serverProbes.add(p);
			}
		}
		if (getInjectors() != null) {
			for (Injector i : getInjectors()) {
				servers.add(i.getServer());
				List<Injector> serverInjectors = injectorsByServer.get(i.getServer());
				if (serverInjectors == null) {
					serverInjectors = new ArrayList<Injector>();
					injectorsByServer.put(i.getServer(), serverInjectors);
				}
				serverInjectors.add(i);
			}
		}
		initDone = true;
		return servers;
	}

	public List<Probe> getProbesByServer(String serverName) {
		if (!initDone) {
			getServers();
		}
		return probesByServer.get(serverName);
	}

	public List<Injector> getInjectorsByServer(String serverName) {
		if (!initDone) {
			getServers();
		}
		return injectorsByServer.get(serverName);
	}

	public List<Alarm> getAlarms(Alarm.Severity sev) {
		List<Alarm> aggregatedAlarms = getAlarms();
		if (aggregatedAlarms == null) {
			return null;
		}
		List<Alarm> res = new ArrayList<Alarm>();
		for (Alarm a : aggregatedAlarms) {
			if (a.getSeverity().equals(sev)) {
				res.add(a);
			}
		}
		if (res.isEmpty()) {
			return null;
		}
		return res;
	}

	public List<Alarm> getAlarms() {
		List<Alarm> aggregatedAlarms = new ArrayList<Alarm>();
		if (probes != null) {
			for (Probe probe : probes) {
				if (probe.getAlarms() != null) {
					aggregatedAlarms.addAll(probe.getAlarms());
				}
			}
		}
		if (injectors != null) {
			for (Injector injector : injectors) {
				if (injector.getAlarms() != null) {
					aggregatedAlarms.addAll(injector.getAlarms());
				}
			}
		}
		if (aggregatedAlarms.isEmpty()) {
			return null;
		}
		return aggregatedAlarms;
	}

	public List<Alarm> getAlarms(String sev) {
		Alarm.Severity severity = Alarm.Severity.valueOf(sev);
		return getAlarms(severity);
	}

}
