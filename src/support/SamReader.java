package support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

public class SamReader {

	File samFile;
	BufferedReader in;
	
	
	public SamReader(String samFileString) throws IOException{
		super();
		
		this.samFile = new File(samFileString);
		this.in = new BufferedReader(new FileReader(samFile));
	}
	
	
	public SamReader(File samFile) throws IOException{
		super();
		this.samFile = samFile;
		
		
		FileInputStream fis = new FileInputStream(this.samFile);
		byte[] bytes = new byte[2];
		fis.read(bytes);
		int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
		boolean gzip = GZIPInputStream.GZIP_MAGIC == head;
		fis.close();

		
		if(gzip){
			this.in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(this.samFile))));
		}else{
			this.in = new BufferedReader((new FileReader(this.samFile)));
		}
		
		
		
		
		
		
		
		
	}

	public SamEntry readEntry()throws IOException{
		String inputline = in.readLine();
		while( inputline != null && inputline.trim().startsWith("@")){
			inputline = in.readLine();
		}
		
		if(inputline != null ){
			return new SamEntry(inputline);
		}
		
		return null;
	}
	
	
	public void close()throws IOException{
		this.in.close();
	}

	
	public Vector<String> getHeader()throws IOException{
		Vector<String> v = new Vector<String>();
		BufferedReader in = new BufferedReader(new FileReader(samFile));
		String inputline = in.readLine();
		while (inputline != null) {
			if(inputline.startsWith("@")){
				v.add(inputline);
			}else{
				break;
			}
			inputline = in.readLine();
		}
		in.close();
		return v;
	}
	
	

}
