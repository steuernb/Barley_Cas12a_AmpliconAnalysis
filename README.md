# Barley Cas12a Amplicon Analysis


scripts for work in manuscript "__An Optimised CRISPR Cas9 and Cas12a Toolkit for Wheat and Barley__" (in preparation)




## Prerequisites

1. BWA; [github.com/lh3/bwa](https://github.com/lh3/bwa). Here, we used version `0.7.17-r1188`
2. Java Runtime Environments 1.8 or higher [www.java.com](https://www.java.com/en/download/manual.jsp)


## Prepare data

Run bwa. Use [genes.fasta](https://github.com/steuernb/Barley_Cas12a_AmpliconAnalysis/blob/main/data/genes.fasta) as reference.
	
* Create bwa index for reference
	
	```
	bwa index genes.fasta
	```
		
* Map each amplicon sample separately
		
	```
	bwa mem genes.fasta SampleX_R1.fq.gz SampleX_R2.fq.gz > SampleX.sam
	```
		
	_Note: Downstream scripts test for suffix `.sam` to process a mapping file._
	
	
## Run Amplicon analysis.

Compile java code or use precompiled jar file [AmpliconAnalysis.jar](https://github.com/steuernb/Barley_Cas12a_AmpliconAnalysis/blob/main/jar/AmpliconAnalysis.jar)

The precompiled version has two arguments, firstly the folder containing all SAM files, secondly path to the output file.

```
java -jar AmpliconAnalysis.jar path/to/inputSAMfolder/ path/to/outputfile.txt
```