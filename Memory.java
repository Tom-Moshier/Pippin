package pippin;

public class Memory {
	public static int DATA_SIZE = 512;
	private int[] data = new int[DATA_SIZE];
	private int changedIndex = -1;
	public Memory() {	

	}
	public int getData(int index){
		return data[index];
	}
	public void setData(int index, int value){
		data[index] = value;
	}
	int[] getData(){
		return data;
	}
	public int getChangedIndex(){
		return changedIndex;
	}
	public void clear() {
		for(int i = 0; i<data.length; i++){
			data[i] = 0;
		}
		changedIndex = -1;
	}
}

