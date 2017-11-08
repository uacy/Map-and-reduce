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
/* 
 * Main
 */

public class ReplicatedWorkers {

	public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException {
		
		/* se seteaza datele si se citeste din fisierul de input cea ce trebuie */
		
		int nThreads = Integer.parseInt(args[0]);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[1])), "ASCII"));
		
		int chunk = Integer.parseInt(in.readLine());
		int numberOfFiles = Integer.parseInt(in.readLine());
		
		String []nameOfFiles = new String[numberOfFiles];
		String delim = ";:/?~\\.,><`\\[\\]\\{\\}\\(\\)!@#$%^&-_+\'= *\"\t\n\r";
		
		/* se salveaza numele fiecarui fisier de procesat */
		
		for(int i = 0; i < numberOfFiles; ++i){
			nameOfFiles[i] = in.readLine();
		}
		in.close();
		
		/* se creaza workpool-ul pt map cu nr de threaduri maxime */
		
		WorkPool<Map> wpm = new WorkPool<Map>(nThreads);
		
		/* se punde in workpool toate taskurile care trebuiesc rezolvate */
		
		for(int i = 0; i < numberOfFiles; ++i){
			File file = new File(nameOfFiles[i]);
			long offset = 0;
			long size = file.length();
			BufferedReader fisier = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while(offset < size){
				int chunkk = chunk;
				fisier.skip(chunkk);
				
				/* aici se verifica daca la sfarsit de chunk citit este un cuvant intreg 
				 * (se trateaza cazul cand este mijlocul unui cuvant)
				 * daca este un cuvant intreg trimite chunk cat a fost dat la citire, altfel
				 * cisteste pana da de un delimitator si seteaza ca chunk citit sa fie
				 * chunk de la citire + nr de caractere ca sa fie un cuvant intreg
				 */
				while(delim.indexOf(fisier.read()) == -1 && chunkk + offset < size){
					chunkk++;
				}
				if(chunkk + offset < size) chunkk++;
					
				wpm.putWork(new Map(nameOfFiles[i],offset,chunkk));
				offset += chunkk;
			}
		}
		
		/* se initializeaza variabilele "globale" pt fiecare fisier */
		
		ArrayList<ArrayList<String>> maxWord = new ArrayList<ArrayList<String>>();

		for(int i = 0; i < numberOfFiles; ++i){
			ArrayList<String> aux = new ArrayList<String>();
			maxWord.add(aux);
		}
		
		int []maxLength = new int[numberOfFiles];
		int [][]aparitii = new int[numberOfFiles][50];
		

		/* se creaza workarii si se pun la treaba :) */
		WorkerMap []wm = new WorkerMap[nThreads];
		for(int i = 0; i < nThreads; ++i){
			wm[i] = new WorkerMap(wpm,maxWord,maxLength,aparitii,nameOfFiles);
			wm[i].start();
		}
		
		for(int i = 0; i < nThreads; ++i){
			wm[i].join();
		}
		
		/* se calculeaza sirul lui fibbonaci pana la al 50-lea termen (nu exista cuvinte atat de lungi) */
		
		int []fib = new int[50];
		fib[0] = 0;
		fib[1] = 1;
		
		for(int i = 2; i < 50; ++i){
			fib[i] = fib[i - 1] + fib[i - 2];
		}
		
		float []rang = new float[numberOfFiles];
		long []numarTotalDeCuvinte = new long[numberOfFiles];
		
		/* se calculeaza rankul pt fiecare fisier */
		
		for(int i = 0; i < numberOfFiles; ++i){
			for(int j = 0; j < 50; ++j){
				if(aparitii[i][j] != 0){
					numarTotalDeCuvinte[i] += aparitii[i][j];
					rang[i] += (fib[j+1]*aparitii[i][j]);
				}
			}
			rang[i] = rang[i] / (numarTotalDeCuvinte[i] + 1);
		}

		ArrayList<Rank> rank = new ArrayList<Rank>();
		
		/* rankurile calculate se pun intr-o lista de clasa Rank pt a fi sortate */
		
		for(int i = 0; i < numberOfFiles; ++i){
			rank.add(new Rank(rang[i],i,maxWord.get(i).get(0).length(),maxWord.get(i).size()));
		}
		
		Collections.sort(rank);
		
		/* se scrie in fisier conform cerintei temei */
		
		FileWriter fw = new FileWriter(args[2],true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for(int i = 0; i < numberOfFiles; ++i){
			bw.write(nameOfFiles[rank.get(i).index]+ ";" + Float.toString(rank.get(i).rank).substring(0, Float.toString(rank.get(i).rank).indexOf(".") + 3) + ";["+ rank.get(i).maxLen + "," + rank.get(i).aparitii + "]\n");
		}
		bw.close();
	}
}
