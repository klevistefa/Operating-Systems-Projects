import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.lang.Math;

public class Paging{

  //Constant used for debugging or "show random" initally set to false; Can be changed through command line
  public static boolean DEBUG = false;
  public static boolean SHOW_RANDOM = false;
  //Quantum used for references
  public static final int quantum = 3;

  //Global data that holds the needed variables to produce the output
  public static Scanner scanner;
  public static int pageSize;
  public static int processSize;
  public static int refNum;
  public static Frame[] frameTable;
  public static int cycle = 1;
  public static int totalFinished = 0;

  public static String replacementAlgorithm;

  public static ArrayList<Process> processes;
  public static ArrayList<Integer> LIFO;
  public static ArrayList<Integer> LRU;

  public static void main(String[] args) {

    //get the random-numbers file
    try {
      scanner = new Scanner(new File("random-numbers.txt"));
    } catch (Exception e){
      e.printStackTrace();
    }


    if (args.length < 7){
      System.out.println("Wrong command line format!!\n" +
                         "Please using the following: \'java Paging\' [M] [P] [S] [J] [N] [R] [D]\n" +
                         "  M = machine size in words (integer)\n" +
                         "  P = page size in words (integer)\n" +
                         "  S = process size (integer)\n" +
                         "  J = job mix (integer from 1 to 4)\n" +
                         "  N = number of references per process (integer)\n" +
                         "  R = replacement algorithm (lifo, lru, or random)\n" +
                         "  D = 0 for no debugging, 1 for debugging output, and 2 for show-random\n");
      System.exit(0);
    }

    initialize(args);
    handlePaging();
    printResults();
  }

  /*Method used to initialize the global variables*/
  public static void initialize(String[] args){
    //Printing the user input
    System.out.printf("The machine size is %s.\nThe page size is %s.\nThe process size is %s.\nThe job mix number is %s.\n"+
       "The number of references per process is %s.\nThe replacement algorithm is %s.\nThe level of debuggin output is %s.\n\n",
       args[0],args[1],args[2],args[3],args[4],args[5], args[6]);

    //Initializing the user input variables
    pageSize = Integer.parseInt(args[1]);
    frameTable = new Frame[Integer.parseInt(args[0])/pageSize];
    processes =  new ArrayList<Process>();
    processSize = Integer.parseInt(args[2]);
    refNum = Integer.parseInt(args[4]);
    replacementAlgorithm = args[5];

    //Set the debugging level
    if (Integer.parseInt(args[6]) % 10 == 1){
      DEBUG = true;
      if (Integer.parseInt(args[6]) / 10 == 1){
        SHOW_RANDOM = true;
      }
    }

    //Initialize the sets of processes
    switch (Integer.parseInt(args[3])){
      case 1:
        processes.add(new Process(1f, 0f, 0f, processSize, refNum));
        break;

      case 2:
        for (int i = 0; i < 4; i++){
          processes.add(new Process(1f, 0f, 0f, processSize, refNum));
        }
        break;

      case 3:
        for (int i = 0; i < 4; i++){
          processes.add(new Process(0f, 0f, 0f, processSize, refNum));
        }
        break;

      case 4:
        processes.add(new Process(.75f, .25f, 0f, processSize, refNum));
        processes.add(new Process(.75f, 0, .25f, processSize, refNum));
        processes.add(new Process(.75f, .125f, .125f, processSize, refNum));
        processes.add(new Process(.5f, .125f, .125f, processSize, refNum));
        break;

      default:
        System.out.println("Error: Wrong input for job mix! (use an integer from 1 to 4)");
        System.exit(0);
        break;
    }

    LRU = new ArrayList<>();
    LIFO = new ArrayList<>();
  }

