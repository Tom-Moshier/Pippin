package pippin;
import java.util.ArrayList;

public class Code {
	class IntTriple {
		private int op;
		private int arg;
		private int indirectionLevel;
		public IntTriple(int op, int arg, int indirectionLevel) {
			this.op = op;
			this.arg = arg;
			this.indirectionLevel = indirectionLevel;
		}
	}
	public static int CODE_MAX = 256;
	private ArrayList<IntTriple> program = new ArrayList<IntTriple>();
	public int getProgramSize() {
		return program.size();
	}
	int getOp(int i) {
		return program.get(i).op;
	}
	int getArg(int i) {
		return program.get(i).arg;
	}
	int getIndirectionLevel(int i) {
		return program.get(i).indirectionLevel;
	}
	void clear() {
		program.clear();
	}
	public void setCode(int op, int arg, int level) {
		program.add(new IntTriple(op,arg,level));
	}
	public String getCodeText(int i) {
		StringBuilder builder = new StringBuilder();
		if(i < program.size()) {
			builder.append(InstructionMap.mnemonics.get(program.get(i).op));
			builder.append(' ');
			for(int j = 0; j < program.get(i).indirectionLevel; j++) {
				builder.append('[');
			}
			builder.append(program.get(i).arg);
		}
		return builder.toString();
	}
}
