package ampliconAnalysis;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadPair {

	String chr;
	int forwardStart;
	int reverseStart; //this is the rightmost position of the read of the reverse strand. This is important for QC. is this the common end?
	
	int reversePos; //this is the leftmost position of the raead on the reverse strand. This is used to find the deletions.
	
	
	String forwardCigar;
	String reverseCigar;
	
	
	
	public ReadPair(String chr) {
		this.chr = chr;
	}


	public boolean isGood(String chr, int start, int end) {
		
		if( !chr.equalsIgnoreCase(this.chr)) {
			return false;
		}
		
		if(start == this.forwardStart && end == this.reverseStart) {
			return true;
		}else {
			return false;
		}
	}
	

	public String getAlleleString() {
		
		
		HashSet<String> deletions = new HashSet<String>();
		int pos = this.forwardStart;
		Pattern p = Pattern.compile("(\\d+)([MD])");
		Matcher m = p.matcher(this.forwardCigar);
		while(m.find()) {
			int length = Integer.parseInt(m.group(1));
			String s = m.group(2);
			if(s.equalsIgnoreCase("D")) {
				String deletion = pos + "-"+ (pos+length) ;
				deletions.add(deletion);
			}
			pos = pos+length;
		}
		
		
		pos = this.reversePos;
		m = p.matcher(this.reverseCigar);
		while(m.find()) {
			int length = Integer.parseInt(m.group(1));
			String s = m.group(2);
			if(s.equalsIgnoreCase("D")) {
				String deletion = pos + "-"+ (pos+length) ;
				if(deletions.contains(deletion)) {
					//System.out.println("found: " + deletion);
				}
				
				deletions.add(deletion);
			}
			pos = pos+length;
		}
		
		Vector<String> v= new Vector<String>();
		v.addAll(deletions);
		
		Collections.sort(v, new Comparator<String>() {
									public int compare(String s, String t) {
										int[] a = {Integer.parseInt(s.split("-")[0]),Integer.parseInt(s.split("-")[1])};
										int[] b = {Integer.parseInt(t.split("-")[0]),Integer.parseInt(t.split("-")[1])};
										
										if(a[0] < b[0]) {return -1;}
										if(a[0] > b[0]) {return 1;}
										if(a[0] == b[0]) {
											if(a[1] <b[1]) {return -1;}
											if(a[1] > b[1]) {return 1;}
											if(a[1] == b[1])return 0;
										}
										return 0;
									}
							}
		);
		
		String alleleString = "";
		
		for(Iterator<String> iterator = v.iterator(); iterator.hasNext();) {
			String a = iterator.next();
			alleleString = alleleString + ";" + a;
		}
		
		if(alleleString.length()>0) {
			alleleString = alleleString.substring(1);
		}else {
			alleleString = "wildType";
		}
		
		return alleleString;
	}


	public int getForwardStart() {
		return forwardStart;
	}

	public void setReversePos(int reversePos) {
		this.reversePos = reversePos;
	}

	public void setForwardStart(int forwardStart) {
		this.forwardStart = forwardStart;
	}



	public int getReverseStart() {
		return reverseStart;
	}



	public void setReverseStart(int reverseStart) {
		this.reverseStart = reverseStart;
	}



	public String getForwardCigar() {
		return forwardCigar;
	}



	public void setForwardCigar(String forwardCigar) {
		this.forwardCigar = forwardCigar;
	}



	public String getReverseCigar() {
		return reverseCigar;
	}



	public void setReverseCigar(String reverseCigar) {
		this.reverseCigar = reverseCigar;
	}
	
	
	
}
