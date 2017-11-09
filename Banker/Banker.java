import java.util.Scanner;
import java.io.File;

public class Banker{

  //Define the resource managers constrains
  public static final int MAX_TASKS = 10;
  public static final int MAX_INST = 25;
  public static final int MAX_RESOURCES = 10;

  //Define activities for tasks
  public static final int INITIATE = 0;
  public static final int REQUEST = 1;
  public static final int RELEASE = 2;
  public static final int COMPUTE = 3;
  public static final int TERMINATE = 4;

  //variable used to print out a more detailed information used for debugging
  public static final boolean detailed = true;

  private static Scanner scanner;
  private static String fileInput;

  //Define variables needed
  public static int totalRes;
  public static int totalTasks;
  public static int totalFinished;
  public static int[] releasedResources;
  public static Resource[] resources;
  public static Task[] tasks;

  public static void main(String[] args) {
    if (args.length != 1){
      System.out.printf("Usage: [%s] [filename]", args[0]);
      System.exit(0);
    }

    fileInput = "./lab3-io/" + args[0];

    optimisticManager();
    System.out.println("\n");
    bankersManager();


  }


  //Initializes the program by cleaning the data structures and setting it up for the resource manager by reading the input
  public static void initialize(String fileName){
    //Reset variables
    totalRes = 0;
    totalTasks = 0;
    totalFinished = 0;

    //initiate the arrays
    releasedResources = new int[MAX_RESOURCES];
    resources = new Resource[MAX_RESOURCES];
    tasks = new Task[MAX_TASKS];

    //set resources
    for (int i = 0; i < MAX_RESOURCES; i++){
      resources[i] = new Resource();
      releasedResources[i] = 0;
    }

    //Reset the data structures
    for (int i = 0; i < MAX_TASKS; i++){
      tasks[i] = new Task(i+1);

      //create resources
      for (int j = 0; j < MAX_RESOURCES; j++){
        tasks[i].setResourceClaim(j, 0);
        tasks[i].setCurrentResource(j, 0);
      }

      for (int j = 0; j < MAX_INST; j++){
        tasks[i].setInstruction(j, new Instruction());
      }
    }

    //Read input and exist if it cant open
    File file = new File(fileName);
    try {
      scanner = new Scanner(file);
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("Can't open file");
      System.exit(0);
    }

    //local variables used for reading
    int inputNumber;
    String[] inputString = scanner.nextLine().split(" ");


    totalTasks = Integer.parseInt(inputString[0]);
    //Exit if there are more tasks than the max allowed
    if (totalTasks > MAX_TASKS){
      System.out.printf("ERROR: The resource manager cannot have more than %d tasks", MAX_TASKS);
      System.exit(0);
    }

    totalRes = Integer.parseInt(inputString[1]);
    //Exit if there are more resources than the max allowed
    if (totalRes > MAX_RESOURCES){
      System.out.printf("ERROR: The resource manager cannot have more than %d resources", MAX_RESOURCES);
      System.exit(0);
    }

    //initialize resources
    for (int i = 0; i < totalRes; i++){
      resources[i].setTotal(Integer.parseInt(inputString[i+2]));
      resources[i].setAmountLeft(Integer.parseInt(inputString[i+2]));
    }

    //initialize instructions
    while(scanner.hasNextLine()){
      String line = scanner.nextLine();
      if (!line.isEmpty()){
        inputString = line.split("\\s+");
        int taskNum = Integer.parseInt(inputString[1]) - 1;
        switch(inputString[0]){
          case "initiate":
            tasks[taskNum].getInstruction(tasks[taskNum].getInstNum()).setInstruction(INITIATE);
            break;
          case "request":
            tasks[taskNum].getInstruction(tasks[taskNum].getInstNum()).setInstruction(REQUEST);
            break;
          case "release":
            tasks[taskNum].getInstruction(tasks[taskNum].getInstNum()).setInstruction(RELEASE);
            break;
          case "compute":
            tasks[taskNum].getInstruction(tasks[taskNum].getInstNum()).setInstruction(COMPUTE);
            break;
          case "terminate":
            tasks[taskNum].getInstruction(tasks[taskNum].getInstNum()).setInstruction(TERMINATE);
            break;
          default:
            System.out.printf("ERROR: Instruction %s is incompatible", inputString[0]);
            break;
        }
        if (inputString[0].equals("compute")){
          //set the delay
          tasks[taskNum].getInstruction(tasks[taskNum].getInstNum()).setDelay(Integer.parseInt(inputString[2]));
        } else {
          //set resource that the instruction is referring and the amount
          tasks[taskNum].getInstruction(tasks[taskNum].getInstNum()).setResource(Integer.parseInt(inputString[2]) - 1);
          tasks[taskNum].getInstruction(tasks[taskNum].getInstNum()).setAmount(Integer.parseInt(inputString[3]));
        }
        //take the next instrucion
        tasks[taskNum].nextInstructionNumber();
      }
    }

    scanner.close();

  }

