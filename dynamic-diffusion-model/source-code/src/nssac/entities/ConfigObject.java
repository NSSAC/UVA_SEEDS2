package nssac.entities;

import java.util.Set;

public class ConfigObject {
	
	private ModelObject modelCoefficients;
	private int replicates;
	private int ticks;
	private String region;
	
	// files
	private String hidMapperFile;
	private String inputCSV;
	private String outFile;
	private String aggregatedOutFile;
	private String mile1File;
	private String mile3File;
	private String mile4File;
	private String networkDBPath;
	
	private boolean isIncentiveApplied;
	private String seedingStrategy;
	private Set<Integer> seedingTicks;
	private int totalBudget;
	
	public ModelObject getModelCoefficients() {
		return modelCoefficients;
	}
	public int getReplicates() {
		return replicates;
	}
	public String getRegion() {
		return region;
	}
	public String getInputCSV() {
		return inputCSV;
	}
	public String getOutFile() {
		return outFile;
	}
	public String getAggregatedOutFile() {
		return aggregatedOutFile;
	}
	public void setModelCoefficients(ModelObject modelCoefficients) {
		this.modelCoefficients = modelCoefficients;
	}
	public void setReplicates(int replicates) {
		this.replicates = replicates;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public void setInputCSV(String inputCSV) {
		this.inputCSV = inputCSV;
	}
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	public void setAggregatedOutFile(String aggregatedOutFile) {
		this.aggregatedOutFile = aggregatedOutFile;
	}
	
	public int getTicks() {
		return ticks;
	}
	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public String getMile1File() {
		return mile1File;
	}
	public void setMile1File(String mile1File) {
		this.mile1File = mile1File;
	}

	public String getMile4File() {
		return mile4File;
	}
	public void setMile4File(String mile4File) {
		this.mile4File = mile4File;
	}

	public String getMile3File() {
		return mile3File;
	}
	public void setMile3File(String mile3File) {
		this.mile3File = mile3File;
	}

	public String getHidMapperFile() {
		return hidMapperFile;
	}
	public void setHidMapperFile(String hidMapperFile) {
		this.hidMapperFile = hidMapperFile;
	}

	public boolean isIncentiveApplied() {
		return isIncentiveApplied;
	}
	public void setIncentiveApplied(boolean isIncentiveApplied) {
		this.isIncentiveApplied = isIncentiveApplied;
	}

	public Set<Integer> getSeedingTicks() {
		return seedingTicks;
	}
	public void setSeedingTicks(Set<Integer> seedingTicks) {
		this.seedingTicks = seedingTicks;
	}

	public int getTotalBudget() {
		return totalBudget;
	}
	public void setTotalBudget(int totalBudget) {
		this.totalBudget = totalBudget;
	}

	public String getSeedingStrategy() {
		return seedingStrategy;
	}
	public void setSeedingStrategy(String seedingStrategy) {
		this.seedingStrategy = seedingStrategy;
	}

	public String getNetworkDBPath() {
		return networkDBPath;
	}
	public void setNetworkDBPath(String networkDBPath) {
		this.networkDBPath = networkDBPath;
	}

	public static final String REGION="region";
	public static final String REPLICATES = "replicates";
	public static final String INPUT_CSV = "inputCSV";
	public static final String OUT_FILE = "outFile";
	public static final String AGGREGATED_OUT_FILE = "aggregatedOutFile";
	public static final String TICKS = "ticks";
	public static final String MILE1_FILE = "mile1File";
	public static final String MILES3_FILE = "mile3File";
	public static final String MILES4_FILE = "mile4File";
	public static final String HID_MAPPER_FILE = "hidMapperFile";
	public static final String IS_INCENTIVE_APPLIED = "incentive";		
	public static final String SEEDING_TICKS = "seedingTicks";
	public static final String TOTAL_BUDGET = "totalBudget";
	public static final String SEEDING_STRATEGY = "seedingStrategy";
	public static final String GREEDY_SEEDING_STRATEGY = "greedy";
	public static final String RANDOM_SEEDING_STRATEGY = "random";
	public static final String NETWORK_DB_PATH = "networkDBPath";
	
}
