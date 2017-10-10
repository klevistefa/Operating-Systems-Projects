public class Process{

  public static final int UNSTARTED = 0;
  public static final int RUNNING = 1;
  public static final int BLOCKED = 2;
  public static final int TERMINATED = 3;
  public static final int READY = 4;

  private int arrivalTime;
  private int arrivalNumber;
  private int cpuTimeLeft;
  private int cpuBurst;
  private int cpuTime;
  private int ioBurst;
  private int state;

  private int totalTime;
  private int waitTime;
  private int ioTime;
  private int cpuBurstTime;
  private int previousCpuBurstTime;
  private int ioBurstTime;

  private float penaltyRatio;


  public Process(int arrivalNumber, int arrivalTime, int cpuBurst, int cpuTime, int ioBurst){
    this.arrivalTime = arrivalTime;
    this.arrivalNumber = arrivalNumber;
    this.cpuBurst = cpuBurst;
    this.cpuTimeLeft = cpuTime;
    this.cpuTime = cpuTime;
    this.ioBurst = ioBurst;

    state = UNSTARTED;
    totalTime = 0;
    waitTime = 0;
    ioTime = 0;

    penaltyRatio = 0;

    cpuBurstTime = -1;
    previousCpuBurstTime = cpuBurstTime;
    ioBurstTime = -1;

  }

  public int getArrivalNumber(){
    return arrivalNumber;
  }

  public int getArrivalTime(){
    return arrivalTime;
  }

  public int getCpuTimeLeft(){
    return cpuTimeLeft;
  }

  public int getCpuBurst(){
    return cpuBurst;
  }

  public int getioBurst(){
    return ioBurst;
  }

  public int getState(){
    return state;
  }

  public void setState(int i){
    state = i;
  }

  public int getCpuBurstTimeLeft(){
    return cpuBurstTime;
  }

  public void setCpuBurstTime(int i){
    if (i >= cpuTimeLeft){
      cpuBurstTime = cpuTimeLeft;
    } else {
      cpuBurstTime = i;
      previousCpuBurstTime = cpuBurstTime;
    }
  }

  public void setioBurstTime(){
    ioBurstTime = previousCpuBurstTime * ioBurst;
  }

  public int getioBurstTimeLeft(){
    return ioBurstTime;
  }

  public int getTotalTime(){
    return totalTime;
  }

  public int getWaitTime(){
    return waitTime;
  }

  public int getioTime(){
    return ioTime;
  }

  public boolean blockProcess(){

    ioBurstTime--;
    if (ioBurstTime == 0){
      ioBurstTime = -1;
      return true;
    }
    return false;

  }

  public boolean runProcess(){

    cpuBurstTime--;
    cpuTimeLeft--;

    if (cpuBurstTime == 0 || cpuTimeLeft == 0){
      cpuBurstTime = -1;
      return true;
    }
    return false;
  }

  public void setPenaltyRatio(int currentCycle){
    penaltyRatio = (float)(currentCycle - this.arrivalTime)/Math.max(1, this.cpuTime - this.cpuTimeLeft);
  }

  public float getPenaltyRatio(){
    return penaltyRatio;
  }

  public void incrementTime(){
    totalTime ++;

    if (this.state == BLOCKED){
      ioTime++;
    } else if (this.state == READY){
      waitTime++;
    }

  }

  public void printProcessData(){
    System.out.println("\t(A, B, C, M) = (" + arrivalTime + ", " + cpuBurst + ", " + cpuTime + ", " + ioBurst + ")");
    System.out.println("\tFinishing Time: " + totalTime);
    System.out.println("\tTurnaround Time: " + (totalTime - arrivalTime));
    System.out.println("\tI/O Time: " + ioTime);
    System.out.println("\tWaiting Time: " + waitTime);
  }
}