  /*Very straight forward resource manager. After it reads instructions counts cycles during initialization. On a request satisfies it if possible.\,
  otherwise deny the task and put it on hold and on the front of the list so it get's processed first when resources are released. Release is very straightforward:
  just release the resource of the task; Compute sets a delay for a task. Next iteration of the algorithm that task will have to compute and not take instructions.
  Termination is pretty straightforward too. After all the tasks are iterated check for deadlock and repeat again until all are finished.
  */
  public static void optimisticManager(){
    initialize(fileInput);

    if (detailed) System.out.println("Debugging:");

    int blockedIndex = 0; //index used for FIFO ordering

    while (totalFinished < totalTasks){

      blockedIndex = 0; //reset denial index to 0

      //Update resources left from last cycle
      for (int i = 0; i < totalRes; i++){
        resources[i].setAmountLeft(resources[i].getAmountLeft() + releasedResources[i]);
        releasedResources[i] = 0;
      }

      for (int i = 0; i < totalTasks; i++){

        //only process non finished tasks
        if (!tasks[i].isTerminated() && !tasks[i].isAborted()){

          switch (tasks[i].currentInstruction().getInstruction()){

            case INITIATE: //optimistic disregards claims
              if (detailed) System.out.printf("Task %d initialized\n", tasks[i].getTaskNum());
              tasks[i].nextInstruction();
              tasks[i].addCycle();
              break;

            case REQUEST: //satisfy a request if possible
              if (resources[tasks[i].currentInstruction().getResource()].getAmountLeft() >= tasks[i].currentInstruction().getAmount()){

                tasks[i].setCurrentResource(tasks[i].currentInstruction().getResource(), tasks[i].getCurrentResource(tasks[i].currentInstruction().getResource()) + tasks[i].currentInstruction().getAmount());
                resources[tasks[i].currentInstruction().getResource()].setAmountLeft(resources[tasks[i].currentInstruction().getResource()].getAmountLeft() - tasks[i].currentInstruction().getAmount());
                tasks[i].satisfy(true);
                if (detailed) System.out.printf("Task %d request allowed\n",tasks[i].getTaskNum());
                tasks[i].nextInstruction();
                tasks[i].addCycle();
              } else {
                //tasks unsatisfied so put it in the fron of the tasks array
                tasks[i].satisfy(false);
                tasks[i].incrementWaitTime();
                tasks[i].addCycle();

                if (blockedIndex != i){
                  Task tempTask = tasks[i];
                  for (int j = i; j > blockedIndex; j--){
                    tasks[j] = tasks[j-1];
                  }
                  tasks[blockedIndex] = tempTask;
                }
                if (detailed) System.out.printf("Denied task %d request\n",tasks[blockedIndex].getTaskNum());
                blockedIndex++;
              }
              break;

            case RELEASE: //just release the tasks resource

              tasks[i].setCurrentResource(tasks[i].currentInstruction().getResource(), tasks[i].getCurrentResource(tasks[i].currentInstruction().getResource()) - tasks[i].currentInstruction().getAmount());
              releasedResources[tasks[i].currentInstruction().getResource()] += tasks[i].currentInstruction().getAmount();
              if (detailed) System.out.printf("Task %d released\n",tasks[i].getTaskNum());
              tasks[i].nextInstruction();
              tasks[i].addCycle();
              break;

              case COMPUTE: //handled as optimistic
                if (!tasks[i].isComputing()){
                  tasks[i].setComputing(true);
                  tasks[i].setCyclesLeft(tasks[i].currentInstruction().getDelay());
                  tasks[i].addCycle();
                  tasks[i].decrementCyclesLeft();
                  if (tasks[i].getCyclesLeft() == 0){
                    tasks[i].setComputing(false);
                    tasks[i].nextInstruction();
                  }
                }
                else {
                  if (detailed) System.out.printf("Task %d computing for %d cycles\n",tasks[i].getTaskNum(), tasks[i].currentInstruction().getDelay());
                  tasks[i].addCycle();
                  tasks[i].decrementCyclesLeft();
                  if (tasks[i].getCyclesLeft() == 0){
                    tasks[i].setComputing(false);
                    tasks[i].nextInstruction();
                  }
                }
                //tasks[i].decrementCyclesLeft();
                break;

            case TERMINATE: //uodate the number of finished taxes
              tasks[i].terminate();
              totalFinished++;
              if (detailed) System.out.printf("Task %d terminated\n",tasks[i].getTaskNum());
              break;
          }
        }

      }
      checkDeadlock();
    }
    printResults("FIFO");
  }

