package ampliconAnalysis;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class AmpliconAnalysis {

	
	
	public static void main(String[] args) {
		
		try {
			
			
			

			
			File dir = new File(args[0]);
			
			BufferedWriter out = new BufferedWriter(new FileWriter(args[1]));

			
			File[] files = dir.listFiles();
			
		

			
			
			for(int i = 1; i<= 112; i++) {
			
				for( int j = 0; j< files.length; j++) {
					File file = files[j];
					if( file.getName().contains("PID-1703-"+i+"_")&& file.getName().endsWith(".sam")){
						String sample = file.getName().split("\\.")[0]; //PID-1570-S1_S1020.sam
						System.out.println(sample);
						AmpliconSample as = new AmpliconSample(file);
						
						out.write(sample);
						
						HashMap<String, String> result = new HashMap<String, String>();
						
						for(Iterator<String> iterator = as.getInputGenes().iterator(); iterator.hasNext();) {
							String inputGene = iterator.next();
							String s=as.findAlleles(inputGene, 1);
							result.put(inputGene, s);
						}
						
						out.write("\t" + result.get("AHAS") + "\t" + result.get("Cyclops") + "\t" + result.get("SYMRK"));
						out.newLine();
						System.out.println("\n\n\n");
					}
					
				}
			
			}
			
			out.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}
