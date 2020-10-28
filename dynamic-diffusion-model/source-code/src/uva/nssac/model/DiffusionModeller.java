package uva.nssac.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nssac.entities.ConfigObject;
import nssac.entities.Household;
import nssac.entities.ModelObject;
import nssac.utility.LogUtility;

/**
 * DatabaseModeller class creates input structures AND sets simulation params AND initializes all the replicate runs. 
 * Network data is processed using sqlite3 database :: //https://stackoverflow.com/questions/14451624/will-sqlite-performance-degrade-if-the-database-size-is-greater-than-2-gigabytes
 * 
 * @author Swapna
 */
public class DiffusionModeller {

	private LogUtility logUtils = new LogUtility();
	private List<Household> households = new ArrayList<Household>();
	private ConfigObject config = new ConfigObject();
	public ExecutorService workExecutor = Executors.newWorkStealingPool();
	
	private Map<Long, Set<Long>> mile1Peers=null;
	private Map<Long, Set<Long>> mile3Peers=null;
	private Map<Long, Set<Long>> mile4Peers=null;
	
	private long hhSize=0;
	
	public static void main(String[] args) {
		DiffusionModeller dbModeller = new DiffusionModeller();
		dbModeller.setSimulationProperties(args[0]);
		dbModeller.readInputs();
		dbModeller.initiateSimulation();
		dbModeller.consolidateOutput();
	}
	
