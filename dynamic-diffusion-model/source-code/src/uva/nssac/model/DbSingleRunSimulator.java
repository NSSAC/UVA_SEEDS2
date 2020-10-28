package uva.nssac.model;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import nssac.entities.ConfigObject;
import nssac.entities.Household;
import nssac.entities.ModelObject;
import nssac.utility.LogUtility;

public class DbSingleRunSimulator implements Callable<List<String>> {
	private LogUtility logUtils = new LogUtility();
	private ConfigObject config;
	private Map<Long, Household> replicateHouseholds;
	private int currentTick=0, totalAdopters=0, stepBudget=0;
	private int replicateNumber;
	private List<String> runOutput;
	//private List<String> probabilityOutputs;
	String separator = ",";
	
	public DbSingleRunSimulator(int runNo, ConfigObject config, Map<Long, Household> hh) {
		this.config = config;
		this.replicateHouseholds = hh;
		this.replicateNumber = runNo;
		this.runOutput = new ArrayList<String>();
		//this.probabilityOutputs = new ArrayList<String>();
	}

	@Override
	public List<String> call() throws Exception {
		logUtils.logPrinter(LocalTime.now(), "DbSingleRunSimulator() for replicate : "+replicateNumber);
		int t=0; currentTick = 0;
		//probabilityOutputs.add("Replicate" + separator + "Tick"+separator+"HID"+separator+"Probability");
		runOutput.add("Replicate" + separator + "Tick" + separator + "CumulativeAdopters");
		
		Set<Integer> seedTicks = config.getSeedingTicks();
		stepBudget = config.getTotalBudget()/seedTicks.size();
		if(config.isIncentiveApplied() && seedTicks.contains(0)) 
			seedAtTickX();
		
		for(t = 1; t<=config.getTicks(); t++) {
			currentTick = t;
			if(config.isIncentiveApplied() && seedTicks.contains(currentTick))
				seedAtTickX();
			timeTickProcessor();
			runOutput.add(replicateNumber + separator + currentTick + separator + totalAdopters);
			logUtils.logPrinter(LocalTime.now(), replicateNumber + " :: TICK: " + currentTick + " , ADOPTERS: " + totalAdopters);
		}
		
		String outFilename = config.getOutFile().replaceAll("<region>", config.getRegion())
				.replace("<modelVersion>", config.getModelCoefficients().modelVersion);
		Files.write(Paths.get(outFilename.replace("##", ""+replicateNumber)), runOutput);
		//Files.write(Paths.get(outFilename.replace("##", "Prob"+replicateNumber)), probabilityOutputs);
		
		return runOutput;
	}

	private Connection getSqlite3Connection() {
		//logUtils.logPrinter(LocalTime.now(), "Connecting to sqlite3 db");
		try {
			Connection conn = null;
			//"jdbc:sqlite:/project/biocomplexity/seeds2/diffusion-model-v2/db/mileNetwork.db"
			conn = DriverManager.getConnection(config.getNetworkDBPath());
		    return conn;
		} catch (SQLException e) {
			logUtils.errorPrinter(LocalTime.now(), "COULD NOT CONNECT TO SQLITE");
		    e.printStackTrace();
			return null;
		}
	}

	private void seedAtTickX() {
		if(config.isIncentiveApplied()) {
			switch (config.getSeedingStrategy()) {
			case ConfigObject.GREEDY_SEEDING_STRATEGY:
				break;
			case ConfigObject.RANDOM_SEEDING_STRATEGY:
				randomSeeding();
				break;
			default:
				logUtils.errorPrinter(LocalTime.now(), "Incentive seeding strategy does not exist");
				break;
			}
		}
	}
	

	private void randomSeeding() {
		logUtils.logPrinter(LocalTime.now(), "Random seeding applied at tick " + currentTick);
		List<Long> hhKeySet = new ArrayList<Long>(replicateHouseholds.keySet());
		int c = 0;
		while(c < stepBudget) {
			Long hid = hhKeySet.get(getRandomIntegerInRange(1, 0, hhKeySet.size()));
			Household h = replicateHouseholds.get(hid);
			if(h.isAdopter() == false) { 
				h.setAdopter(true);
				h.setAdoptionTick(currentTick);
				h.setIncentiveFlag(true);
				totalAdopters++; // treated as adopters
				replicateHouseholds.put(hid, h);
				c++; // if that household is already an adopter by tick t, then seed another house
			} 	
		}
	}
	
	private Integer getRandomIntegerInRange(int length, int minInclusive, int maxExclusive) {
		Random r = new Random();
		return r.ints(minInclusive, (maxExclusive)).limit(1).findFirst().getAsInt();
		//r.ints(length, minInclusive, maxExclusive).;
		}

	

