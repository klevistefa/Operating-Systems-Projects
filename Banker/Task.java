public class Task{
  //A class that reperesents a task of the resource manager

  //Some predefined variables
  public static final int MAX_INST = 25;
  public static final int MAX_RESOURCES = 10;

  private int taskNum; //task identifier
  private int instNum; //number of instructions which get incremented every time a new instruction is added
  public int currentInst; //current instruction index
  private int cyclesLeft;
  private int waitTime;
  private int cycle;

  //task states
  private boolean terminated;
  private boolean aborted;
  private boolean satisfied;

  private boolean isComputing;

  private int[] resourceClaims; //resource claim
  private int[] resourceCurrent; //currently allocated resources

  private Instruction[] instructions; //Instruction type array that holds the logic of what the task needs to do throughout the program

  public Task(int taskNum){

    instNum = 0;
    currentInst = 0;
    cyclesLeft = 0;
    terminated = false;
    waitTime = 0;
    cycle = 0;
    aborted = false;
    satisfied = true;
    isComputing = false;
    this.taskNum = taskNum;

    instructions = new Instruction[MAX_INST];
    resourceClaims = new int[MAX_RESOURCES];
    resourceCurrent = new int[MAX_RESOURCES];
  }

  public void setComputing(boolean condition){
    isComputing = condition;
  }

  public boolean isComputing(){
    return isComputing;
  }

  public void setTaskNum(int num){
    taskNum = num;
  }

  public int getTaskNum(){
    return taskNum;
  }

  public int getInstNum(){
    return instNum;
  }

  public int getCurrentResource(int index){
    return resourceCurrent[index];
  }

  public int getResourceClaim(int resourceIndex){
    return resourceClaims[resourceIndex];
  }

  public void setResourceClaim(int index, int claim){
    this.resourceClaims[index] = claim;
  }

  public void setCurrentResource(int index, int num){
    this.resourceCurrent[index] = num;
  }

  public Instruction currentInstruction(){
    return this.instructions[currentInst];
  }

  public Instruction getInstruction(int index){
    return this.instructions[index];
  }

  public void setInstruction(int index, Instruction instruction){
    this.instructions[index] = instruction;
  }


  public int getCycle(){
    return this.cycle;
  }

  public int getCyclesLeft(){
    return this.cyclesLeft;
  }

  public void setCyclesLeft(int delay){
    cyclesLeft = delay;
  }

  public int getWaitTime(){
    return waitTime;
  }

  public boolean isTerminated(){
    return terminated;
  }

  public boolean isAborted(){
    return aborted;
  }

  public boolean isSatisfied(){
    return satisfied;
  }

  public void abort(){
    aborted = true;
  }

  public void satisfy(boolean condition){
    satisfied = condition;
  }

  public void terminate(){
    terminated = true;
  }

  public void addCycle(){
    cycle++;
  }

  public void decrementCyclesLeft(){
    cyclesLeft--;
  }

  public void nextInstruction(){
    currentInst++;
  }

  // public void setCurrentResource(int index, int num){
  //   resourceCurrent[index] = num;
  // }

  public void incrementWaitTime(){
    waitTime++;
  }

  public void nextInstructionNumber(){
    instNum++;
  }
}