  /* This method checks to see if if a deadlock occurs by comparing the number of tasks alive with the number of tasks that can't be satisfied
  It finds a satisfied task by seeing which tasks can't request because of they're requesting more resources then there are available. This way we find
  unsatisfied tasks. When deadlock occurs in a FIFO scheduling the first unsatisfied tasks is aborted and it releases all of its resources. */
  public static void checkDeadlock(){

    int totalUnsatisfied = 0;
    int totalAlive = 0;

    for (int i =0; i < totalTasks; i++){
      if (!tasks[i].isTerminated() && !tasks[i].isAborted()){ //count the number of alive tasks
        totalAlive++;

        //check to see if task is satisfied or not
        if (!tasks[i].isSatisfied() &&
          tasks[i].currentInstruction().getInstruction() == REQUEST &&
          (resources[tasks[i].currentInstruction().getResource()].getAmountLeft() +
          releasedResources[tasks[i].currentInstruction().getResource()]) <
          tasks[i].currentInstruction().getAmount()){
            totalUnsatisfied++;
        }
      }
    }

    //Abort task if deadlock
    if (totalUnsatisfied == totalAlive && totalAlive != 0){
      int lowestTask = MAX_TASKS+1;
      int lowestTaskIndex = MAX_TASKS;

      for (int i = 0; i < totalTasks; i++){
        if (!(tasks[i].isAborted() || tasks[i].isTerminated())){

          if ((resources[tasks[i].currentInstruction().getResource()].getAmountLeft() +
            releasedResources[tasks[i].currentInstruction().getResource()]) <
            tasks[i].currentInstruction().getAmount()){
              if (tasks[i].getTaskNum() < lowestTask){
                lowestTask = tasks[i].getTaskNum();
                lowestTaskIndex = i;
              }
            }
        }
      }

      if (detailed) System.out.printf("Aborted task %d\n",tasks[lowestTaskIndex].getTaskNum());
      tasks[lowestTaskIndex].abort();
      totalFinished++;

      //release resources
      for (int i = 0; i < totalRes; i++){
        releasedResources[i] += tasks[lowestTaskIndex].getCurrentResource(i);
      }
      checkDeadlock(); //recursively checkDeadlock to see if deadlock is still present
    }
  }

