package ampliconAnalysis;


import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import support.SamEntry;
import support.SamReader;

public class AmpliconSample {

	File inputSAM;
	
	HashMap<String, int[]> boundaries;
	
	HashMap<String, ReadPair> reads;
	
	HashMap<String, Integer> alleles; //an allele is a series of deletions. I'll encode those in the key string. The values are the number of read pairs supporting it. 
	
	
	
	
	
	
	
	public AmpliconSample(File inputSam)throws IOException {
		this.inputSAM = inputSam;
		
		this.getBoundaries();
		this.recordReads();
		
	}
	
	
	public Vector<String> getInputGenes(){
		Vector<String> v = new Vector<String>();
		
		v.addAll(boundaries.keySet());
		Collections.sort(v);
		return v;
		
	}
	
	
	/**
	 * 
	 * Report patterns of deletions that are supported by reads.
	 * 
	 * 
	 * @param percentageThreshold
	 * @throws IOException
	 */
	public String findAlleles(String inputGene, int percentageThreshold)throws IOException{
		this.alleles =new HashMap<String, Integer>();
		System.out.println(inputGene);
		int numTotalReads = 0;
		int numUsableReads = 0;
		int numReadsSupportingPatternsBelowThreshold=0;
		
		
		for(Iterator<String> iterator = reads.keySet().iterator(); iterator.hasNext();) {
			String name = iterator.next();
			ReadPair pair = reads.get(name);
			numTotalReads++;
			if(!pair.isGood(inputGene, this.boundaries.get(inputGene)[0], this.boundaries.get(inputGene)[1])) {
				continue;
			}
			numUsableReads++;
			String alleleString = pair.getAlleleString();
			
			int num = 0;
			if(alleles.containsKey(alleleString)) {
				num = alleles.get(alleleString);
				
			}
			
			num++;
			alleles.put(alleleString, num);
		}
		
		System.out.println("\tTotal read pairs: " + numTotalReads);
		System.out.println("\tUsable Read Pairs: " + numUsableReads);
		
		String wt ="0";
		
		for(Iterator<String> iterator = alleles.keySet().iterator(); iterator.hasNext();) {
			String alleleString =iterator.next();
			int num = alleles.get(alleleString);
			if( num >  numUsableReads/100  ) {
				System.out.println("\t"+alleleString + "\t" + num + "\t" + (Math.round(num*10000.0/numUsableReads)/100.0) + "%");
			}else {
				numReadsSupportingPatternsBelowThreshold = numReadsSupportingPatternsBelowThreshold + num;
			}
			if( alleleString.equalsIgnoreCase("wildType")) {
				wt = (Math.round(num*10000.0/numUsableReads)/100.0)+"";
			}
		}
		System.out.println( "\tOthers below " + percentageThreshold + "% of reads: "+numReadsSupportingPatternsBelowThreshold +"\t" +(Math.round(numReadsSupportingPatternsBelowThreshold * 10000.0/numUsableReads)/100.0) +"%");
		
		System.out.println("\tWildtype report: " + wt);
		
		return wt;
		
	}
	
	/**
	 * 
	 *  record all read pairs; Only start positions and CIGARs is necessary here. 
	 *  This fills the reads HashMap
	 * 
	 * @param inputSam
	 * @throws IOException
	 */
	public void recordReads()throws IOException{
		reads = new HashMap<String, ReadPair>(); 
		
		SamReader reader = new SamReader(this.inputSAM);
		for(SamEntry entry = reader.readEntry(); entry != null; entry = reader.readEntry()) {
			String name  = entry.getQNAME();
			if(!reads.containsKey(name)) {
				reads.put(name, new ReadPair(entry.getRNAME()));
			}
			if(!entry.queryStrandIsReverse()) { //forward mapping
				reads.get(name).setForwardStart(entry.getPOS());
				reads.get(name).setForwardCigar(entry.getCIGAR());
			}else {
				reads.get(name).setReverseStart(this.getReversePos(entry.getPOS(), entry.getCIGAR()));
				reads.get(name).setReversePos(entry.getPOS());
				reads.get(name).setReverseCigar(entry.getCIGAR());
			}
			
		}
		reader.close();
	}
	
	
	
	
	