  /*Handles paging as described in the assignment:
    1. If it's the first reference for a process use the word 111*k mod S (k being the word and S the size of the process).
    2. If not the first reference find the next referenced word depeneding on the job mix (i.e: A, B, C)
    3. When word is found calculate the word's resident page using Math.floor(word / pageSize)
    4. Check if page is currently in the frame.
    5. If page is in the frame update with LRU
    6. If page is not in the frame find a free frame if possible. If not possible then evict the resident frame using the specified replacement algorithm
    */
  public static void handlePaging(){
    while (totalFinished < processes.size()){ //iterate until all the processes are finished

      for (int i = 0; i < processes.size(); i++){
        if (!processes.get(i).isFinished()){ //process not finished

          for (int j = 0; j < quantum; j++){ //acount for the quantum

            //If this is the first time referenced
            if (processes.get(i).isFirstTimeReference()){

              int freeFrame = findFreeFrame(); //find the highest numbered free frame

              //If there exists a free frame
              if (freeFrame != -1){

                frameTable[freeFrame] = new Frame(cycle, i, (int)Math.floor((double)((111 * (i+1)) % processes.get(i).getSize())/(double)pageSize));

                if (DEBUG){
                  System.out.printf("%d references word %d (page %d) at time %d: Fault, using frame %d.\n", i+1, (111 * (i+1)) % processes.get(i).getSize(),
                                    (int)Math.floor((double)((111 * (i+1)) % processes.get(i).getSize())/(double)pageSize), cycle, freeFrame);
                }

                processes.get(i).incrementFaults();
                LIFO.add(freeFrame);
                LRU.add(freeFrame);

              //If there aren't any free frames
              } else {

                if(DEBUG){
                  System.out.printf("%d references word %d (page %d) at time %d: ", i + 1, (111 * (i+1))%processes.get(i).getSize(),
                                  (int) Math.floor((double)((111 * (i+1)) % processes.get(i).getSize()) / (double)pageSize), cycle);
                }

                processes.get(i).incrementFaults();

                if (replacementAlgorithm.equals("lifo")){

                  int topIndex = LIFO.size() - 1;

                  if(DEBUG){
                    System.out.printf("Fault, evicting page %d of %d from frame %d.\n", frameTable[LIFO.get(topIndex)].getValue(),
                                      frameTable[LIFO.get(topIndex)].getProcess() + 1, LIFO.get(topIndex));
                  }

                  processes.get(frameTable[LIFO.get(topIndex)].getProcess()).setResidency(cycle - frameTable[LIFO.get(topIndex)].getCycle());
                  processes.get(frameTable[LIFO.get(topIndex)].getProcess()).incrementEvictions();
                  frameTable[LIFO.get(topIndex)] = new Frame(cycle, i, (int) Math.floor((double)((111 * (i+1)) % processes.get(i).getSize()) / (double)pageSize));
                  // int temp = LIFO.get(topIndex);
                  // LIFO.remove(topIndex);
                  // LIFO.add(temp);

                } else if (replacementAlgorithm.equals("lru")){

                  if(DEBUG){
                    System.out.printf("Fault, evicting page %d of %d from frame %d.\n", frameTable[LRU.get(0)].getValue(),
                    frameTable[LRU.get(0)].getProcess() + 1, LRU.get(0));
                  }

                  processes.get(frameTable[LRU.get(0)].getProcess()).setResidency(cycle - frameTable[LRU.get(0)].getCycle());
                  processes.get(frameTable[LRU.get(0)].getProcess()).incrementEvictions();
                  frameTable[LRU.get(0)] = new Frame(cycle, i, (int) Math.floor((double)((111 * (i+1)) % processes.get(i).getSize()) / (double)pageSize));
                  LRU.add(LRU.remove(0));

                } else if (replacementAlgorithm.equals("random")){
                  int randFrame = scanner.nextInt()%frameTable.length;

                  if(DEBUG){
                    System.out.printf("Fault, evicting page %d of %d from frame %d.\n", frameTable[randFrame].getValue(),
                    frameTable[randFrame].getProcess() + 1, randFrame);
                  }

                  processes.get(frameTable[randFrame].getProcess()).setResidency(cycle - frameTable[randFrame].getCycle());
                  processes.get(frameTable[randFrame].getProcess()).incrementEvictions();
                  frameTable[randFrame] = new Frame(cycle, i, (int) Math.floor((double)((111 * (i+1)) % processes.get(i).getSize()) / (double)pageSize));

                } else {
                  System.out.println("Error: Replacement Algorithm unrecognizable");
                  System.exit(0);
                }
              }

              processes.get(i).setFirstTimeReferencedFalse();
              processes.get(i).setPreviousRef(111 * (i+1) % processes.get(i).getSize());

            //If this is not the first time referenced
            } else {
              int frame = checkInTable(i , (int) Math.floor((double)processes.get(i).getCurrentRef() / (double)pageSize));
              if(DEBUG){
                System.out.printf("%d references word %d (page %d) at time %d: ", i + 1, processes.get(i).getCurrentRef(),
                                  (int)Math.floor((double)processes.get(i).getCurrentRef()/(double)pageSize), cycle);
              }

              //If frame not in the table
              if (frame == -1){
                processes.get(i).incrementFaults();
                int freeFrame = findFreeFrame();

                //If there is a free frame put the page in the frame
                if (freeFrame != -1){
                  frameTable[freeFrame] = new Frame(cycle, i, (int)Math.floor((double)processes.get(i).getCurrentRef()/(double)pageSize));

                  if(DEBUG){ System.out.printf("Fault, using free frame %d.\n", freeFrame); }

                  LRU.add(freeFrame);
                  LIFO.add(freeFrame);

                //If there's no free frame, evict a page depeneding on the specified algorithm from the command line
                } else {

                  if (replacementAlgorithm.equals("lifo")){
                    int topIndex = LIFO.size() - 1;

                    if(DEBUG){
					                 System.out.printf("Fault, evicting page %d of %d from frame %d.\n", frameTable[LIFO.get(topIndex)].getValue(),
							                               frameTable[LIFO.get(topIndex)].getProcess() + 1, LIFO.get(topIndex));
					          }

					          processes.get(frameTable[LIFO.get(topIndex)].getProcess()).setResidency(cycle - frameTable[LIFO.get(topIndex)].getCycle());
					          processes.get(frameTable[LIFO.get(topIndex)].getProcess()).incrementEvictions();

					          frameTable[LIFO.get(topIndex)] = new Frame(cycle, i, (int)Math.floor((double)processes.get(i).getCurrentRef()/(double)pageSize));
					          // int temp = LIFO.get(topIndex);
					          // LIFO.remove(topIndex);
					          // LIFO.add(temp);

                  } else if (replacementAlgorithm.equals("lru")){

                    if(DEBUG){
                      System.out.printf("Fault, evicting page %d of %d from frame %d.\n", frameTable[LRU.get(0)].getValue(),
                                      frameTable[LRU.get(0)].getProcess() + 1, LRU.get(0));
                    }
                    processes.get(frameTable[LRU.get(0)].getProcess()).setResidency(cycle - frameTable[LRU.get(0)].getCycle());
                    processes.get(frameTable[LRU.get(0)].getProcess()).incrementEvictions();

                    frameTable[LRU.get(0)] = new Frame(cycle, i, (int)Math.floor((double)processes.get(i).getCurrentRef()/(double)pageSize));
                    LRU.add(LRU.remove(0));

                  } else if (replacementAlgorithm.equals("random")){

                    int randomFrame = scanner.nextInt() % frameTable.length;
                    if (DEBUG){
                      System.out.printf("Fault, evicting page %d of %d from frame %d.\n", frameTable[randomFrame].getValue(),
							                         frameTable[randomFrame].getProcess() + 1, randomFrame);
                    }

                    processes.get(frameTable[randomFrame].getProcess()).setResidency(cycle - frameTable[randomFrame].getCycle());
                    processes.get(frameTable[randomFrame].getProcess()).incrementEvictions();
                    frameTable[randomFrame] = new Frame(cycle, i, (int)Math.floor((double)processes.get(i).getCurrentRef()/(double)pageSize));

                  } else {
                    System.out.println("Error: Algorithm unrecognizable");
                    System.exit(0);
                  }
                }

              //Update the LRU references for the resident pages
              } else {

                if (DEBUG){
                  System.out.printf("Hit in frame %d.\n", frame);
                }

                for (int k = 0; k < LRU.size(); k++){
                  if (frame == LRU.get(k)){
                    LRU.remove(k);
                    LRU.add(frame);
                    break;
                  }
                }
              }
              //Set previous word referenced to the current word referenced
              processes.get(i).setPreviousRef(processes.get(i).getCurrentRef());
            }

            //Calculate the next reference
            int random = scanner.nextInt();
            if(SHOW_RANDOM){
              System.out.printf("%d uses random number: %d\n", i + 1, random);
            }

            double probability = random/(Integer.MAX_VALUE + 1d);

            //Find the next word as described in the lab through the job mix input
            if (probability < processes.get(i).getA()) {
                 processes.get(i).setCurrentRef((processes.get(i).getPreviousRef()+1)%processes.get(i).getSize());

            } else if(probability < processes.get(i).getA() + processes.get(i).getB()) {
			           processes.get(i).setCurrentRef((processes.get(i).getPreviousRef()-5+processes.get(i).getSize()) % processes.get(i).getSize());

			      } else if(probability < processes.get(i).getA() + processes.get(i).getB() + processes.get(i).getC()) {
			           processes.get(i).setCurrentRef((processes.get(i).getPreviousRef()+4)%processes.get(i).getSize());

            //fully random
			      } else {
			           int tempRandom = scanner.nextInt();
			           if(SHOW_RANDOM){
                   System.out.printf("%d uses random number: %d\n", i + 1, tempRandom);
                 }
			           processes.get(i).setCurrentRef(tempRandom % processes.get(i).getSize());

			      }

            if (processes.get(i).decrementRef()){
              totalFinished++;
              j = quantum; //skip the quantum loop if process is finished
            }
            cycle++;
          }
        }
      }
    }
  }

