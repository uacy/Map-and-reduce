import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;


/* Clasa Thread care se ocupa de rezolvarea unui task de tip Map
 * in timp ce citeste cuvintele si le proceseaza le adauga intr-o
 * variabila "globala" pentru a imbina solutile in acelasi pas
 */
public class WorkerMap extends Thread{
	WorkPool<Map> wp;
	ArrayList<ArrayList<String>> maxWord;
	int []maxLength;
	int [][]aparitii;
	String []nameOfFiles;
	
	public WorkerMap(WorkPool<Map> workpool,ArrayList<ArrayList<String>> maxWord, int []maxLength, int[][] aparitii, String[] nameOfFiles){
		this.maxWord = maxWord;
		this.maxLength = maxLength;
		this.aparitii = aparitii;
		this.wp = workpool;
		this.nameOfFiles = nameOfFiles;
	}

	void procesMap(Map mp){
		BufferedReader in;
		char[] buffer;
		StringTokenizer tokenizer;
		String delim = ";:/?~\\.,><`\\[\\]\\{\\}\\(\\)!@#$%^&-_+\'= *\"\t\n\r";
		
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mp.filename)), "ASCII"));			
			
			int index = 0;
			
			/* aflam indexul fisierului salvat in main pt a salva rezultatele in variabila corespunzatoare fisierului*/
			
			for(int i = 0; i < nameOfFiles.length;++i){
				if(nameOfFiles[i].equals(mp.filename)) index = i;
			}
			
			/* se da skip pana la offset si se citeste cat trebuie */
			
			in.skip(mp.offset);
			
			buffer = new char[mp.chunk];
			in.read(buffer,0,mp.chunk);
			
			/* se imparte in cuvinte dupa delimitatori si se proceseaza si updateaza cu noile date */
			
			tokenizer = new StringTokenizer(new String(buffer),delim);
			while(tokenizer.hasMoreTokens()){
				String aux = new String(tokenizer.nextToken());
				int len = aux.length();
				synchronized(aparitii){aparitii[index][len] += 1;}
				if(len < maxLength[index]){
					continue;
				}else if(len > maxLength[index]){
					maxLength[index] = len;
					synchronized(maxWord){
					maxWord.get(index).clear();
					maxWord.get(index).add(aux);
					}
				}else if(len == maxLength[index]){
					synchronized(maxWord){
						if(!maxWord.get(index).contains(aux)){
							maxWord.get(index).add(aux);
						}
					}
				}

			}
		in.close();
		}catch(Exception e){}
	}
	
	/* functia de run... (thredul primeste treaba atata timp cat exista altfel se opreste */
	
	public void run(){
		System.out.println("Thread-ul workerMap " + this.getName() + " a pornit...");
		
		while(true){
			Map mp = wp.getWork();
			if(mp == null) break;
			
			procesMap(mp);
		}
		System.out.println("Thread-ul workerMap " + this.getName() + " s-a terminat...");
	
	}

}
