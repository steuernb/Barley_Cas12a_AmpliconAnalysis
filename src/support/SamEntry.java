package support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SamEntry {

	String qName;
	int flag;
	String rName;
	int pos;
	int mapq;
	String cigar;
	String mrnm; //Mate Reference sequence NaMe; "=" if the same as rName;
	int mPos; // 1-based leftmost Mate Position of the clipped sequence
	int iSize; // inferred insert size
	String seq; // query sequence; "=" for a match to the reference;
	String qual; // query Quality; ASCII -33 gives the Phred base quality
	String opt; //optional fields
	
	
	
	
	
	
	
	public SamEntry(String entryString) {
		super();
	
		String[] split = entryString.split("\t",-1);
		try{	
			this.qName = split[0];
			this.flag = Integer.parseInt(split[1]);
			this.rName = split[2];
			this.pos =Integer.parseInt( split[3]);
			this.mapq =Integer.parseInt( split[4]);
			this.cigar = split[5];
			this.mrnm = split[6];
			this.mPos = Integer.parseInt(split[7]);
			this.iSize = Integer.parseInt(split[8]);
			this.seq = split[9];
			this.qual = split[10];
		}catch(NumberFormatException e){
			System.err.println("NumberFormatException for entry string:\t"+entryString+"\n");
			throw new NumberFormatException(e.getMessage());
		}
		if(split.length>11){
			opt = split[11];
			for(int i = 12; i<split.length;i++){
				opt = opt+"\t"+split[i];
			}
			
		}
		
	}


	
	
	/**
	 * get the tab separated complete entry (without a newline at the end)
	 * @return
	 */
	public String getEntry(){
		return qName+"\t"+flag+"\t"+rName+"\t"+pos+"\t"+mapq+"\t"+cigar+"\t"+mrnm+"\t"+mPos+"\t"+iSize+"\t"+seq+"\t"+qual+"\t"+opt;
	}


	
	
	
	/*   
	 * getters for Specification 1.4   
	 */
	
	/**
	 * Query template NAME
	 * @return
	 */
	public String getQNAME(){
		return this.qName;
	}
	
	/**
	 * bitwise FLAG
	 * @return
	 */
	public int getFLAG(){
		return this.flag;
	}
	
	/**
	 * Reference sequence NAME
	 * @return
	 */
	public String getRNAME(){
		return this.rName;
	}
	
	/**
	 * 1-based leftmost mapping POSition
	 * @return
	 */
	public int getPOS(){
		return this.pos;
	}
	
	/**
	 * MAPping Quality
	 * @return
	 */
	public int getMAPQ(){
		return this.mapq;
	}
	
	/**
	 * CIGAR String
	 * @return
	 */
	public String getCIGAR(){
		return this.cigar;
	}
	
	/**
	 * Ref. name of the mate/next segment
	 * @return
	 */
	public String getRNEXT(){
		return this.mrnm;
	}
	
	/**
	 * Position of the mate/next segment
	 * @return
	 */
	public int getPNEXT(){
		return this.mPos;
	}
	
	/**
	 * observed Template LENgth
	 * @return
	 */
	public int getTLEN(){
		return this.iSize;
	}
	
	/**
	 * segment SEQuence
	 * @return
	 */
	public String getSEQ(){
		return this.seq;
	}
	
	/**
	 * ASCII of Phred-scaled base QUALity +33
	 * @return
	 */
	public String getQUAL(){
		return this.qual;
	}
	
	
	private boolean getFlagAtPosition(int position){
		char[] s = Integer.toBinaryString(this.flag).toCharArray();
		if(position>s.length){
			return false;
		}else{
			if(Integer.parseInt( s[s.length - position]+"") != 1){
				return false;
			}else{
				return true;
			}
		}
	}
	
	
	public boolean isReadMappedInProperPair(){
		return getFlagAtPosition(2);
	}
	
	public boolean queryUnmapped(){
		return getFlagAtPosition(3);
	}
	public boolean mateUnmapped(){
		return getFlagAtPosition(4);
	}
	public boolean queryStrandIsReverse(){
		return getFlagAtPosition(5);
	}
	public boolean mateStrandIsReverse(){
		return getFlagAtPosition(6);
	}
	public boolean isFirstInPair(){
		return getFlagAtPosition(7);
	}
	public boolean isSecondInPair(){
		return getFlagAtPosition(8);
	}
	public boolean alignmentNotPrimary(){
		return getFlagAtPosition(9);
	}
	
	
	

	
	/**
	 * adds this read to a coverage array of the reference. The array must have the same length as the mapped reference sequence. This is NOT checked!
	 * @param cov
	 * 			a numeric array representing the coverage of the reference
	 * @return
	 * 			a numeric array representing the coverage of the reference including this read.
	 */
	public int[] addCoverage(int[] cov){
		Pattern p = Pattern.compile("\\d+[SMIDHN]+");
		Matcher m = p.matcher(this.cigar);
		int pos = this.getPOS()-1;
		while(m.find()){
			String s = m.group();
			if(s.endsWith("M") || s.endsWith("D") || s.endsWith("N")){
				cov[pos]++;
				pos++;
			}
		}
		
		
		return cov;
	}
	
	/**
	 * adds this read to a coverage array of the reference. The array must have the same length as the mapped reference sequence. This is NOT checked!
	 * The coverage is determined by mapped reads. mismatches within the read are still regarded as covered.
	 * @param cov
	 * @return
	 */
	public int[] addSimpleCoverage(int[] cov){
		int pos = this.getPOS() -1;
		int readLength = this.getSEQ().length();
		//System.out.println(this.getSEQ() + " " + pos );
		for(int i = pos; i < pos + readLength; i++){
			try{cov[i] ++;}catch (ArrayIndexOutOfBoundsException e) {}
		}
		return cov;
	}
	
	
	/**
	 * if the optional value X0 (number of best hits) is present, it is returned. Otherwise the return is -1
	 * @return
	 * 		Number of best hits (if parameter is present) or -1
	 */
	public int getX0(){
		int num = -1;
		String[] split = opt.split("\t");
		for( int i = 0; i< split.length; i++){
			if( split[i].startsWith("X0:")){
				try{
					return Integer.parseInt(split[i].split(":")[2]);
				}catch(Exception e){}	
			}
		}
		
		
		return num;
		
	}
	
	/**
	 * if the optional value NM (edit distance) is present, it is returned. Otherwiese the return value is -1
	 * @return
	 * 		Edit distance of the read to the reference (if NM is present) or -1
	 */
	public int getNM(){
		int num = -1;
		String[] split = opt.split("\t");
		for( int i = 0; i< split.length; i++){
			if( split[i].startsWith("NM:")){
				try{
					return Integer.parseInt(split[i].split(":")[2]);
				}catch(Exception e){}	
			}
		}
		
		
		return num;
	}
	
	
	/**
	 * if the optional value AS (alignment score) is present, it is returned. Otherwise the return value is -1
	 * @return
	 * 	     Alignment score of the mapping to the reference (if AI is present) or -1
	 */
	public int getAS(){
		int num = -1;
		String[] split = opt.split("\t");
		for( int i = 0; i< split.length; i++){
			if( split[i].startsWith("AS:")){
				try{
					return Integer.parseInt(split[i].split(":")[2]);
				}catch(Exception e){}	
			}
		}
		
		
		return num;
	}
	
	
	
	/**
	 * return the number of matches/mismatches within the CIGAR string. Basically add every number that is before an M.
	 * 
	 * @return
	 * 		number of matches/mismatches in the file.
	 */
	public int getNumMfromCIGAR(){
		String cigar = this.getCIGAR();
		Pattern p = Pattern.compile("(\\d+)M" );
		Matcher m = p.matcher(cigar);
		int numM = 0;
		while(m.find()){
			int i = Integer.parseInt(m.group(1));
			numM = numM + i;
		}
		return numM;
	}
	
	
	public int getNumInDels(){
		String cigar = this.getCIGAR();
		Pattern p = Pattern.compile("(\\d+)[IDN]" );
		Matcher m = p.matcher(cigar);
		int numInDel = 0;
		while(m.find()){
			int i = Integer.parseInt(m.group(1));
			numInDel = numInDel + i;
		}
		return numInDel;
		
	}
	
	public int[] getMappingRange() {
		int[] a = {this.getPOS(), this.getPOS() + this.getSEQ().length()-1};
		
		Pattern p = Pattern.compile("(\\d+)[HS]");
		Matcher m = p.matcher(this.cigar);
		while(m.find()) {
			int skip = Integer.parseInt(m.group(1));
			a[1] = a[1] - skip;
		}
		
		p = Pattern.compile("(\\d+)D");
		m = p.matcher(this.cigar);
		while(m.find()) {
			int add = Integer.parseInt(m.group(1));
			a[1] = a[1] + add;
		}
		
		
		p = Pattern.compile("(\\d+)I");
		m = p.matcher(this.cigar);
		while(m.find()) {
			int skip = Integer.parseInt(m.group(1));
			a[1] = a[1] - skip;
		}
		
		return a;
	}
}
