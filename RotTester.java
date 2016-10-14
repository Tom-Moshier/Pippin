package pippin;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

public class RotTester {

	
	MachineModel machine = new MachineModel();
    int[] dataCopy = new int[Memory.DATA_SIZE];
    
    @Before
    public void setup() {
    	for (int i = 0; i < Memory.DATA_SIZE; i++) {
            dataCopy[i] = 0;
    	}
    	dataCopy[0] = 10;
    	dataCopy[1] = 10;
    	dataCopy[2] = 1;
    	for(int i =10; i<21; i++){
    		dataCopy[i] = i-10;
    	}
    	for(int i = 0; i<Memory.DATA_SIZE; i++){
    		machine.setData(i,dataCopy[i]);
    	}
    }
    @Test
    // Tests the correct rotation
    public void testROTcorrect() {    
    	assertArrayEquals(dataCopy,machine.getData());
    	Instruction instr = machine.get(0x14);
    	instr.execute(0, 1);
    	for(int i = 10; i<20; i++){
    		dataCopy[i] = i-11;
    	}
    	dataCopy[10] = 9;
    	assertArrayEquals(dataCopy,machine.getData());
    }
    @Test (expected=IllegalArgumentException.class)
    // Tests the exception for indirect addressing of ROT
    public void testROTfailure() {
    	assertArrayEquals(dataCopy,machine.getData());
    	Instruction instr = machine.get(0x14);
    	instr.execute(0, 0);
    	for(int i = 10; i<20; i++){
    		dataCopy[i] = i-11;
    	}
    	dataCopy[10] = 9;
    	assertArrayEquals(dataCopy,machine.getData());
    }
}