  /*This resource manager performs initialization like the optimistic one, but records claims as well and aborts a task that claims more then there are resurces in
  total. Request satisfies the task request and sees if the current state is safe. If it's not it reverses the request, and the current task is put on hold and in the
  right place in the array that serves as a FIFO queue. If resources are insufficient then it handles request like the optimistic manager. Release, compute, and terminate
  are performed just as in the optimistic manager.
  */
  public static void bankersManager(){
    initialize(fileInput);

    if (detailed) System.out.println("Debugging:");

    int blockedIndex = 0; //same purpose as in optimistic

    while (totalFinished < totalTasks){

      blockedIndex = 0; //reset

      //Update resources left from last cycle
      for (int i = 0; i < totalRes; i++){
        resources[i].setAmountLeft(resources[i].getAmountLeft() + releasedResources[i]);
        releasedResources[i] = 0;
      }

      for (int i = 0; i < totalTasks; i++){
        //only process non finished tasks
        if (!tasks[i].isTerminated() && !tasks[i].isAborted()){

          switch (tasks[i].currentInstruction().getInstruction()){

            case INITIATE:
              //abort task if claim is more then total available
              if (tasks[i].currentInstruction().getAmount() > resources[tasks[i].currentInstruction().getResource()].getTotal()){
                tasks[i].abort();
                totalFinished++;
                System.out.printf("Task %d claims %d units of resource %d which exceeds number of units present (%d). Task aborted.\n",
                  tasks[i].getTaskNum(), tasks[i].currentInstruction().getAmount(), tasks[i].currentInstruction().getResource()+1,
                    resources[tasks[i].currentInstruction().getResource()].getTotal());

              } else { //else record its claims

                tasks[i].setResourceClaim(tasks[i].currentInstruction().getResource(), tasks[i].currentInstruction().getAmount());
                if (detailed) System.out.printf("Task %d initialized\n", tasks[i].getTaskNum());
                tasks[i].nextInstruction();
                tasks[i].addCycle();
              }
              break;

            case REQUEST:
              //if claims are exceeded task aborted
              if (tasks[i].getCurrentResource(tasks[i].currentInstruction().getResource()) + tasks[i].currentInstruction().getAmount() > tasks[i].getResourceClaim(tasks[i].currentInstruction().getResource())){

                tasks[i].abort();
                totalFinished++;

                for (int j = 0; j < totalRes; j++){
                  resources[j].setAmountLeft(resources[j].getAmountLeft() + tasks[i].getCurrentResource(j));

                }
                if (detailed) System.out.printf("During cycle %d-%d, task %d request exceedes its claim. Task aborted\n",tasks[i].getCycle(), tasks[i].getCycle()+1, tasks[i].getTaskNum());
              }
              //if resources sufficient satisfy the request temporarily
              else if (resources[tasks[i].currentInstruction().getResource()].getAmountLeft() >= tasks[i].currentInstruction().getAmount()){

                tasks[i].setCurrentResource(tasks[i].currentInstruction().getResource(),
                tasks[i].getCurrentResource(tasks[i].currentInstruction().getResource()) + tasks[i].currentInstruction().getAmount());
                resources[tasks[i].currentInstruction().getResource()].setAmountLeft(resources[tasks[i].currentInstruction().getResource()].getAmountLeft()
                - tasks[i].currentInstruction().getAmount());

                //check to see if state is safe
                if (!isSafeState()){

                  //if not reverse the request
                  tasks[i].setCurrentResource(tasks[i].currentInstruction().getResource(),
                  tasks[i].getCurrentResource(tasks[i].currentInstruction().getResource()) - tasks[i].currentInstruction().getAmount());
                  resources[tasks[i].currentInstruction().getResource()].setAmountLeft(resources[tasks[i].currentInstruction().getResource()].getAmountLeft()
                  + tasks[i].currentInstruction().getAmount());

                  //update cycle and wait time, but dont move to next instruction
                  tasks[i].satisfy(false);
                  tasks[i].incrementWaitTime();
                  tasks[i].addCycle();

                  //Order it according to FIFO
                  if (blockedIndex != i){
                    Task tempTask = tasks[i];
                    for (int j = i; j > blockedIndex; j--){
                      tasks[j] = tasks[j-1];
                    }
                    tasks[blockedIndex] = tempTask;
                  }
                  if (detailed) System.out.printf("Task %d request denied: not safe\n",tasks[blockedIndex].getTaskNum());
                  blockedIndex++;

                } else {
                  //if safe state accept the request and update the cycle
                  tasks[i].satisfy(true);
                  if (detailed) System.out.printf("Task %d request allowed\n",tasks[i].getTaskNum());
                  tasks[i].addCycle();
                  tasks[i].nextInstruction();
                }
              }
              else { //else insufficient resources so handle it as the optimistic manager

                tasks[i].satisfy(false);
                tasks[i].incrementWaitTime();
                tasks[i].addCycle();

                if (blockedIndex != i){
                  Task tempTask = tasks[i];
                  for (int j = i; j > blockedIndex; j--){
                    tasks[j] = tasks[j-1];
                  }
                  tasks[blockedIndex] = tempTask;
                }
                if (detailed) System.out.printf("Task %d request denied: insufficient resources\n",tasks[blockedIndex].getTaskNum());
                blockedIndex++;
              }
              break;

            case RELEASE: //handles as optimistic
              tasks[i].setCurrentResource(tasks[i].currentInstruction().getResource(), tasks[i].getCurrentResource(tasks[i].currentInstruction().getResource()) - tasks[i].currentInstruction().getAmount());
              releasedResources[tasks[i].currentInstruction().getResource()] += tasks[i].currentInstruction().getAmount();
              if (detailed) System.out.printf("Task %d released\n",tasks[i].getTaskNum());
              tasks[i].nextInstruction();
              tasks[i].addCycle();
              break;

            case COMPUTE: //handled as optimistic
              if (!tasks[i].isComputing()){
                tasks[i].setComputing(true);
                tasks[i].setCyclesLeft(tasks[i].currentInstruction().getDelay());
                tasks[i].addCycle();
                tasks[i].decrementCyclesLeft();
                if (tasks[i].getCyclesLeft() == 0){
                  tasks[i].setComputing(false);
                  tasks[i].nextInstruction();
                }
              }
              else {
                if (detailed) System.out.printf("Task %d computing for %d cycles\n",tasks[i].getTaskNum(), tasks[i].currentInstruction().getDelay());
                tasks[i].addCycle();
                tasks[i].decrementCyclesLeft();
                if (tasks[i].getCyclesLeft() == 0){
                  tasks[i].setComputing(false);
                  tasks[i].nextInstruction();
                }
              }
              //tasks[i].decrementCyclesLeft();
              break;

            case TERMINATE: //handles as optimistic
              tasks[i].terminate();
              totalFinished++;
              if (detailed) System.out.printf("Task %d terminated at cycle %d\n",tasks[i].getTaskNum(), tasks[i].getCycle());
              break;
          }

        }
      }

    }
    printResults("BANKER'S");
  }