  /*Checks if the page of a process is in the table and returns the current frame if it is, and -1 otherwise*/
  public static int checkInTable(int process, int page){
    for (int i = 0; i < frameTable.length; i++){
      if (frameTable[i] != null && frameTable[i].getValue() == page && frameTable[i].getProcess() == process){
        return i;
      }
    }
    return -1;
  }

  //Finds the "highest numbered" free frame and return its index or -1 if it doesn't exists
  public static int findFreeFrame(){
    for (int i = frameTable.length-1; i > -1; i--){
      if (frameTable[i] == null){
        return i;
      }
    }
    return -1;
  }

  /*Prints the desired output*/
  public static void printResults(){
    int totalFaults = 0;
    int totalEvictions = 0;
    int totalResidency = 0;

    System.out.println();

    for (int i = 0; i < processes.size(); i++){

      //Print the evictions if there are any
      if (processes.get(i).getEvictions() != 0){
        System.out.printf("Process %d had %d faults and %f average residency.\n", i+1, processes.get(i).getFaults(),
                         (double)processes.get(i).getResidency()/(double)processes.get(i).getEvictions());
        totalFaults += processes.get(i).getFaults();
        totalEvictions += processes.get(i).getEvictions();
        totalResidency += processes.get(i).getResidency();

      //If there are not evictions
      } else {
        System.out.printf("Process %d had %d faults.\n     With no evictions, the average residence is undefined.\n", i+1, processes.get(i).getFaults());
        totalFaults += processes.get(i).getFaults();
      }
    }

    //If there are any evictions in total
    if (totalEvictions != 0){
      System.out.printf("\nThe total number of faults is %d and the overall average residency is %f.\n", totalFaults, (double)totalResidency/(double)totalEvictions);
    //If no evictions exist in total
    } else {
      System.out.printf("\nThe total number of faults is %d.\n     With no evictions, the overall average residence residence is undefined.\n", totalFaults);
    }
  }
}
