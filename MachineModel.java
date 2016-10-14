package pippin;
import java.util.Observable;
import java.util.TreeMap;
import java.util.Map;

public class MachineModel extends Observable{
	class Registers{
		private int accumulator;
		private int programCounter;
	}
	public final Map<Integer, Instruction> INSTRUCTION_MAP = new TreeMap<>();
	private Registers cpu = new Registers();
	private Memory memory = new Memory();
	private boolean withGUI = false;
	private boolean running = false;
	private Code code;
	

	public MachineModel() {
		this(false);
	}
	
	public MachineModel(boolean withGUI) {
		
		this.withGUI = withGUI;
		// ADD
		INSTRUCTION_MAP.put(0x3, (arg, level) -> {
			if(level<0 || level>2) {throw new IllegalArgumentException("ADD level must be 0-2");}
			if(level != 0) {INSTRUCTION_MAP.get(0x3).execute(memory.getData(arg), level-1);} 
			else {
				cpu.accumulator += arg;
				cpu.programCounter += 1; 
			}
		});
		// NOP
		INSTRUCTION_MAP.put(0x0, (arg, level) -> {
			if(level != 0){throw new IllegalArgumentException("NOP cannot be above 0");}
			else{cpu.programCounter += 1;}
		});
		// LOD
		INSTRUCTION_MAP.put(0x1, (arg,level) -> {
			if(level<0 || level>2) {throw new IllegalArgumentException("LOD level must be 0-2");}
			if(level != 0) {INSTRUCTION_MAP.get(0x1).execute(memory.getData(arg),level-1);} 
			else {
				cpu.accumulator = arg;
				cpu.programCounter += 1;
			}
		});
		// STO
		INSTRUCTION_MAP.put(0x2, (arg,level) -> {
			if(level < 1 || level > 2){	throw new IllegalArgumentException("STO level must be 1 or 2");}
			if(level == 1){
				memory.setData(arg,cpu.accumulator);
				cpu.programCounter += 1;
			}
			else {INSTRUCTION_MAP.get(0x2).execute(memory.getData(arg),level-1);}
		});
		// SUB
		INSTRUCTION_MAP.put(0x4, (arg,level) -> {
			if(level<0 || level>2) {throw new IllegalArgumentException("SUB level must be 0-2");}
			if(level != 0) {INSTRUCTION_MAP.get(0x4).execute(memory.getData(arg),level-1);}
			else {
				cpu.accumulator -= arg;
				cpu.programCounter += 1;
			}
		});
		// MUL
		INSTRUCTION_MAP.put(0x5, (arg,level) ->{
			if(level<0 || level>2) {throw new IllegalArgumentException("MUL level must be 0-2");}
			if(level != 0) {INSTRUCTION_MAP.get(0x5).execute(memory.getData(arg),level-1);}
			else {
				cpu.accumulator *= arg;
				cpu.programCounter += 1;
			}
		});
		// DIV
		INSTRUCTION_MAP.put(0x6, (arg, level) -> {
			if(level <0 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in DIV instruction");
			}
			if(level > 0) {
				INSTRUCTION_MAP.get(0x6).execute(memory.getData(arg), level-1);
			}
			else{
				if( arg == 0){
					throw new DivideByZeroException("Division by Zero");
				}
				else{
				cpu.accumulator /= arg;
				cpu.programCounter ++;
				}
			}
		});
		// AND
		INSTRUCTION_MAP.put(0x7, (arg,level) ->{
			if(level < 0 || level > 1){throw new IllegalArgumentException("AND level must be 0-1");}
			if(level == 1){INSTRUCTION_MAP.get(0x7).execute(memory.getData(arg),level-1);}
			else{
				if(arg != 0 && cpu.accumulator != 0){cpu.accumulator = 1;}
				else{cpu.accumulator = 0;}
				cpu.programCounter += 1;
			}
		});
		// JUMP
		INSTRUCTION_MAP.put(0xB, (arg,level) ->{
			if(level<0 || level>1){throw new IllegalArgumentException("JUMP level must be 0-1");}
			if(level == 1){INSTRUCTION_MAP.get(0xB).execute(memory.getData(arg),level-1);}
			else{
				cpu.programCounter = arg;
			}
		});
		// JMPZ
		INSTRUCTION_MAP.put(0xC, (arg,level) ->{
			if(level<0 || level >1){throw new IllegalArgumentException("JMPZ level must be 0-1");}
			if(level == 1){INSTRUCTION_MAP.get(0xC).execute(memory.getData(arg),level-1);}
			else{
				if(cpu.accumulator == 0){
					cpu.programCounter = arg;
				}
				else{cpu.programCounter += 1;}
			}
		});
		// NOT
		INSTRUCTION_MAP.put(0x8, (arg,level) ->{
			if(level != 0){throw new IllegalArgumentException("NOT level must be 0");}
			else{
				if(cpu.accumulator == 0){
					cpu.accumulator = 1;
				}
				else {
					cpu.accumulator = 0;
				}
				cpu.programCounter += 1;
			}
		});
		// CMPZ
		INSTRUCTION_MAP.put(0x9, (arg,level) ->{
			if(level != 1){throw new IllegalArgumentException("CMPZ level must be 1");}
			else {
				if(memory.getData(arg) == 0){
					cpu.accumulator = 1;
				}
				else {
					cpu.accumulator = 0;
				}
				cpu.programCounter += 1;
			}
		});
		// CMPL
		INSTRUCTION_MAP.put(0xA, (arg,level) ->{
			if(level != 1){throw new IllegalArgumentException("CMPL level must be 1");}
			else {
				if(memory.getData(arg)<0){cpu.accumulator = 1;}
				else {cpu.accumulator =0;}
				cpu.programCounter += 1;
			}
		});
		// HALT
		INSTRUCTION_MAP.put(0xF, (arg,level) -> {
			halt();
		});
		/**
		 * An instruction which rotates the memory elements which are in the
		 * range start...start+length -1 by the amount move. The instruction
		 * will only work if there is no exception thrown. 
		 * 
		 * <table BORDER CELLPADDING=3 CELLSPACING=1>
		 * <caption>Exception Errors</caption>
		 *  <tr>
		 *    <td ALIGN=CENTER> <b>{@code Queue} Reason</b></td>
		 *    <td ALIGN=CENTER> <b>Error</b></td>
		 *  </tr>
		 *  <tr>
		 *    <td>{level != 1}</td>
		 *    <td>{Illegal Argument Exception}</td>
		 *  </tr>
		 *  <tr>
		 *    <td>{start<0}</td>
		 *    <td>{IllegalArgumentException}</td>
		 *  </tr>
		 *  <tr>
		 *    <td>{length<0}</td>
		 *    <td>{IllegalArgumentException}</td>
		 *  </tr>
		 *  <tr>
		 *    <td>{start+length-1>= Memory.DATA_SIZE}</td>
		 *    <td>{IllegalArgumentException}</td>
		 *  </tr>
		 *  <tr>
		 *    <td>{start<= arg + 2 && start + length -1 <= arg}</td>
		 *    <td>{IllegalArgumentException}</td>
		 *  </tr>
		 * </table>
		 * 
		 * Example showing the move of -3
		 * <br>
		<table style="text-align: left; width: 30%;" border="0" cellpadding="10"
		cellspacing="2">
		<tbody>
		<tr>
		<td>
		<table style="text-align: left; width: 10%;" border="1"
		cellpadding="2" cellspacing="2">
		<tbody>
		<tr>
		<td style="vertical-align: top;">0<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">1<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">2<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">3<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">4<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">5<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">12<br>
		</td>
		<td style="vertical-align: top;">207<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">13<br>
		</td>
		<td style="vertical-align: top;">58<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">14<br>
		</td>
		<td style="vertical-align: top;">-3<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">207<br>
		</td>
		<td style="vertical-align: top;">121<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">208<br>
		</td>
		<td style="vertical-align: top;">112<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">209<br>
		</td>
		<td style="vertical-align: top;">170<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">210<br>
		</td>
		<td style="vertical-align: top;">719<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">211<br>
		</td>
		<td style="vertical-align: top;">1254<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">212<br>
		</td>
		<td style="vertical-align: top;">6092<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">260<br>
		</td>
		<td style="vertical-align: top;">33<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">261<br>
		</td>
		<td style="vertical-align: top;">96<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">262<br>
		</td>
		<td style="vertical-align: top;">900<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">263<br>
		</td>
		<td style="vertical-align: top;">203<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">264<br>
		</td>
		<td style="vertical-align: top;">2<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		</tbody>
		</table>
		</td>
		<td>
		<table style="text-align: left; width: 10%;" border="1"
		cellpadding="2" cellspacing="2">
		<tbody>
		<tr>
		<td style="vertical-align: top;">0<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">1<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">2<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">3<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">4<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">5<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">12<br>
		</td>
		<td style="vertical-align: top;">207<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">13<br>
		</td>
		<td style="vertical-align: top;">58<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">14<br>
		</td>
		<td style="vertical-align: top;">-3<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">207<br>
		</td>
		<td style="vertical-align: top;">719<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">208<br>
		</td>
		<td style="vertical-align: top;">1254<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">209<br>
		</td>
		<td style="vertical-align: top;">6092<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">210<br>
		</td>
		<td style="vertical-align: top;">x1<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">211<br>
		</td>
		<td style="vertical-align: top;">x2<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">212<br>
		</td>
		<td style="vertical-align: top;">x3<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">260<br>
		</td>
		<td style="vertical-align: top;">203<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">261<br>
		</td>
		<td style="vertical-align: top;">2<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">262<br>
		</td>
		<td style="vertical-align: top;">121<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">263<br>
		</td>
		<td style="vertical-align: top;">112<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">264<br>
		</td>
		<td style="vertical-align: top;">170<br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		<tr>
		<td style="vertical-align: top;">...<br>
		</td>
		<td style="vertical-align: top;"><br>
		</td>
		</tr>
		</tbody>
		</table>
		</td>
		</tr>
		</tbody>
		</table>
		<br>
		*
		@author tmoshie1
		@author achelli1
		@param arg which is used to get the 3 values start, length, and move 
		@para level which must be 1
		*/
		INSTRUCTION_MAP.put(0x14, (arg,level) -> {
			if(level != 1){throw new IllegalArgumentException("ROT level must be 1");}
			else{
				int start = memory.getData(arg);
				int length = memory.getData(arg+1);
				int move = memory.getData(arg+2);
				if(start<0 || length<0 || start+length-1>= Memory.DATA_SIZE){
					throw new IllegalArgumentException("ROT failure");
				}
				if(start<= arg + 2 && start + length -1 <= arg){
					throw new IllegalArgumentException("ROT failure 2");
				}
				while(move>0){
					cpu.accumulator = getData(start+length-1);
					for(int index = start+length-1; index>start; index--){
						setData(index, getData(index-1));
					}
					setData(start,cpu.accumulator);
					move--;
				}
				while(move<0){
					cpu.accumulator = getData(start);
					for(int index = start+1; index<start+length; index++){
						setData(index-1, getData(index)); 
					}
					setData(start+length-1,cpu.accumulator);
					move++;
				}
			}
		});
	}
	
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public int getData(int index) {
		return memory.getData(index);
	}
	public void setData(int index, int value) {
		memory.setData(index, value);
	}
	public Instruction get(Object key) {
        return INSTRUCTION_MAP.get(key);
    }
	public Code getCode() {
		return code;
	}
	public void step() {
		try {
			int pc = cpu.programCounter;
			int opCode = code.getOp(pc);
			int arg = code.getArg(pc);
			int iL = code.getIndirectionLevel(pc);
			get(opCode).execute(arg, iL);
			}
			catch(Exception e) {
				halt();
				throw e;
			}
	}
	
	public void clear() {
		clearMemory();
		if(code != null) {
			code.clear();
		}
		//Need this in if statement?
		cpu.accumulator = 0;
		cpu.programCounter = 0;
	}

    int[] getData() {
        return memory.getData();
    }

    public int getProgramCounter() {
        return cpu.programCounter;
    }

    public int getAccumulator() {
        return cpu.accumulator;
    }

    public void setAccumulator(int i) {
        cpu.accumulator = i;
    }
    public void setProgramCounter(int i) {
    	cpu.programCounter = i;
    }
    public int getChangedIndex() {
    	return memory.getChangedIndex();
    }
    public void halt () {
    	if(withGUI) {
    		running = false;
    	} else {
    		System.exit(0); 
    	}
    }
    public void clearMemory() {
    	memory.clear();
    }

	public void setCode(Code code) {
		this.code = code;
	}

}
