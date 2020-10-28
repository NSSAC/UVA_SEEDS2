package nssac.entities;


/**
 * This objects holds the model properties. E.g. coefficients, file paths, etc.
 * @author Swapna
 *
 */
public class ModelObject {
	
	public String modelVersion;
	public String modelPropertyPath;
	
	public double alpha;     // value controlling the output probability of the model. DEFAULT = 1
	public double intercept;
	
	public double incomeCoeff;
	public double hhSizeCoeff;
	public double numCarStorageCoeff;
	public double totalValueCoeff;
	public double bedroomsCoeff;
	public double acreageCoeff;
	public double asrYearCoeff;
	public double bathsCoeff;
	public double poolCoeff;
	public double sqFootageCoeff;
	public double npvCoeff;
	public double dailyEnergyConsumptionCoeff;
	public double heatingFuelCoeff;
	public double climateCoeff;
	public double educationLevelCoeff;
	public double houseTypeCoeff;
	public double areaTypeCoeff;
	
	public double lik1Coeff;
	public double lik2Coeff;
	public double lik3Coeff;
	public double lik4Coeff;
	public double lik5Coeff;
	public double lik6Coeff;

	public double mile1Coeff;
	public double mile2Coeff;
	public double mile3Coeff;
	public double mile4Coeff;
	
	
	// Special/additional variables for specific model versions go here
	public double coldVeryCold;
	public double mixedHumidCoeff;
	public double hotHumidCoeff;
	
	public double eduLevel1Coeff;
	public double eduLevel2Coeff;
	public double eduLevel3Coeff;
	public double eduLevel4Coeff;
	public double eduLevel5Coeff;
	
	public double hfl1Coeff;
	public double hfl2Coeff;
	public double hfl3oeff;
	public double hfl4Coeff;
	public double hfl5Coeff;
	public double hfl6Coeff;
	public double hfl7Coeff;
	
	public double houseType2Coeff;
	public double houseType3Coeff;
	
	public double urbanAreaCoeff;
	
	public double totalValue75kCoeff;
	
	
	public ModelObject(String modelVersion) {
		this.modelVersion = modelVersion;
		this.alpha = 1.0;
		
		// set all coefficients to 0 by default
		this.intercept = 0;
		
		this.incomeCoeff = 0;
		this.hhSizeCoeff = 0;
		this.bedroomsCoeff = 0;
		this.acreageCoeff = 0;
		this.asrYearCoeff = 0;
		this.bathsCoeff = 0;
		this.poolCoeff = 0;
		this.sqFootageCoeff = 0;
		this.npvCoeff = 0;
		this.dailyEnergyConsumptionCoeff = 0;
		this.heatingFuelCoeff = 0;
		this.climateCoeff = 0;
		this.educationLevelCoeff = 0;
		this.houseTypeCoeff = 0;
		this.numCarStorageCoeff=0;
		this.totalValueCoeff = 0;
		this.areaTypeCoeff=0;
		
		this.lik1Coeff = 0;
		this.lik2Coeff = 0;
		this.lik3Coeff = 0;
		this.lik4Coeff = 0;
		this.lik5Coeff = 0;
		this.lik6Coeff = 0;
		
		this.mile1Coeff=0;
		this.mile2Coeff=0;
		this.mile3Coeff=0;
		this.mile4Coeff=0;
		
		// Special/additional variables for specific model versions go here
		this.coldVeryCold=0;
		this.mixedHumidCoeff=0;
		this.hotHumidCoeff=0;
		
		this.eduLevel1Coeff=0;
		this.eduLevel2Coeff=0;
		this.eduLevel3Coeff=0;
		this.eduLevel4Coeff=0;
		this.eduLevel5Coeff=0;
		
		this.hfl1Coeff=0;
		this.hfl2Coeff=0;
		this.hfl3oeff=0;
		this.hfl4Coeff=0;
		this.hfl5Coeff=0;
		this.hfl6Coeff=0;
		this.hfl7Coeff=0;
		
		this.houseType2Coeff=0;
		this.houseType3Coeff=0;
		
		this.urbanAreaCoeff=0;
		
		this.totalValue75kCoeff=0;
	}
	
	
	public static final String MODEL_VERSION = "modelVersion";
	public static final String MODEL_PROPERTY_PATH = "modelPropertyPath";

	public static final String M1 = "M1";
	
	public static final String ALPHA_VALUE = "alpha";
	public static final String INTERCEPT = "intercept";

	public static final String INCOME_COEFF = "incomeCoeff";
	public static final String HHSIZE_COEFF = "hhSizeCoeff";
	public static final String NUM_CAR_STORAGE_COEFF = "numCarStorageCoeff";
	public static final String TOTAL_VALUE_COEFF = "totalValueCoeff";
	public static final String BEDROOMS_COEFF = "bedroomsCoeff";
	public static final String ACREAGE_COEFF = "acreageCoeff";
	public static final String ASRYEAR_COEFF = "asrYearCoeff";
	public static final String BATHS_COEFF = "bathsCoeff";
	public static final String POOL_COEFF = "poolCoeff";
	public static final String SQFTG_COEFF = "sqFootageCoeff";
	public static final String NPV_COEFF = "npvCoeff";
	public static final String ENERGY_COEFF = "dailyEnergyConsumptionCoeff";
	public static final String HFL_COEFF = "heatingFuelCoeff";
	public static final String CLIMATE_COEFF = "climateCoeff";
	public static final String EDUCATIONlEVEL_COEFF = "educationLevelCoeff";
	public static final String HOUSETYPE_COEFF = "houseTypeCoeff";
	public static final String AREA_TYPE_COEFF = "areaTypeCoeff";
	
