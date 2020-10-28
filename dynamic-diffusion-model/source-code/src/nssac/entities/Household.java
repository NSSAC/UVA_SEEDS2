package nssac.entities;

public class Household {
	
	private long hid;
	private int hhSize;
	private int income;
	private int bedrooms;
	private int acreage;
	private int asrYear;
	private int baths;
	private int pool;
	private double sqFootage;
	private double npv;
	private double dailyEnergyConsumption;
	private int heatingFuel;
	private String climate;
	private int educationLevel;
	private int houseType;
	private int numCarStorage;
	private double totalValue;
	private String areaType;
	
	private int lik1;
	private int lik2;
	private int lik3;
	private int lik4;
	private int lik5;
	private int lik6;
	
	// ---------------------- updated by model --------------------
	private boolean isAdopter;
	private boolean isSeedAdopter;
	private int adoptionTick;
	private double adoptionProbability;
	
	private boolean incentiveFlag;
	
	private int mile1;
	private int mile2;
	private int mile3;
	private int mile4;
	
	public Household(long hid) {
		this.hid = hid;
		this.hhSize = 0;
		this.income = 0;
		this.bedrooms = 0;
		this.acreage = 0;
		this.asrYear = 0;
		this.baths = 0;
		this.pool = 0;
		this.climate = "";
		this.sqFootage = 0;
		this.npv = 0;
		this.dailyEnergyConsumption = 0;
		this.heatingFuel = 0;
		this.educationLevel = 0;
		this.houseType = 0;
		this.numCarStorage = 0;
		this.totalValue = 0;
		this.areaType = "";
		
		this.lik1 = 0;
		this.lik2 = 0;
		this.lik3 = 0;
		this.lik4 = 0;
		this.lik5 = 0;
		this.lik6 = 0;

		this.mile1 = 0;
		this.mile2 = 0;
		this.mile3 = 0;
		this.mile4 = 0;
		
		this.isAdopter = false;
		this.isSeedAdopter = false;
		this.adoptionTick = -1;
		this.adoptionProbability=0;
	}
	
	public Household(Household toCloneHH) {
		this.hid = toCloneHH.getHid();
		this.hhSize = toCloneHH.getHhSize();
		this.income = toCloneHH.getIncome();
		this.bedrooms = toCloneHH.getBedrooms();
		this.acreage = toCloneHH.getAcreage();
		this.asrYear = toCloneHH.getAsrYear();
		this.baths = toCloneHH.getBaths();
		this.pool = toCloneHH.getPool();
		this.climate = toCloneHH.getClimate();
		this.sqFootage = toCloneHH.getSqFootage();
		this.npv = toCloneHH.getNpv();
		this.dailyEnergyConsumption = toCloneHH.getDailyEnergyConsumption();
		this.heatingFuel = toCloneHH.getHeatingFuel();
		this.educationLevel = toCloneHH.getEducationLevel();
		this.houseType = toCloneHH.getHouseType();
		this.numCarStorage = toCloneHH.getNumCarStorage();
		this.totalValue = toCloneHH.getTotalValue();
		this.areaType = toCloneHH.getAreaType();
		
		this.lik1 = toCloneHH.getLik1();
		this.lik2 = toCloneHH.getLik2();
		this.lik3 = toCloneHH.getLik3();
		this.lik4 = toCloneHH.getLik4();
		this.lik5 = toCloneHH.getLik5();
		this.lik6 = toCloneHH.getLik6();

		this.mile1 = toCloneHH.getMile1();
		this.mile2 = toCloneHH.getMile2();
		this.mile3 = toCloneHH.getMile3();
		this.mile4 = toCloneHH.getMile4();
		
		this.isAdopter = toCloneHH.isAdopter();
		this.isSeedAdopter = toCloneHH.isSeedAdopter();
		this.adoptionTick = toCloneHH.getAdoptionTick();
		this.adoptionProbability= toCloneHH.getAdoptionProbability();
	}

	
	public int getBedrooms() {
		return bedrooms;
	}

	public void setBedrooms(int bedrooms) {
		this.bedrooms = bedrooms;
	}

	public int getAcreage() {
		return acreage;
	}

	public void setAcreage(int acreage) {
		this.acreage = acreage;
	}

	public int getAsrYear() {
		return asrYear;
	}

