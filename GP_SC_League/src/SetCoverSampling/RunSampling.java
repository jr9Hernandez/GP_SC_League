package SetCoverSampling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Random;

public class RunSampling {
	
	static DataRecollection dataRecollection;
	
	
	public RunSampling(int pathLogInt, String pathTableSetCover, String curriculumPortfolio) throws IOException {
		
		
		//Here we collect the data
		String pathLog=String.valueOf(pathLogInt);
		dataRecollection=new DataRecollection(pathLog,pathTableSetCover);
		dataRecollection.runDataRecollection(curriculumPortfolio);
		
		
		//Here we sample
		
//		File[] files = new File("logs/logs_states_"+pathLog).listFiles();
//		preSampling(files);
//		files = new File("logs/logs_states_"+pathLog).listFiles();
//	    sampling(files);
	    
	    //System.out.println("end");
		
		
	}
	

	private static void preSampling(File[] files) throws IOException {	
		for (File file : files) {
			//get 10% of files in each directory
			for(File direc : file.listFiles()){
				if(direc.isDirectory()){
					//sampling 50% of data for parent. 
					copyFilesForParent(50, file, direc);
				}
			}
		}
	}

	/**
	 * Copy the quantity of files estimated by percFiles to folder parent from direcChild.
	 * @param percFiles percentage of files to copy
	 * @param parent
	 * @param direcChild
	 * @throws IOException 
	 */
	private static void copyFilesForParent(double percFiles, File parent, File direcChild) throws IOException {
		Random rand = new Random();
		File[] files = direcChild.listFiles();
		int qtdFiles = files.length;
		double perc = percFiles/100;
		double qtdFilesToCopy = qtdFiles*perc;
		
		for (int i = 0; i < qtdFilesToCopy; i++) {
			File fCopy = files[rand.nextInt(qtdFiles)];
			copyFile(fCopy, new File(parent.getAbsolutePath()+"/"+fCopy.getName()));
		}
		
	}
	
	 public static void copyFile(File source, File destination) throws IOException {
	        if (destination.exists())
	            destination.delete();
	        FileChannel sourceChannel = null;
	        FileChannel destinationChannel = null;
	        try {
	            sourceChannel = new FileInputStream(source).getChannel();
	            destinationChannel = new FileOutputStream(destination).getChannel();
	            sourceChannel.transferTo(0, sourceChannel.size(),
	                    destinationChannel);
	        } finally {
	            if (sourceChannel != null && sourceChannel.isOpen())
	                sourceChannel.close();
	            if (destinationChannel != null && destinationChannel.isOpen())
	                destinationChannel.close();
	       }
	   }


	public static void sampling(File[] files) {
	    for (File file : files) {
	            //System.out.println("Directory: " + file.getName());
	            //sampling(file.listFiles()); // Calls same method again.
	    	
	        	dataRecollection.samplingByFiles(file.getName(), file.listFiles(new FilenameFilter() {
	        	    public boolean accept(File dir, String name) {
	        	        return name.toLowerCase().endsWith(".txt");
	        	    }
	        	}));	        	
	    }
	}

}