	/**
	 * Pre-requisite : All input files and objects should be created.
	 * This function initializes the simulation. We run this simulation for given no. of replicates and time ticks.
	 * Since household attributes are altered, deep copies are created for every simulation replicate run and
	 * all other structures are kept READ-ONLY.
	 * Depending upon the no. of cores/cpu on each machine, the executor will run parallel replicate simulations.
	 */
	private void initiateSimulation() {
		logUtils.logPrinter(LocalTime.now(), "Initiating the simulation...");
		try {
			// each replicate execute independently following steps
			if(config.getRegion().equals("svr")) {
				List<DbSingleRunSimulator> jobs = new ArrayList<DbSingleRunSimulator>(config.getReplicates());
				for(int r=1; r <= config.getReplicates(); r++) {
					logUtils.logPrinter(LocalTime.now(), "Replicate : " + r);
					Map<Long, Household> hh = new HashMap<Long, Household>();
					Iterator<Household> iterator = households.iterator();
					while(iterator.hasNext()) {
						Household deepCopy = new Household((Household) iterator.next());
						hh.put(deepCopy.getHid(),deepCopy); 
					}
					jobs.add(new DbSingleRunSimulator(r, config, hh));
				}
				households.clear();
				workExecutor.invokeAll(jobs);
			} else {
				List<SingleRunSimulator> jobs = new ArrayList<SingleRunSimulator>(config.getReplicates());
				for(int r=1; r <= config.getReplicates(); r++) {
					logUtils.logPrinter(LocalTime.now(), "Replicate : " + r);
					Map<Long, Household> hh = new HashMap<Long, Household>();
					Iterator<Household> iterator = households.iterator();
					while(iterator.hasNext()) {
						Household deepCopy = new Household((Household) iterator.next());
						hh.put(deepCopy.getHid(),deepCopy); 
					}
					jobs.add(new SingleRunSimulator(r, config, hh, mile1Peers, mile3Peers, mile4Peers));
				}
				households.clear();
				workExecutor.invokeAll(jobs);
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function reads the input file for household info. The paths to all files are set in input.properties 
	 * which is read in setSimulationProperties() function. All paths are read from ConfigObject instance.
	 * ANY ADDITIONAL INPUT REQUIRED BY SIMULATION SHOULD BE READ HERE.
	 * Tasks performed in this function:
	 * 1. Input CSV file according to the modelVersion
	 * 
	 */
	private void readInputs() {
		logUtils.logPrinter(LocalTime.now(), "Reading input csv file : " + config.getInputCSV());
		String csvFile = config.getInputCSV();
		List<String> modelHeader = null;
		modelHeader = get_M1_Header();
		logUtils.logPrinter(LocalTime.now(), "Header for model : " + modelHeader);
		if(config.getRegion().equals("svr")) {
			readM1SVRHouseholdRecords(modelHeader, csvFile);
		} else {
			readM1HouseholdRecords(modelHeader, csvFile);
			ModelObject mo = config.getModelCoefficients(); 
			if(mo.mile1Coeff!=0D)
				processMile1Peers(config.getMile1File());
			if(mo.mile3Coeff!=0D)
				processMile3Peers(config.getMile3File());
			if(mo.mile4Coeff!=0D)
				processMile4Peers(config.getMile4File());
		}
		logUtils.logPrinter(LocalTime.now(), "ALL input files read successfully.");
	}
	
	private int getSourceVertexIndex(String[] header) {
		int i=-1;
		for(i=0; i<header.length; i++) {
			if(header[i].equals("HID1")) {
				break;
			}
		}
		return i;
	}
	
	private void processMile1Peers(String filename) {
		logUtils.logPrinter(LocalTime.now(), "Poplating ajacency list for 1 mile");
		try {
			mile1Peers = new HashMap<Long, Set<Long>>();
			List<String> lines = Files.readAllLines(Paths.get(filename));
			int sourceVertexIndex = getSourceVertexIndex(lines.get(0).split(",")); // get appropriate header index for HID1
			lines.stream().filter(row-> !row.startsWith("HID")).forEach(row -> {
				long hid1 = Long.parseLong(row.split(",")[sourceVertexIndex]);
				long hid2 = Long.parseLong(row.split(",")[sourceVertexIndex+1]);
				if(hid1 != hid2) {
					/*****
					 * while making the csv file, we have lets say 1,2,3,4,5. Then, i, j are iterators on the list.
					 * if i=x, then, j=i+1 or j=i.
					 * Lets say, if we have pairs:: (1,2) , (1,5) , (2,3) , (2,6) ; then, for the first 2 pairs we have entries:
					 * 1 -> 2,5 
					 * 2 -> 1
					 * 5 -> 1
					 */
					if(!mile1Peers.containsKey(hid1))
						mile1Peers.put(hid1, new HashSet<Long>());
					if(!mile1Peers.containsKey(hid2))
						mile1Peers.put(hid2, new HashSet<Long>());
					
					Set<Long> neighbors1 = mile1Peers.get(hid1);
					neighbors1.add(hid2);
					mile1Peers.put(hid1, neighbors1);
					
					Set<Long> neighbors2 = mile1Peers.get(hid2);
					neighbors2.add(hid1);
					mile1Peers.put(hid2, neighbors2);
				}
			});
			logUtils.logPrinter(LocalTime.now(), "DONE Populating ajacency list for 1 mile");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processMile3Peers(String mile3File) {
		logUtils.logPrinter(LocalTime.now(), "Populating ajacency list for 3 miles");
		try {
			mile3Peers = new HashMap<Long, Set<Long>>();
			List<String> lines = Files.readAllLines(Paths.get(mile3File));
			int sourceVertexIndex = getSourceVertexIndex(lines.get(0).split(",")); // get appropriate header index for HID1
			lines.stream().filter(row-> !row.startsWith("HID")).forEach(row -> {
				long hid1 = (long)Double.parseDouble(row.split(",")[sourceVertexIndex]);
				long hid2 = (long)Double.parseDouble(row.split(",")[sourceVertexIndex+1]);
				if(hid1 != hid2) {
					/*****
					 * while making the csv file, we have lets say 1,2,3,4,5. Then, i, j are iterators on the list.
					 * if i=x, then, j=i+1 or j=i.
					 * Lets say, if we have pairs:: (1,2) , (1,5) , (2,3) , (2,6) ; then, for the first 2 pairs we have entries:
					 * 1 -> 2,5 
					 * 2 -> 1
					 * 5 -> 1
					 */
					if(!mile3Peers.containsKey(hid1))
						mile3Peers.put(hid1, new HashSet<Long>());
					if(!mile3Peers.containsKey(hid2))
						mile3Peers.put(hid2, new HashSet<Long>());
					
					Set<Long> neighbors1 = mile3Peers.get(hid1);
					neighbors1.add(hid2);
					mile3Peers.put(hid1, neighbors1);
					
					Set<Long> neighbors2 = mile3Peers.get(hid2);
					neighbors2.add(hid1);
					mile3Peers.put(hid2, neighbors2);
				}
			});
			
			logUtils.logPrinter(LocalTime.now(), "DONE Poplating ajacency list for 3 miles");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processMile4Peers(String filename) {
		logUtils.logPrinter(LocalTime.now(), "Populating ajacency list for 4 miles");
		try {
			mile4Peers = new HashMap<Long, Set<Long>>();
			List<String> lines = Files.readAllLines(Paths.get(filename));
			int sourceVertexIndex = getSourceVertexIndex(lines.get(0).split(",")); // get appropriate header index for HID1
			lines.stream().filter(row-> !row.startsWith("HID")).forEach(row -> {
				long hid1 = (long)Double.parseDouble(row.split(",")[sourceVertexIndex]);
				long hid2 = (long)Double.parseDouble(row.split(",")[sourceVertexIndex+1]);
				if(hid1 != hid2) {
					/*****
					 * while making the csv file, we have lets say 1,2,3,4,5. Then, i, j are iterators on the list.
					 * if i=x, then, j=i+1 or j=i.
					 * Lets say, if we have pairs:: (1,2) , (1,5) , (2,3) , (2,6) ; then, for the first 2 pairs we have entries:
					 * 1 -> 2,5 
					 * 2 -> 1
					 * 5 -> 1
					 */
					if(!mile4Peers.containsKey(hid1))
						mile4Peers.put(hid1, new HashSet<Long>());
					if(!mile4Peers.containsKey(hid2))
						mile4Peers.put(hid2, new HashSet<Long>());
					
					Set<Long> neighbors1 = mile4Peers.get(hid1);
					neighbors1.add(hid2);
					mile4Peers.put(hid1, neighbors1);
					
					Set<Long> neighbors2 = mile4Peers.get(hid2);
					neighbors2.add(hid1);
					mile4Peers.put(hid2, neighbors2);
				}
			});
			
			logUtils.logPrinter(LocalTime.now(), "DONE Poplating ajacency list for 4 miles");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readM1HouseholdRecords(List<String> modelHeader, String csvFile) { 
		try {
			System.out.println("readM1HouseholdRecords()");
			long[] ct= new long[1];
			ct[0] = 0;
			Files.readAllLines(Paths.get(csvFile)).stream().filter(row -> !row.startsWith("hid")).forEach( row -> {
				String[] points = row.split(",");
				long hid = Long.parseLong(points[modelHeader.indexOf("hid")]);
				Household h = new Household(hid);
				h.setAcreage((int)Double.parseDouble(points[modelHeader.indexOf("acreage")]));
				h.setAsrYear((int)Double.parseDouble(points[modelHeader.indexOf("asrYear")]));
				h.setBaths((int)Double.parseDouble(points[modelHeader.indexOf("baths")]));
				h.setBedrooms((int)Double.parseDouble(points[modelHeader.indexOf("bedrooms")]));
				h.setClimate(points[modelHeader.indexOf("climate")]);
				h.setDailyEnergyConsumption(Double.parseDouble(points[modelHeader.indexOf("dailyEnergyConsumption")]));
				h.setEducationLevel(Integer.parseInt(points[modelHeader.indexOf("educationLevel")]));
				h.setHeatingFuel(Integer.parseInt(points[modelHeader.indexOf("heatingFuel")]));
				h.setHhSize(Integer.parseInt(points[modelHeader.indexOf("hhSize")]));
				h.setHouseType(Integer.parseInt(points[modelHeader.indexOf("houseType")]));
				h.setIncome(Integer.parseInt(points[modelHeader.indexOf("income")]));
				h.setNumCarStorage(Integer.parseInt(points[modelHeader.indexOf("numCarStorage")]));
				h.setPool(Integer.parseInt(points[modelHeader.indexOf("pool")]));
				h.setSqFootage(Double.parseDouble(points[modelHeader.indexOf("sqFootage")]));
				h.setTotalValue(Double.parseDouble(points[modelHeader.indexOf("totalValue")]));
				h.setNpv(Double.parseDouble(points[modelHeader.indexOf("npv")]));
				h.setAreaType(points[modelHeader.indexOf("areaType")]);
				households.add(h);
				ct[0] = ct[0] + 1;
			});
			
			hhSize = ct[0];
			System.out.println("Population size = " + hhSize);
		} catch (IOException e) {
			logUtils.errorPrinter(LocalTime.now(), "Input csv file has a problem.");
			e.printStackTrace();
		}
	}

	
	private void readM1SVRHouseholdRecords(List<String> modelHeader, String csvFile) { 
		try {
			// read hid mapper file for svr
			Map<Long,Long> hidMapper = new HashMap<Long, Long>();
			Files.readAllLines(Paths.get(config.getHidMapperFile())).stream().filter(row -> !row.startsWith("map")).forEach(row -> {
				String[] pts = row.split(",");
				hidMapper.put(Long.parseLong(pts[1]), Long.parseLong(pts[0]));
			});
			long[] ct= new long[1];
			ct[0] = 0;
			Files.readAllLines(Paths.get(csvFile)).stream().filter(row -> !row.startsWith("hid")).forEach( row -> {
				String[] points = row.split(",");
				long hid_mapping_key= hidMapper.get(Long.parseLong(points[modelHeader.indexOf("hid")]));
				Household h = new Household(hid_mapping_key);
				h.setAcreage((int)Double.parseDouble(points[modelHeader.indexOf("acreage")]));
				h.setAsrYear((int)Double.parseDouble(points[modelHeader.indexOf("asrYear")]));
				h.setBaths((int)Double.parseDouble(points[modelHeader.indexOf("baths")]));
				h.setBedrooms((int)Double.parseDouble(points[modelHeader.indexOf("bedrooms")]));
				h.setClimate(points[modelHeader.indexOf("climate")]);
				h.setDailyEnergyConsumption(Double.parseDouble(points[modelHeader.indexOf("dailyEnergyConsumption")]));
				h.setEducationLevel(Integer.parseInt(points[modelHeader.indexOf("educationLevel")]));
				h.setHeatingFuel(Integer.parseInt(points[modelHeader.indexOf("heatingFuel")]));
				h.setHhSize(Integer.parseInt(points[modelHeader.indexOf("hhSize")]));
				h.setHouseType(Integer.parseInt(points[modelHeader.indexOf("houseType")]));
				h.setIncome(Integer.parseInt(points[modelHeader.indexOf("income")]));
				h.setNumCarStorage(Integer.parseInt(points[modelHeader.indexOf("numCarStorage")]));
				h.setPool(Integer.parseInt(points[modelHeader.indexOf("pool")]));
				h.setSqFootage(Double.parseDouble(points[modelHeader.indexOf("sqFootage")]));
				h.setTotalValue(Double.parseDouble(points[modelHeader.indexOf("totalValue")]));
				h.setNpv(Double.parseDouble(points[modelHeader.indexOf("npv")]));
				h.setAreaType(points[modelHeader.indexOf("areaType")]);
				households.add(h);
				ct[0] = ct[0] + 1;
			});
			hhSize = ct[0];
			System.out.println("Population size = " + hhSize);
		} catch (IOException e) {
			logUtils.errorPrinter(LocalTime.now(), "Input csv file has a problem.");
			e.printStackTrace();
		}
	}

	
	private List<String> get_M1_Header() {
		//hid,acreage,bedrooms,asrYear,totalValue,longitude,latitude,numCarStorage,income,pool,sqFootage,climate,houseType,
		//heatingFuel,educationLevel,hhSize,areaType,baths,dailyEnergyConsumption,npv
		//List<String> header = new ArrayList<String>();
		/*header.add("hid");
		header.add("acreage");
		header.add("bedrooms");
		header.add("asrYear");
		header.add("totalValue");
		header.add("longitude");
		header.add("latitude");
		header.add("numCarStorage");
		header.add("income");
		header.add("pool");
		header.add("sqFootage");
		header.add("climate");
		header.add("houseType");
		header.add("heatingFuel");
		header.add("educationLevel");
		header.add("hhSize");
		header.add("areaType");
		header.add("baths");
		header.add("dailyEnergyConsumption");
		header.add("npv");*/
		List<String> header = null;
		try {
		String fname = config.getInputCSV();
		BufferedReader br = new BufferedReader(new FileReader(fname));
	    header = Arrays.asList(br.readLine().split(","));
	    br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return header;
	}

	
	
	

	/**
	 * This function sets all the simulation parameters. Any property/field added in the input file, 
	 * should also be read and added to the ConfigObject or ModelObject as needed.
	 * The ConfigObject will hold all the generic properties and ModelObject will hold all properties of the model.
	 * More importantly, ModelObject is the object read by all replicate runs.
	 * @param propertyFilePath
	 * @return
	 */
	private ConfigObject setSimulationProperties(String propertyFilePath) {
		try {
			Properties property = new Properties();
			property.load(new FileInputStream(new File(propertyFilePath)));
			String baseDir = property.getProperty("baseDir");
			System.out.println(baseDir);
			config.setRegion(property.getProperty(ConfigObject.REGION));
			
			ModelObject mo = setModelProperties(property.getProperty(ModelObject.MODEL_VERSION), 
					baseDir+(property.getProperty(ModelObject.MODEL_PROPERTY_PATH).replaceAll("<region>", config.getRegion())));
			config.setModelCoefficients(mo);
			config.setNetworkDBPath(property.getProperty(ConfigObject.NETWORK_DB_PATH).replaceAll("<region>", config.getRegion()).replace("<baseDir>", baseDir));
			logUtils.logPrinter(LocalTime.now(), "Database network path: "+config.getNetworkDBPath());
			config.setReplicates(Integer.parseInt(property.getProperty(ConfigObject.REPLICATES)));
			config.setTicks(Integer.parseInt(property.getProperty(ConfigObject.TICKS)));
			if(property.getProperty(ConfigObject.IS_INCENTIVE_APPLIED).equals("true")) {
				config.setIncentiveApplied(true);
				logUtils.warningPrinter(LocalTime.now(), "Incentive will be applied");
			}
			else
				config.setIncentiveApplied(false);
			config.setTotalBudget(Integer.parseInt(property.getProperty(ConfigObject.TOTAL_BUDGET)));
			config.setSeedingTicks(processSeedingTicks(property.getProperty(ConfigObject.SEEDING_TICKS)));
			switch (property.getProperty(ConfigObject.SEEDING_STRATEGY)) {
			case ConfigObject.GREEDY_SEEDING_STRATEGY:
				config.setSeedingStrategy(ConfigObject.GREEDY_SEEDING_STRATEGY);
				break;
			case ConfigObject.RANDOM_SEEDING_STRATEGY:
				config.setSeedingStrategy(ConfigObject.RANDOM_SEEDING_STRATEGY);
				break;
			default:
				break;
			}
			
			config.setHidMapperFile(baseDir+(property.getProperty(ConfigObject.HID_MAPPER_FILE)).replaceAll("<region>", config.getRegion()) );
			config.setMile1File(baseDir+property.getProperty(ConfigObject.MILE1_FILE)
					.replace("<region>", config.getRegion())
					);
			config.setMile3File(baseDir+property.getProperty(ConfigObject.MILES3_FILE)
					.replace("<region>", config.getRegion())
					);
			config.setMile4File(baseDir+property.getProperty(ConfigObject.MILES4_FILE)
					.replace("<region>", config.getRegion())
					);
			config.setInputCSV(baseDir+property.getProperty(ConfigObject.INPUT_CSV)
					.replace("<region>", config.getRegion())
					.replace("<modelVersion>", mo.modelVersion)
					);
			config.setOutFile(baseDir+property.getProperty(ConfigObject.OUT_FILE)
					.replace("<region>", config.getRegion())
					.replace("<modelVersion>", mo.modelVersion)
					.replace("<alpha>", String.valueOf(mo.alpha).replace(".","-"))
					);
			config.setAggregatedOutFile(baseDir+property.getProperty(ConfigObject.AGGREGATED_OUT_FILE)
					.replace("<region>", config.getRegion())
					.replace("<modelVersion>", mo.modelVersion)
					.replace("<alpha>", String.valueOf(mo.alpha).replace(".","-"))
					);
			logUtils.logPrinter(LocalTime.now(), "Configuration (input and model) objects populated successfully.");
		} catch (IOException e) {
			logUtils.errorPrinter(LocalTime.now(), "Cannot read input property file.");
			e.printStackTrace();
		}
		
		return config;
	}

	private Set<Integer> processSeedingTicks(String commaSeparatedSeedingTicks) {
		Set<Integer> seedTicks = new HashSet<Integer>();
		for(String s : commaSeparatedSeedingTicks.split(",")) {
			seedTicks.add(Integer.parseInt(s.trim()));
		}
		return seedTicks;
	}



	/**
	 * Read all the coefficients and other values related to the model.
	 * Note: modelVersion needs to be set in the input.properties file. 
	 * According to this entry, the model file will be read from 'modelPropertyPath' present in input.properties
	 * @param modelVersion
	 * @param modelPropertyFilepath
	 * @return
	 */
	private ModelObject setModelProperties(String modelVersion, String modelPropertyFilepath) {
		ModelObject mo = new ModelObject(modelVersion);
		try {
			Properties modelProp = new Properties(); // Property reader for model properties
			modelProp.load(new FileInputStream(new File(modelPropertyFilepath.replace("<modelVersion>", modelVersion))));
			mo.alpha = Double.parseDouble(modelProp.getProperty(ModelObject.ALPHA_VALUE));
			mo.intercept = Double.parseDouble(modelProp.getProperty(ModelObject.INTERCEPT));
			
			mo.incomeCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.INCOME_COEFF));
			mo.hhSizeCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.HHSIZE_COEFF));
			mo.bedroomsCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.BEDROOMS_COEFF));
			mo.acreageCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.ACREAGE_COEFF));
			mo.asrYearCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.ASRYEAR_COEFF));
			mo.bathsCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.BATHS_COEFF));
			mo.poolCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.POOL_COEFF));
			mo.sqFootageCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.SQFTG_COEFF));
			mo.npvCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.NPV_COEFF));
			mo.dailyEnergyConsumptionCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.ENERGY_COEFF));
			mo.heatingFuelCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.HFL_COEFF));
			mo.climateCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.CLIMATE_COEFF));
			mo.educationLevelCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.EDUCATIONlEVEL_COEFF));
			mo.houseTypeCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.HOUSETYPE_COEFF));
			mo.numCarStorageCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.NUM_CAR_STORAGE_COEFF));
			mo.totalValueCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.TOTAL_VALUE_COEFF));
			
			mo.lik1Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.LIK1_COEFF));
			mo.lik2Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.LIK2_COEFF));
			mo.lik3Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.LIK3_COEFF));
			mo.lik4Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.LIK4_COEFF));
			mo.lik5Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.LIK5_COEFF));
			mo.lik6Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.LIK6_COEFF));
			
			mo.mile1Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.MILE1_COEFF));
			mo.mile2Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.MILE2_COEFF));
			mo.mile3Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.MILE3_COEFF));
			mo.mile4Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.MILE4_COEFF));
			
			// TRANSFORMED VARIABLES COEFFICIENTS
			mo.coldVeryCold = Double.parseDouble(modelProp.getProperty(ModelObject.COLD_COEFF));
			mo.mixedHumidCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.MIXEDHUMID_COEFF));
			mo.hotHumidCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.HOTHUMID_COEFF));
			
			mo.eduLevel1Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.EDU1_COEFF));
			mo.eduLevel2Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.EDU2_COEFF));
			mo.eduLevel3Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.EDU3_COEFF));
			mo.eduLevel4Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.EDU4_COEFF));
			mo.eduLevel5Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.EDU5_COEFF));
			
			mo.hfl1Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.HFL1_COEFF));
			mo.hfl2Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.HFL2_COEFF));
			mo.hfl3oeff = Double.parseDouble(modelProp.getProperty(ModelObject.HFL3_COEFF));
			mo.hfl4Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.HFL4_COEFF));
			mo.hfl5Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.HFL5_COEFF));
			mo.hfl6Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.HFL6_COEFF));
			mo.hfl7Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.HFL7_COEFF));
			
			mo.houseType2Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.HOUSETYPE2_COEFF));
			mo.houseType3Coeff = Double.parseDouble(modelProp.getProperty(ModelObject.HOUSETYPE3_COEFF));
			
			mo.urbanAreaCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.URBAN_COEFF));
			
			mo.totalValue75kCoeff = Double.parseDouble(modelProp.getProperty(ModelObject.TOT_VAL_G75k_COEFF));
			
		} catch (IOException e) {
			logUtils.errorPrinter(LocalTime.now(), "Model property error");
			e.printStackTrace();
		}
		return mo;
	}

	/**
	 * Consolidate the output for all replicates
	 */
	private void consolidateOutput() {
		Map<Integer, List<Integer>> out = new HashMap<Integer, List<Integer>>(config.getTicks()+1);
		String inFile = config.getOutFile();
		try {
			//logUtils.warningPrinter(LocalTime.now(), "Sleeping for 45 sec before writing consolidated output file.");
			TimeUnit.SECONDS.sleep(2);
			for(int i=1; i<=config.getReplicates(); i++) {
				String tFile = inFile.replace("##", ""+i);
				tFile = tFile.replaceAll("<modelVersion>", config.getModelCoefficients().modelVersion);
				tFile = tFile.replaceAll("<region>", config.getRegion());
				tFile = tFile.replace("<alpha>", String.valueOf(config.getModelCoefficients().alpha).replace(".","-"));
				Files.readAllLines(Paths.get(tFile)).stream().filter(row -> !row.startsWith("Replicate")).forEach(row -> {
					String[] points = row.split(",");
					int tickNo = Integer.parseInt(points[1]); 
					int count = Integer.parseInt(points[2]);
					if(!out.containsKey(tickNo)) {
						out.put(tickNo, new ArrayList<Integer>(config.getReplicates()+1));
					}
					List<Integer> countList = out.get(tickNo);
					countList.add(count);
					out.put(tickNo, countList);
				});
			}
			String outFile = config.getAggregatedOutFile();
			outFile = outFile.replaceAll("<modelVersion>", config.getModelCoefficients().modelVersion);
			outFile = outFile.replaceAll("<region>", config.getRegion());
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
            String separator = ",";
            String header = "Tick-Replicate"+ separator + "Average" + separator +"%Adopters" + separator +
            		"LowerBound" + separator +  "UpperBound" + separator + "StdDev" + separator + "Min" + separator +  "Max" ;
            for(int k=1; k<=config.getReplicates(); k++) {
            	header = header + separator + k;
            }
            header  = header + "\n";
            bw.write(header);
			for(Integer tick : out.keySet()) {
				List<Integer> clist = out.get(tick);
				int avg = (int) Math.ceil(clist.stream().mapToInt(Integer::intValue).average().getAsDouble());
				float pAdopt = (float) avg/hhSize * 100;
				int min = clist.stream().mapToInt(Integer::intValue).min().getAsInt();
				int max = clist.stream().mapToInt(Integer::intValue).max().getAsInt();	
				int stdDev = calculateStdDev(clist, avg);
				int lowerB = Math.abs(stdDev-avg);
				int upperB = Math.abs(stdDev+avg);
				String val = clist.toString();
				val = val.substring(1, val.length()-1);
				bw.write(tick+separator+avg+separator+pAdopt+separator+lowerB+separator+upperB+separator+stdDev
						+separator+min+separator+max+separator+val+"\n");
			}
			bw.flush();
			bw.close();
			logUtils.logPrinter(LocalTime.now(), "Consolidated output location: " + outFile);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	private int calculateStdDev(List<Integer> clist, int avg) {
		double stdDev = 0;
		for(int i=0; i<clist.size(); i++) {
			stdDev = stdDev + Math.pow((clist.get(i)-avg), 2);
		}
		stdDev = stdDev/clist.size();
		stdDev= Math.sqrt(stdDev);
		return (int) Math.round(stdDev);
	}



}
