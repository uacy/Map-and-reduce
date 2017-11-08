/**
 * Clasa ce reprezinta o solutie partiala pentru problema de rezolvat. Aceste
 * solutii partiale constituie task-uri care sunt introduse in workpool.
 */
public class Map {
	String filename;
	long offset;
	int chunk;
	
	public Map(String in,long offset,int chunk){
		filename = in;
		this.offset = offset;
		this.chunk = chunk;
	}
}