	public static final String LIK1_COEFF = "lik1Coeff";
	public static final String LIK2_COEFF = "lik2Coeff";
	public static final String LIK3_COEFF = "lik3Coeff";
	public static final String LIK4_COEFF = "lik4Coeff";
	public static final String LIK5_COEFF = "lik5Coeff";
	public static final String LIK6_COEFF = "lik6Coeff";
	
	public static final String MILE1_COEFF = "mile1Coeff";
	public static final String MILE2_COEFF = "mile2Coeff";
	public static final String MILE3_COEFF = "mile3Coeff";
	public static final String MILE4_COEFF = "mile4Coeff";
	
	// Special/additional transformed variables for specific model versions go here
	public static final String COLD_COEFF = "coldVeryCold";
	public static final String MIXEDHUMID_COEFF = "mixedHumidCoeff";
	public static final String HOTHUMID_COEFF= "hotHumidCoeff";
	
	public static final String EDU1_COEFF = "eduLevel1Coeff";
	public static final String EDU2_COEFF = "eduLevel2Coeff";
	public static final String EDU3_COEFF = "eduLevel3Coeff";
	public static final String EDU4_COEFF = "eduLevel4Coeff";
	public static final String EDU5_COEFF = "eduLevel5Coeff";
	
	public static final String HFL1_COEFF = "hfl1Coeff";
	public static final String HFL2_COEFF = "hfl2Coeff";
	public static final String HFL3_COEFF = "hfl3oeff";
	public static final String HFL4_COEFF = "hfl4Coeff";
	public static final String HFL5_COEFF = "hfl5Coeff";
	public static final String HFL6_COEFF = "hfl6Coeff";
	public static final String HFL7_COEFF = "hfl7Coeff";
	
	public static final String HOUSETYPE2_COEFF = "houseType2Coeff";
	public static final String HOUSETYPE3_COEFF = "houseType3Coeff";
	
	public static final String URBAN_COEFF = "urbanAreaCoeff";
	
	public static final String TOT_VAL_G75k_COEFF = "totalValue75kCoeff";
	

	/**
	 * Model1 v1.0 is the model as of 9 June 2019. 
	 * Trained on synthetic data (region unknown), this is the decision adjusted syn pop model
	 * @param h
	 * @return
	 */
	public double M1 (Household h) {
		double linearResult=0, prob=0;
		
		linearResult = this.intercept 
				+ (h.getAcreage() * this.acreageCoeff)
				+ (h.getAsrYear() * this.asrYearCoeff)
				+ (h.getBedrooms() * this.bedroomsCoeff)
				+ (h.getDailyEnergyConsumption() * this.dailyEnergyConsumptionCoeff)
				+ (h.getNumCarStorage() * this.numCarStorageCoeff)
				+ (h.getPool() * this.poolCoeff)
				+ (h.getTotalValue() * this.totalValueCoeff)
				+ (h.getHhSize() * this.hhSizeCoeff)
				+ (h.getMile1() * this.mile1Coeff)
				+ (h.getMile3() * this.mile3Coeff)
				+ (h.getMile4() * this.mile4Coeff)
				;
		
		if(h.getAreaType().equals("U")) // 0=rural, 1=urban : taken from M10 definition by Zhihao
			linearResult = linearResult + (1 * this.urbanAreaCoeff);
		
		if(h.getClimate().equals("Cold/Very Cold"))
			linearResult = linearResult + (1 * this.coldVeryCold);
		else if(h.getClimate().equals("Hot-Humid"))
			linearResult = linearResult + (1 * this.hotHumidCoeff);
		
		//x.totalVal=1 if totalVal>75000; x.totalVal=0 if totalVal<=75000.
		if(h.getTotalValue() > 75000)
			linearResult = linearResult + (1 * this.totalValue75kCoeff);
		
		if(h.getEducationLevel() == 2)
			linearResult = linearResult + (1 * this.eduLevel2Coeff);
		else if (h.getEducationLevel() == 3)
			linearResult = linearResult + (1 * this.eduLevel3Coeff);
		
		if(h.getHeatingFuel() == 2)
			linearResult = linearResult + (1 * this.hfl2Coeff);
		else if(h.getHeatingFuel() == 3)
			linearResult = linearResult + (1 * this.hfl3oeff);
		else if(h.getHeatingFuel() == 7)
			linearResult = linearResult + (1 * this.hfl7Coeff);
		
		if(h.getHouseType() == 2)
			linearResult = linearResult + (1 * this.houseType2Coeff);
		else if(h.getHouseType() == 3)
			linearResult = linearResult + (1 * this.houseType3Coeff);

		// Calculate probability. Logistic Regression Equation= (e^X/1+e^X)
		prob=Math.pow(Math.E, linearResult)/(1+Math.pow(Math.E, linearResult));
				
		return prob;
	}

}
