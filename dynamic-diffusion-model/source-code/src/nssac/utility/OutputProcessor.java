package nssac.utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputProcessor {

	public static void main(String[] args) {
		OutputProcessor outProcessor = new OutputProcessor();
		outProcessor.combineReplicates();

	}

	private void combineReplicates() {
		// replicate filename: M11-rappahannockOut13.csv
		// probability filename: M11-rappahannockOutProb78.csv -- not processing in this function
		int noReplicates = 100;
		int ticks = 10;
		String modelVersion = "M11";
		String region = "rappahannock";
		String outFile = "D://SEEDS2//DiffusionModel//NotInGitFiles//output-data//<region>//<modelVersion>//<modelVersion>-<region>OutConsolidated.csv";
		outFile = outFile.replaceAll("<modelVersion>", modelVersion);
		outFile = outFile.replaceAll("<region>", region);
		
		String inFile = "D://SEEDS2//DiffusionModel//NotInGitFiles//output-data//<region>//<modelVersion>//<modelVersion>-<region>Out##.csv";
		Map<Integer, List<Integer>> out = new HashMap<Integer, List<Integer>>(ticks+1);
		
		try {
			for(int i=1; i<=noReplicates; i++) {
				String tFile = inFile.replace("##", ""+i);
				tFile = tFile.replaceAll("<modelVersion>", modelVersion);
				tFile = tFile.replaceAll("<region>", region);
				
				Files.readAllLines(Paths.get(tFile)).stream().filter(row -> !row.startsWith("Replicate")).forEach(row -> {
					String[] points = row.split(",");
					int tickNo = Integer.parseInt(points[1]); 
					int count = Integer.parseInt(points[2]);
					if(!out.containsKey(tickNo)) {
						out.put(tickNo, new ArrayList<Integer>(noReplicates+1));
					}
					List<Integer> countList = out.get(tickNo);
					countList.add(count);
					out.put(tickNo, countList);
					
				});
			}
			
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
            String separator = ",";
            String header = "Tick-Replicate";
            for(int k=1; k<=noReplicates; k++) {
            	header = header + separator + k;
            }
            header  = header + separator + "Average" + separator + "Min" + separator +  "Max" + separator +  "StdDev" + "\n";
            bw.write(header);
			for(Integer tick : out.keySet()) {
				List<Integer> clist = out.get(tick);
				int avg = (int) Math.ceil(clist.stream().mapToInt(Integer::intValue).average().getAsDouble());
				int min = clist.stream().mapToInt(Integer::intValue).min().getAsInt();
				int max = clist.stream().mapToInt(Integer::intValue).max().getAsInt();	
				int stdDev = calculateStdDev(clist, avg);
				String val = clist.toString();
				val = val.substring(1, val.length()-1);
				bw.write(tick+separator+val+separator+avg+separator+min+separator+max+separator+stdDev+"\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
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
