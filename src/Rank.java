/* Aceasta clasa ajuta la sortarea fisierelor dupa rank 
 * pastreaza indexul initial pt retinerea numelui fisierului
 * si se sorteaza dupa rank
 */

public class Rank implements Comparable<Rank>{
	float rank;
	int index;
	int maxLen;
	int aparitii;
	
	public Rank(float rank,int index,int maxLen, int apariti){
		this.rank = rank;
		this.index = index;
		this.maxLen = maxLen;
		this.aparitii = apariti;
	}
	
	public int intRank(){
		return (int)(this.rank * 100);
	}

	@Override
	public int compareTo(Rank arg) {
		int local = intRank();
		int other = arg.intRank();
		
		if(local < other) return 1;
		else if(local > other) return -1;
		else return 0;
	}
}