  /*A method used by the banker to see if a state is safe by checking if all the tasks will be satisfied with the current resources left.
  It iterates all the tasks by trying to satisfy their requirements until no more tasks can be satisfied. At this point there are two outcomes.
  Either no more tasks can be satisfied because all have been satisfied. In this case the safe is state. Or no more tasks can be satisfied because
  a deadlock has occured. In this case the safe is not state.*/
  public static boolean isSafeState(){

    Resource[] resTemp = new Resource[totalRes]; //temporary array of resources so the changes are not permanent
    int finished = -1; //variable to keep track if iteration finished

    boolean[] finishedIndex = new boolean[totalTasks]; //keeps track if a task is finished or not
    int alive = totalTasks - totalFinished;

    //create a temporary copy of resources
    for(int i = 0; i < totalRes; i++){
      resTemp[i] = new Resource();
      resTemp[i].setTotal(resources[i].getTotal());
      resTemp[i].setAmountLeft(resources[i].getAmountLeft());
    }

    //find which tasks are finished and which are not
    for(int i = 0; i < totalTasks; i++){
      if(tasks[i].isTerminated() || tasks[i].isAborted()){
        finishedIndex[i] = true;
      } else {
        finishedIndex[i] = false;
      }
    }

    //while not finished
    while (finished!=0){
      finished = 0;
      for (int i = 0; i < totalTasks; i++){
        int totalResSatisfied = 0;

        if (!finishedIndex[i]){ //if task hasn't finished
          for (int j = 0; j < totalRes; j++){
            if (tasks[i].getResourceClaim(j) - tasks[i].getCurrentResource(j) <= resTemp[j].getAmountLeft()){
              totalResSatisfied++;
            }
          }
          //if task satisfied pretend releasing its resources and see if more tasks can be satisfied
          if (totalResSatisfied == totalRes){
            alive--;
            finished++;
            finishedIndex[i] = true;
            for (int j = 0; j < totalRes; j++){
              resTemp[j].setAmountLeft(resTemp[j].getAmountLeft() + tasks[i].getCurrentResource(j));
            }
          }
        }
      }
    }
    return (alive == 0);
  }

  /*Prints the output for banker*/
  public static void printResults(String resManager){

    int totalRunTime = 0;
    int totalWaitTime = 0;

    Task[] newTasks = new Task[totalTasks];

    //reorder tasks by task number
    for(int i = 0; i < totalTasks; i++){
      newTasks[tasks[i].getTaskNum()-1] = tasks[i];
    }

    for(int i = 0; i < totalTasks; i++){
      tasks[i] = newTasks[i];
    }

    System.out.printf("%18s\n",resManager);
    for(int i = 0; i < totalTasks; i++){
      if(!tasks[i].isAborted()){
        System.out.printf("%10s%2d%7d%4d%4d%%\n","Task",tasks[i].getTaskNum(),tasks[i].getCycle(),tasks[i].getWaitTime(), Math.round((100*((float)tasks[i].getWaitTime()/(float)tasks[i].getCycle()))));
        totalRunTime += tasks[i].getCycle();
        totalWaitTime += tasks[i].getWaitTime();

      } else {
        System.out.printf("%10s%2d%13s\n","Task",tasks[i].getTaskNum(),"aborted");
      }
    }
    System.out.printf("%11s%8d%4d%4d%%\n","total",totalRunTime,totalWaitTime, Math.round((100*((float)totalWaitTime/(float)totalRunTime))));
  }
}