	/**
	 * This is the unitary function of the simulation.
	 * At each time tick/step what needs to be done, is added to this function. Let current time tick be t.
	 * We process adopters and then non-adopters sequentially.
	 * If household became adopter at t-1, then update diffusion params.
	 * If household is non-adopter, find its adoption probability and mark it as adopter or non-adopter.
	 */
	private void timeTickProcessor() {
		//logUtils.logPrinter(LocalTime.now(), "timeTickProcessor() :Processing adopters");
		Iterator<Long> adopterIterator = replicateHouseholds.keySet().iterator();
		while(adopterIterator.hasNext()) {
			Household h = replicateHouseholds.get(adopterIterator.next());
			if(h.isAdopter() && (h.getAdoptionTick() == (currentTick-1))) { // only adopter in previous tick will cause diffusion
				updatePeerEffects(h);
			} 
		}
		//logUtils.logPrinter(LocalTime.now(), "timeTickProcessor() :Processing non-adopters");
		Iterator<Long> nonAdopterIterator = replicateHouseholds.keySet().iterator();
		while(nonAdopterIterator.hasNext()) {
			Household h = replicateHouseholds.get(nonAdopterIterator.next());
			if(h.isAdopter() == false) { 
				processNonAdopter(h);
			} 
		}
	}
	
	private void processNonAdopter(Household h) {
		double adoptionProbability = getModelProbability(h);
		h.setAdoptionProbability(adoptionProbability);
		if(get01Random() < adoptionProbability) {
			h.setAdopter(true);
			h.setAdoptionTick(currentTick);
			totalAdopters++;
		}
	}

	/**
	 * Add all types of peer effects that should be updated for a household when causing diffusion
	 * @param h
	 */
	private void updatePeerEffects(Household h) {
		updateXMileNeighbors(h); // diffusion type 1
	}

	

	/**
	 * update weight for each hh that lies within specified mile radius of the source household
	 * @param sourceHousehold
	 */
	private void updateXMileNeighbors(Household sourceHousehold) {
		ModelObject mo = config.getModelCoefficients();
		Iterator<Long> hhIterator = replicateHouseholds.keySet().iterator();
		
		if(mo.mile1Coeff!=0D) {
			Set<Long> hhNeighborList = mileQuery(1, sourceHousehold.getHid());
			while(hhIterator.hasNext()) {
				Long currentHID = hhIterator.next();
				if(hhNeighborList.contains(currentHID)) {
					Household h = replicateHouseholds.get(currentHID);
					h.setMile1(h.getMile1()+1);
					replicateHouseholds.put(h.getHid(), h);
				}
			}
		}
		/*if(mo.mile2Coeff!=0D) {
			Set<Long> hhNeighborList = mileQuery(2, sourceHousehold.getHid());
			while(hhIterator.hasNext()) {
				Long currentHID = hhIterator.next();
				if(hhNeighborList.contains(currentHID)) {
					Household h = replicateHouseholds.get(currentHID);
					h.setMile2(h.getMile2()+1);
					replicateHouseholds.put(h.getHid(), h);
				}
			}
		}*/
		if(mo.mile3Coeff!=0D) {
			Set<Long> hhNeighborList = mileQuery(3, sourceHousehold.getHid());
			while(hhIterator.hasNext()) {
				Long currentHID = hhIterator.next();
				if(hhNeighborList.contains(currentHID)) {
					Household h = replicateHouseholds.get(currentHID);
					h.setMile3(h.getMile3()+1);
					replicateHouseholds.put(h.getHid(), h);
				}
			}
		}
		if(mo.mile4Coeff!=0D) {
			Set<Long> hhNeighborList = mileQuery(4, sourceHousehold.getHid());
			while(hhIterator.hasNext()) {
				Long currentHID = hhIterator.next();
				if(hhNeighborList.contains(currentHID)) {
					Household h = replicateHouseholds.get(currentHID);
					h.setMile4(h.getMile4()+1);
					replicateHouseholds.put(h.getHid(), h);
				}
			}
		}
	}
	
	private Set<Long> mileQuery(int mileNo, long sourceHID) {
		//logUtils.debugPrinter(LocalTime.now(), "Finding neihgbors for "+ sourceHID);
		Set<Long> neighbors = new HashSet<Long>();
		String tablename = "mile"+mileNo+"_svr"; // TODO: add region and make it configurable
		String sql = "SELECT hid, neighbors FROM "+ tablename +" WHERE hid = ?";

		try {
		Connection sqliteConnection = getSqlite3Connection();
		PreparedStatement pstmt  = sqliteConnection.prepareStatement(sql);
		pstmt.setString(1,String.valueOf(sourceHID));
        ResultSet rs  = pstmt.executeQuery();
        while (rs.next()) {
        	//System.out.println(rs.getString("hid"));
        	String[] n = (new String(rs.getBytes("neighbors"))).split(",");
        	for(String i : n) {
        		neighbors.add(Long.parseLong(i.trim()));
        	}
        }
        sqliteConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//logUtils.debugPrinter(LocalTime.now(), "DONE finding neihgbors for "+ sourceHID);
		//System.out.println(neighbors.toString());
		return neighbors;
	}

	private static double get01Random(){
		double n = 0;
		n = Math.random();
		//System.out.println(n);
	    return n;
	}
	
	private double getModelProbability(Household h) {
		double prob = 0;
		ModelObject model = config.getModelCoefficients();
		switch (model.modelVersion) {
		case ModelObject.M1:
			prob = model.M1(h);
			//probabilityOutputs.add(replicateNumber + separator + currentTick +separator+h.getHid()+separator+prob);
			prob = prob * model.alpha; 
			break;
		default:
			logUtils.errorPrinter(LocalTime.now(), "The model version does not exist.");
			break;
		}
		return prob;
	}

	


}
