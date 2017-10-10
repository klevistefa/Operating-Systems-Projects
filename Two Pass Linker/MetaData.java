//Author: Klevis Tefa

import java.util.HashMap;
import java.util.ArrayList;

public class MetaData{
  private HashMap<String, Integer> symbols; //Symbols and their corresponding value (used to print symbol table)
  private HashMap<String, Integer> symModule; //keep track of each symbol's module
  private HashMap<String, Integer> addSize;
  private HashMap<String, Boolean> isUsed; //keeping track if a symbol is used

  private ArrayList<String> symList; //list of all symbols
  private ArrayList<Integer> modules; //list od all modules
  private ArrayList<String> multDefs; //symbols that are defined multiple times

  private String symTable = "Symbol Table\n";
  private String warnings = "";

  private int lastAddrs = 0;

  public MetaData(){
    symbols = new HashMap<String, Integer>();
    symModule = new HashMap<String, Integer>();
    addSize = new HashMap<String, Integer>();
    isUsed = new HashMap<String, Boolean>();
    symList = new ArrayList<String>();
    modules = new ArrayList<Integer>();
    multDefs = new ArrayList<String>();

    modules.add(0); //first module always starts with 0

  }

  public int getLastAddr(){
    return lastAddrs;
  }

  public void addWarning(String s){
    warnings += s;
  }

  public void setLastAddr(int n){
    lastAddrs = n;
  }

  public void addModule(int addr){
    modules.add(addr);
  }

  public int getModuleSize(){
    return modules.size();
  }

  public Integer getModuleAddr(int module){
    return modules.get(module);
  }

  public void addSymbol(String key, int value, int module){
    if (symbols.get(key) != null){
      multDefs.add(key);

    } else {
      symbols.put(key, value);
      isUsed.put(key, false);
      symList.add(key);
      symModule.put(key, module);
    }
  }

  public Integer getSymAddr(String key){
    return symbols.get(key);
  }

  public void printSymbolTable(){
    int moduleSize = 0;
    ArrayList<String> sizeList = new ArrayList<String>();

    for (int i = 0; i < symList.size(); i++){
      if (symModule.get(symList.get(i)) > modules.size()){
        moduleSize = getLastAddr() - getModuleAddr(symModule.get(symList.get(i))-1);

      } else {
        moduleSize = getModuleAddr(symModule.get(symList.get(i))) - getModuleAddr(symModule.get(symList.get(i))-1);
      }
      //Keeping track of symbols that their definition lies outside module
      if (symbols.get(symList.get(i)) - getModuleAddr(symModule.get(symList.get(i))- 1) > moduleSize-1){
        symModule.put(symList.get(i), getModuleAddr(symModule.get(symList.get(i))- 1)-1);
        symbols.put(symList.get(i), getModuleAddr(symModule.get(symList.get(i))- 1));
        sizeList.add(symList.get(i));
      }
    }


    for (int i = 0; i < symList.size(); i++){

      symTable += symList.get(i) + " = " + symbols.get(symList.get(i));
      if (multDefs.contains(symList.get(i))){
        symTable += " Error: This varible is defined multiple times. Its first value is used.";
      }

      if (sizeList.contains(symList.get(i))){
        symTable += " Error: The definition of " + symList.get(i) + " is outside module " + Integer.toString(symModule.get(symList.get(i))-1) + "; Zero (relative) used instead.";
      }
      symTable += "\n";
    }
    System.out.println(symTable);
  }

  public void setUsed(String key, boolean used){
	   isUsed.put(key,used);
  }

  public void printWarningList(){
	   for(int i = 0; i < symList.size(); i++){
	      if(!isUsed.get(symList.get(i))){
		        warnings += "Warning: " + symList.get(i) + " was defined in module " + Integer.toString(symModule.get(symList.get(i)) - 1 )+ " but was never used.\n";
	      }
	    }
	   System.out.println(warnings);
  }
}