	public void setAsrYear(int asrYear) {
		this.asrYear = asrYear;
	}

	public int getBaths() {
		return baths;
	}

	public void setBaths(int baths) {
		this.baths = baths;
	}

	public int getPool() {
		return pool;
	}

	public void setPool(int pool) {
		this.pool = pool;
	}

	public double getSqFootage() {
		return sqFootage;
	}

	public void setSqFootage(double sqFootage) {
		this.sqFootage = sqFootage;
	}

	public double getNpv() {
		return npv;
	}

	public void setNpv(double npv) {
		this.npv = npv;
	}

	public double getDailyEnergyConsumption() {
		return dailyEnergyConsumption;
	}

	public void setDailyEnergyConsumption(double dailyEnergyConsumption) {
		this.dailyEnergyConsumption = dailyEnergyConsumption;
	}

	public int getHeatingFuel() {
		return heatingFuel;
	}

	public void setHeatingFuel(int heatingFuel) {
		this.heatingFuel = heatingFuel;
	}

	public String getClimate() {
		return climate;
	}

	public void setClimate(String climate) {
		this.climate = climate;
	}

	public int getEducationLevel() {
		return educationLevel;
	}

	public void setEducationLevel(int educationLevel) {
		this.educationLevel = educationLevel;
	}

	public int getHouseType() {
		return houseType;
	}

	public void setHouseType(int houseType) {
		this.houseType = houseType;
	}

	public int getLik2() {
		return lik2;
	}

	public void setLik2(int lik2) {
		this.lik2 = lik2;
	}

	public int getLik3() {
		return lik3;
	}

	public void setLik3(int lik3) {
		this.lik3 = lik3;
	}

	public int getLik4() {
		return lik4;
	}

	public void setLik4(int lik4) {
		this.lik4 = lik4;
	}

	public int getLik5() {
		return lik5;
	}

	public void setLik5(int lik5) {
		this.lik5 = lik5;
	}

	public int getLik6() {
		return lik6;
	}

	public void setLik6(int lik6) {
		this.lik6 = lik6;
	}

	public long getHid() {
		return hid;
	}

	public void setHid(long hid) {
		this.hid = hid;
	}

	public int getHhSize() {
		return hhSize;
	}

	public void setHhSize(int hhSize) {
		this.hhSize = hhSize;
	}

	public int getIncome() {
		return income;
	}

	public void setIncome(int income) {
		this.income = income;
	}

	public boolean isAdopter() {
		return isAdopter;
	}

	public void setAdopter(boolean isAdopter) {
		this.isAdopter = isAdopter;
	}

	public boolean isSeedAdopter() {
		return isSeedAdopter;
	}

	public void setSeedAdopter(boolean isSeedAdopter) {
		this.isSeedAdopter = isSeedAdopter;
	}

	public int getAdoptionTick() {
		return adoptionTick;
	}

	public void setAdoptionTick(int adoptionTick) {
		this.adoptionTick = adoptionTick;
	}

	public double getAdoptionProbability() {
		return adoptionProbability;
	}

	public void setAdoptionProbability(double adoptionProbability) {
		this.adoptionProbability = adoptionProbability;
	}

	public int getMile1() {
		return mile1;
	}

	public void setMile1(int mile1) {
		this.mile1 = mile1;
	}

	public int getMile2() {
		return mile2;
	}

	public void setMile2(int mile2) {
		this.mile2 = mile2;
	}

	public int getMile4() {
		return mile4;
	}

	public void setMile4(int mile4) {
		this.mile4 = mile4;
	}

	public int getNumCarStorage() {
		return numCarStorage;
	}

	public void setNumCarStorage(int numCarStorage) {
		this.numCarStorage = numCarStorage;
	}

	public double getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}

	public int getLik1() {
		return lik1;
	}

	public void setLik1(int lik1) {
		this.lik1 = lik1;
	}

	public int getMile3() {
		return mile3;
	}

	public void setMile3(int mile3) {
		this.mile3 = mile3;
	}

	public String getAreaType() {
		return areaType;
	}

	public void setAreaType(String areaType) {
		this.areaType = areaType;
	}

	public boolean isIncentiveFlag() {
		return incentiveFlag;
	}

	public void setIncentiveFlag(boolean incentiveFlag) {
		this.incentiveFlag = incentiveFlag;
	}

}