	/**
	 * 
	 * find start and end. Look for the most common start position of forward reads and the most common end for reverse reads.
	 * 
	 * @param inputSam
	 * @throws IOException
	 */
	public void getBoundaries()throws IOException{
		
		this.boundaries = new HashMap<String, int[]>();
		
		
		HashMap<String, HashMap<Integer, Integer>> starts = new HashMap<String, HashMap<Integer, Integer>>(); //<sample <position, num>>
		HashMap<String, HashMap<Integer, Integer>> ends = new HashMap<String, HashMap<Integer, Integer>>();
		
		
		int numF = 0; //just qc number of forward reads in sample after removing hardclipped reads
		int numR = 0; //just qc number of reverse reads in sample after removing hardclipped reads
		
		SamReader reader = new SamReader(this.inputSAM);
		for(SamEntry entry = reader.readEntry(); entry != null; entry = reader.readEntry()) {
			
			if(entry.queryUnmapped()) {
				continue;
			}
			
			String cigar = entry.getCIGAR();
			if(cigar.contains("H") ) {
				continue;
			}
			
			String inputGene = entry.getRNAME();
			
			if(!starts.containsKey(inputGene)) {
				starts.put(inputGene, new HashMap<Integer, Integer>());
				ends.put(inputGene, new HashMap<Integer, Integer>());
				
			}
			
			
			if( !entry.queryStrandIsReverse()) {
				numF++;
				int mystart = entry.getPOS();
				
				int num = 0;
				if( starts.get(inputGene).containsKey(mystart)) {
					num = starts.get(inputGene).get(mystart);
				}
				num++;
				starts.get(inputGene).put(mystart, num);
				
			}else {
				numR++;
				
				int myend = this.getReversePos(entry.getPOS(), cigar);
				
				int num = 0;
				if( ends.get(inputGene).containsKey(myend)) {
					num = ends.get(inputGene).get(myend);
				}
				num++;
				ends.get(inputGene).put(myend, num);
			}
			
			
			
			
			
		}
		//System.out.println("Forward reads: " + numF + "\tReverse reads: " + numR);
		reader.close();
		
		for(Iterator<String> iterator1 = starts.keySet().iterator(); iterator1.hasNext();) {
			
			
			String inputGene = iterator1.next();
			int best = 0;
			
			int[] boundary = {-1,-1};
		
			
			for(Iterator<Integer> iterator = starts.get(inputGene).keySet().iterator(); iterator.hasNext();) {
				int start = iterator.next();
				int num = starts.get(inputGene).get(start);
				if( num > best) {
					boundary[0] = start;
					best = num;
					
				}
			//System.out.println(start + "\t" + num);
			}
			
			
			best = 0;
			for(Iterator<Integer> iterator = ends.get(inputGene).keySet().iterator(); iterator.hasNext();) {
				int end = iterator.next();
				int num = ends.get(inputGene).get(end);
				if( num > best) {
					boundary[1] = end;
					best = num;
					
				}
				
			}
			this.boundaries.put(inputGene, boundary);
			System.out.println("Amplicon boundaries for "+inputGene+" are " +boundary[0] + " and " + boundary[1]);
		}
	}
	
	
	/**
	 * accoding to leftmost position and cigar String, figure out where a reverse strand read actually starts.
	 * @param pos 
	 * @param cigar
	 * @return
	 */
	private int getReversePos(int pos, String cigar) {
		int myend = pos;
		Pattern p = Pattern.compile("(\\d+)([MD])");
		Matcher m = p.matcher(cigar);
		while(m.find()) {
			int length = Integer.parseInt(m.group(1));
			
			myend = myend + length;
		}
		return myend; 
	}
}
