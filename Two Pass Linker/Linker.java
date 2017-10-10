//Author: Klevis Tefa

import java.io.File;
import java.util.*;

public class Linker{

  public static File input;
  public static Scanner scanner;
  public static String memoryMap = "Memory Map\n";
  public static ArrayList<String> tokens; //list of all the strings in the input file
  public static MetaData data;



  public static void main(String[] args) {
    data = new MetaData();
    tokens = new ArrayList<String>();
    getTokens(args[0]);
		LinkerFirstPass();
    data.printSymbolTable();
    LinkerSecondPass();
    PrintOutput();

  }

  public static void LinkerFirstPass(){
    //int pointer = 0;
    int pointer = 1; //skipping the first token which shows number of modules
    int memoryCounter = 0;
    int modCounter = 1;
    int counter = 0;

    //Handling a single module
    while (pointer < tokens.size()){

      //Handling external symbol definitions
      counter = 2 * Integer.parseInt(tokens.get(pointer++));
      for (int i = 0; i < counter; i+= 2){
        data.addSymbol(tokens.get(pointer++), memoryCounter + Integer.parseInt(tokens.get(pointer++)), modCounter);
      }

      //Skipping use list for now
      counter = 2 * Integer.parseInt(tokens.get(pointer++));
      pointer += counter;

      //Counting memory
      counter = Integer.parseInt(tokens.get(pointer++));
      memoryCounter += counter;

      data.addModule(memoryCounter);
      pointer += counter;
      modCounter++;
    }

    data.setLastAddr(memoryCounter);
  } //end of first pass

  public static void LinkerSecondPass(){
    HashMap<String, Integer> useList = new HashMap<String, Integer>(); //Use lits of NU pairs of (S, R) type as described in the assignment

    int pointer = 1; //skipping the number of modules
    int counter = 0;
    int memoryCounter = 0;
    int moduleNr = 0;

    while (pointer < tokens.size()){
      useList.clear();

      //Skipping external symbol definitions
      pointer += 1 + 2*Integer.parseInt(tokens.get(pointer));
      //Keeping track of used elements
      counter = 2*Integer.parseInt(tokens.get(pointer++));
      for(int i = 0; i < counter; i+=2){
        useList.put(tokens.get(pointer++), Integer.parseInt(tokens.get(pointer++)));
      }

      //Handling instructions
      counter = Integer.parseInt(tokens.get(pointer++));
      HashMap<String, Boolean> isUsed = new HashMap<String, Boolean>(); //keeping track if the elements of the current use list are used or no (and setting all false in next line)

      for (Map.Entry<String, Integer> entry: useList.entrySet()){ isUsed.put(entry.getKey(), false);}

      int[] words = new int[counter];
      //putting the current modules instructions in an array
      for (int i = 0; i < counter; i++){
        words[i] = Integer.parseInt(tokens.get(pointer++));
      }
      //associating instructions reffered by the use list elements with its appropriate symbol
      HashMap<Integer, String> resolvedAddrs = new HashMap<Integer,String>();
      for (Map.Entry<String,Integer> entry: useList.entrySet()){
        String symbol = entry.getKey();
        int relAddr = entry.getValue();

        //while the end of linked list not reached (!= 777) instructions with their appropriate use list symbol
        while ((words[relAddr]/10)%1000 != 777){

          resolvedAddrs.put(relAddr, symbol);
          relAddr = words[relAddr]/10%1000;
        }

        resolvedAddrs.put(relAddr, symbol);
      }
      //Start handling each word one by one
      for (int i = 0; i < counter; i++){
        int addrComponent = words[i] % 10;
        int instruction = words[i]/10;

        if (addrComponent == 1 || addrComponent == 2){
          if (resolvedAddrs.containsKey(i)){
            if (data.getSymAddr(resolvedAddrs.get(i)) != null){ //catching an error

              int relAddr = data.getSymAddr(resolvedAddrs.get(i));
              memoryMap += memoryCounter++ + ": " + Integer.toString((instruction/1000)*1000 + relAddr);
              memoryMap += " Error: Immediate address on use list; treated as External\n";

              data.setUsed(resolvedAddrs.get(i), true);
              isUsed.put(resolvedAddrs.get(i), true);
            }
          } else {
            memoryMap += memoryCounter++ + ": " + instruction + "\n";
          }

        } else if (addrComponent == 3){
          if (resolvedAddrs.containsKey(i)){
            if (data.getSymAddr(resolvedAddrs.get(i)) == null){//catching error

              memoryMap += memoryCounter++ + ": " + Integer.toString((instruction/1000)*1000);
              memoryMap += " Error: " + resolvedAddrs.get(i) + " is note defined. zero used\n";
              isUsed.put(resolvedAddrs.get(i), true);

            } else {

              int relAddr = data.getSymAddr(resolvedAddrs.get(i));
              memoryMap += memoryCounter++ + ": " + Integer.toString((instruction/1000)*1000 + relAddr) + "\n";

              data.setUsed(resolvedAddrs.get(i), true);
              isUsed.put(resolvedAddrs.get(i), true);

            }
          } else {

            int relAddr = instruction%1000 + data.getModuleAddr(moduleNr);
            memoryMap += memoryCounter++ + ": " + Integer.toString(((instruction/1000)*1000) + relAddr) + "\n";

          }
        } else if (addrComponent == 4){

          int externalAddr = 0;
          if (resolvedAddrs.containsKey(i)){

            externalAddr = data.getSymAddr(resolvedAddrs.get(i));
            memoryMap += memoryCounter++ + ": " + Integer.toString((instruction/1000)*1000 + externalAddr) +  "\n";

            data.setUsed(resolvedAddrs.get(i), true);
            isUsed.put(resolvedAddrs.get(i), true);

          } else { //catching error

            memoryMap += memoryCounter++ + ": " + Integer.toString((instruction/1000)*1000 + externalAddr);
            memoryMap += " Error: E type address not on use chain; treated as I type.\n";
          }

        } else { //if address component is something other than 1, 2, 3, 4, print error message and exit program
          System.err.println("ERROR: Something went wrong in instruction handling during the second pass.");
    		  System.err.println("Pointer: " + Integer.toString(pointer - counter + i));
    		  System.err.println("Current Token: " + tokens.get(pointer - counter + i));
    		  System.exit(0);
        }
      }

      //Add warning in case symbols that appeared in the use list for a module but weren't used.
      for (Map.Entry<String, Integer> entry: useList.entrySet()){
        String symbol = entry.getKey();
          if (!isUsed.get(symbol)){
            data.addWarning("Warning: In module " +moduleNr + ", " + symbol + " appeared in the use list but was not actually used.\n");
          }
        }
        moduleNr ++;
      }

  }//end of 2nd passer

  public static void getTokens(String fileName){

    //Load the input file
    try{
      input = new File(fileName);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    //Build the input string from fileName
    try{
      scanner = new Scanner(input);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    while (scanner.hasNextLine()){
      if (scanner.hasNext()){
        tokens.add(scanner.next());
      } else {
        break;
      }
    }
  }

  public static void PrintOutput(){
    System.out.println(memoryMap);
    data.printWarningList();
  }
}
