package pippin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Assembler {
	/**
	 * lists the mnemonics of the instructions that do not have arguments
	 */
	 public static Set<String> noArgument = new TreeSet<String>();
	 /**
	 * lists the mnemonics of the instructions that allow immediate addressing
	 */
	 public static Set<String> allowsImmediate = new TreeSet<String>();
	 /**
	 * lists the mnemonics of the instructions that allow indirect addressing
	 */
	 public static Set<String> allowsIndirect = new TreeSet<String>(); 
	 static {
		 noArgument.add("HALT");
		 noArgument.add("NOP");
		 noArgument.add("NOT");
		 allowsImmediate.add("LOD");
		 allowsImmediate.add("ADD");
		 allowsImmediate.add("SUB");
		 allowsImmediate.add("MUL");
		 allowsImmediate.add("DIV");
		 allowsImmediate.add("AND");
		 allowsImmediate.add("JUMP");
		 allowsImmediate.add("JMPZ");
		 allowsImmediate.add("ROT");
		 allowsIndirect.add("LOD");
		 allowsIndirect.add("STO");
		 allowsIndirect.add("ADD");
		 allowsIndirect.add("SUB");
		 allowsIndirect.add("MUL");
		 allowsIndirect.add("DIV");
		 } 
	 /**
	  * Method to assemble a file to its binary representation. If the input has errors
	  * a list of errors will be written to the errors map. If there are errors,
	  * they appear as a map with the line number as the key and the description of the error
	  * as the value. If the input or output cannot be opened, the "line number" key is 0.
	  * @param input the source assembly language file
	  * @param output the binary version of the program if the souce program is
	  * correctly formatted
	  * @param errors the errors map
	  * @return
	  */
	  public static boolean assemble(File input, File output, Map<Integer, String> errors) {
		  ArrayList<String> inputText = new ArrayList<>();
		  ArrayList<String> inCode = new ArrayList<>();
		  ArrayList<String> inData = new ArrayList<>();
		  ArrayList<String> outCode = new ArrayList<>();
		  ArrayList<String> outData = new ArrayList<>();
		  if(errors == null){ throw new IllegalArgumentException("Coding error: the error map is null");}
		  //make an error checking function for the different types and syntax
		  try (Scanner inp = new Scanner(input)) {
			  // while loop reading the lines from input in inputText
			  while(inp.hasNextLine()){
				  inputText.add(inp.nextLine());
			  }
		  } catch (FileNotFoundException e) {
			  errors.put(0, "Error: Unable to open the input file");
		  }
		  int blankLn = 0;
		  for(int i = 0; i<inputText.size(); i++){
			  if(inputText.get(i).trim().length() > 0){
				  if(inputText.get(i).charAt(0) == ' '){
					  //just spaces or tabs too?
					  errors.put(i+1, "Error on line " + (i+1) + ": starts with white space");
				  }
			  }
		  } 
		  int blankLnNum = -10;
		  for(int i = 0; i<inputText.size(); i++){
			  if(inputText.get(i).trim().length() == 0 && blankLnNum <0){
				  blankLnNum = i;
			  }
			  if(blankLnNum>0 && inputText.get(i).trim().length() > 0){
				  errors.put(blankLnNum, "Error on line " + blankLnNum + ": illegal blank line");
				  blankLnNum = -10;
			  }
		  }
		  boolean data = false;
		  for(int i = 0; i<inputText.size(); i++){
			  if(data == false){
				  if(inputText.get(i).trim().equalsIgnoreCase("DATA")){
					  if(inputText.get(i).trim().equals("DATA")){
						  data = true;
					  }
					  else{
						  errors.put(i+1, "Error on line " + (i+1) + "DATA is incorrectly labeled");
					  }
				  }
				  else{
					  inCode.add(inputText.get(i).trim());
				  }
			  }
			  else{
				  inData.add(inputText.get(i).trim());
			  }
		  }
		  for(int i =0; i<inCode.size(); i++){
			  String[] parts = inCode.get(i).split("\\s+");
			  if(!InstructionMap.opcode.containsKey(parts[0].toUpperCase())){
				  errors.put(i+1,"Error on line " + (i+1) + " illegal mnemonic");;
			  }
			  else{
				  if(!InstructionMap.opcode.containsKey(parts[0])){
					  errors.put(i+1,"Error on line " + (i+1) + " mnemonics must be in uppercase");
				  }
				  else {
					  if(noArgument.contains(parts[0]) && parts.length > 1){
						  errors.put(i+1,"Error on line " + (i+1) + "mnemonic does not take arguments");
					  }
					  else if(noArgument.contains(parts[0]) && parts.length == 1){
						  outCode.add(Integer.toHexString(InstructionMap.opcode.get(parts[0])) + " 0 0");
					  }
					  else{
						  if(parts[1].length() == 2 && parts[1] == "[["){
							  if(allowsIndirect.contains(parts[0])){
								 try{
									  int arg = Integer.parseInt(parts[1].substring(2),16);
									  outCode.add(Integer.toHexString(InstructionMap.opcode.get(parts[0])) +
									  Integer.toHexString(arg).toUpperCase() + " 2");
								  } catch(NumberFormatException e) {
									  errors.put(i+1, "Error on line "+(i+1)+ ": indirect argument is not a hex number");
								  } 
							  }
							  else {
								  errors.put(i+1, "Error on line " + (i+1) + "Does not allow indirect addressing");
							  }
						  }
						  if(parts[1].length() == 2 && parts[1].charAt(0) == '['){
							  try{
								  int arg = Integer.parseInt(parts[1].substring(2),16);
								  outCode.add(Integer.toHexString(InstructionMap.opcode.get(parts[0])) +
								  Integer.toHexString(arg).toUpperCase() + " 1");
								 } catch(NumberFormatException e) {
								  errors.put(i+1, "Error on line "+(i+1)+ ": direct argument is not a hex number");
								 }
						  }
						  if(parts[1].length() == 2 && parts[1].charAt(0) != '['){
							  if(allowsImmediate.contains(parts[0])){
								  try{
									  int arg = Integer.parseInt(parts[1].substring(2),16);
									  outCode.add(Integer.toHexString(InstructionMap.opcode.get(parts[0])) +
									  Integer.toHexString(arg).toUpperCase() + " 0");
								  } catch(NumberFormatException e) {
									  errors.put(i+1, "Error on line "+(i+1)+ ": immediate argument is not a hex number");
								  }
							  }
							  else{
								  errors.put(i+1, "Error on line " + (i+1)+ "allowsImmediate does not contain our value");
								  
							  }
						  }
					  }
				  }
				  
			  }
		  }
		  int offset = inCode.size() +1;
		  for(int i = 0; i<inCode.size(); i++){
			  String[] parts = inData.get(i).split("\\s+");
			  // Loop through out data, what?
			  if(parts.length != 2) {
				  errors.put((offset+i), "Error on line " + (offset +i) + "This is not an address/value pair");
			  }
			  else {
				  int addr = -1;
				  int val = -1;
				  try{
					  addr = Integer.parseInt(parts[0],16);
				  } catch(NumberFormatException e){
					  errors.put((offset+i),"Error on line " + (offset + i) + "The address is not a hex number");
				  }
				  try{
					  val = Integer.parseInt(parts[1],16);
				  } catch(NumberFormatException e){
					  errors.put((offset+i),"Error on line " + (offset + i) + "The address is not a hex number");
				  }
				  outData.add(Integer.toHexString(addr).toUpperCase() + " "
						  + Integer.toHexString(val).toUpperCase());
			  }
		  }
		  if(errors.size()==0) {
			  try (PrintWriter outp = new PrintWriter(output)){
			  for(String str : outCode) outp.println(str);
			  outp.println(-1); // the separator where the source has “DATA”
			  for(String str : outData) outp.println(str);
			  } catch (FileNotFoundException e) {
			  errors.put(0, "Error: Unable to write the assembled program to the output file");
			  }
		  }
		  return true; // TRUE means there were no errors 
	  } 
}
