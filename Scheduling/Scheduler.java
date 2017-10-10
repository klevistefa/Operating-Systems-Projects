import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;

public class Scheduler{

  private static Scanner scanner;
  private static Scanner randomScanner;
  private static boolean verbose;

  private static final int FCFS = 0;
  private static final int RR = 1;
  private static int quantum = 2;
  private static final int SJF = 2;
  private static final int HPRN = 3;

  private static ArrayList<Process> processes;
  private static ArrayList<Process> readyProcesses;
  private static ArrayList<Process> blockedProcesses;
  private static ArrayList<Process> finishedProcesses;
  private static ArrayList<Process> allProcesses; //used to print out all the info needed;

  private static Process runningProcess;
  private static int cpuCycle;
  private static int totalBlockedTime;


  public static void main(String[] args) {

    processes = new ArrayList<Process>();
    readyProcesses = new ArrayList<Process>();
    blockedProcesses = new ArrayList<Process>();
    finishedProcesses = new ArrayList<Process>();
    allProcesses = new ArrayList<Process>();

    String fileInput = "";

    if (args.length > 1){
      if (args[0].equals("--verbose")){
        verbose = true;
      } else {
        System.out.printf("Error: No command found for %s.\n", args[0]);
        System.exit(1);
      }
      fileInput = args[1];
    } else {
      fileInput = args[0];
    }

    for (int i = 1; i < 2; i++){
      resetData(fileInput);
      if (verbose) {
        System.out.println("This detailed printout gives the state and remaning burst for each process.\n");
        printVerbose(i == RR ? true : false); //check if scheduler is Round Robin
      }
      while (processes.size() > 0 || blockedProcesses.size() > 0 || readyProcesses.size() > 0 || runningProcess != null){
        // switch (i){
        //   case FCFS:
        //   FirstComeFirstServe();
        //   break;
        //
        //   case RR:
        //   RoundRobin();
        //   break;
        //
        //   case SJF:
        //   ShortestJobFirst();
        //   break;
        //
        //   case HPRN:
        //   HighestPenaltyRatioNext();
        //   break;
        // }
        RoundRobin();
      }
      printData(i);
    }


  }

  public static void FirstComeFirstServe(){

    //if there are no current running processes and there are processes in ready state
    if (runningProcess == null && readyProcesses.size()>0){
      runningProcess = readyProcesses.get(0); //run the first process in ready state
      if (runningProcess.getCpuBurstTimeLeft() == -1){
        runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst())); //set its CpuBurstTime
      }
      runningProcess.setState(Process.RUNNING); //set state to running
      readyProcesses.remove(0); //remove it from the ready processes
    }

    //check all the processes that their arrival time corresponds to the CPU's current cpuCycle
    for (int i = 0; i < processes.size(); i++){
      if (processes.get(i).getArrivalTime() == cpuCycle){
        if (runningProcess == null){ //if no current running process, run the first process that came
          runningProcess = processes.get(i);
          runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst())); //set its CPU CpuBurstTime
          runningProcess.setState(Process.RUNNING); //set state to running
        } else {
          readyProcesses.add(processes.get(i)); //else add the process to the ready list
          processes.get(i).setState(Process.READY); //set status to ready
        }
        processes.remove(i); //remove process from the processes.
        i--;
      }
    }

    incrementTime();
    if (verbose) {printVerbose(false);} //false because not Round Robin

    ArrayList<Process> temp = new ArrayList<Process>(); //temporary list of processes that finished their I/O time
    for (int i = 0; i < blockedProcesses.size(); i++){
       //keep track of processes that go from blocked to ready
      if (blockedProcesses.get(i).blockProcess()){
        temp.add(blockedProcesses.get(i));
        blockedProcesses.get(i).setState(Process.READY); //if blocked time is zero process become ready for the CPU
        blockedProcesses.remove(i);
        i--;
      }
    }
    //sort ready processes depending on their arrival time
    sortProcesses(temp);

    //Add them to the actual ready list of processes
    for (int i = 0; i < temp.size(); i++){
      readyProcesses.add(temp.get(i));
      temp.remove(i);
      i--;
    }
    temp.clear();

    if (runningProcess != null){
      if (runningProcess.runProcess()){ //check if a process has finished running or if his time has ended.
        if (runningProcess.getCpuTimeLeft() == 0){//process complete
          finishedProcesses.add(runningProcess);
          runningProcess.setState(Process.TERMINATED);
          runningProcess = null;
        } else { //process CPU burst time is 0;
          blockedProcesses.add(runningProcess);
          if (runningProcess.getioBurstTimeLeft() == -1){ // I/O burst is complete
            runningProcess.setioBurstTime();; //set the blocked time
          }
          runningProcess.setState(Process.BLOCKED);
          runningProcess = null;
        }
      }
    }
  }

  public static void RoundRobin(){

    //if there are no current running processes and there are processes in ready state
    if (runningProcess == null && readyProcesses.size()>0){
      runningProcess = readyProcesses.get(0); //run the first process in ready state
      if (runningProcess.getCpuBurstTimeLeft() == -1){
        runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst())); //set its CpuBurstTime
      }
      runningProcess.setState(Process.RUNNING); //set state to running
      readyProcesses.remove(0); //remove it from the ready processes
    }

    //check all the processes that their arrival time corresponds to the CPU's current cpuCycle
    for (int i = 0; i < processes.size(); i++){
      if (processes.get(i).getArrivalTime() == cpuCycle){
        if (runningProcess == null){ //if no current running process, run the first process that came
          runningProcess = processes.get(i);
          runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst())); //set its CPU CpuBurstTime
          runningProcess.setState(Process.RUNNING); //set state to running
        } else {
          readyProcesses.add(processes.get(i)); //else add the process to the ready list
          processes.get(i).setState(Process.READY); //set status to ready
        }
        processes.remove(i); //remove process from the processes.
        i--;
      }
    }

    incrementTime();
    if (verbose) {printVerbose(true);}

    ArrayList<Process> temp = new ArrayList<Process>(); //temporary list of processes that finished their I/O time

    for (int i = 0; i < blockedProcesses.size(); i ++){
      if (blockedProcesses.get(i).blockProcess()){ //process done with I/O time
        temp.add(blockedProcesses.get(i));
        blockedProcesses.get(i).setState(Process.READY);
        blockedProcesses.remove(i);
        i--;
      }
    }

    if (runningProcess != null){
      if (runningProcess.runProcess()){
        if (runningProcess.getCpuTimeLeft() == 0){ //process complete
          runningProcess.setState(Process.TERMINATED);
          finishedProcesses.add(runningProcess);
          runningProcess = null;
        } else { //process CPU burst time done; Switch to blocked
          blockedProcesses.add(runningProcess);
          if (runningProcess.getioBurstTimeLeft() == -1){
            runningProcess.setioBurstTime();; //set the new IO burst time
          }
          runningProcess.setState(Process.BLOCKED);
          runningProcess = null;
        }
        quantum = 2; //reset quantum for the new process to run
      } else {
        quantum --; //decrement quantum time;

        if (quantum == 0){ //current running process' cpu time time share is done
          if (readyProcesses.size() > 0){
            runningProcess.setState(Process.READY);
            temp.add(runningProcess); //add it back to the temporary list of ready processes
            runningProcess = readyProcesses.get(0); //switch to next ready process in line
            runningProcess.setState(Process.RUNNING);

            if (runningProcess.getCpuBurstTimeLeft() == -1){ //if process complete previous cpu burst time
              runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst())); //assign new cpu burst time
            }
            readyProcesses.remove(0); //take the current running process from the ready list
            sortProcesses(temp);

            //set everything back to ready processes
            for (int i = 0; i < temp.size(); i++){
              readyProcesses.add(temp.get(i));
              temp.remove(i);
              i--;
            }
            quantum = 2; //reset quantum
          } else if (temp.size() > 0){

            sortProcesses(temp); //sort the ready processes in temp
            runningProcess.setState(Process.READY); //context switch
            temp.add(runningProcess); //add it back to temp list of ready processes
            runningProcess = temp.get(0); //run the first process on the sorted temp list
            runningProcess.setState(Process.RUNNING);

            if (runningProcess.getCpuBurstTimeLeft() == -1){ //if process complete previous cpu burst time
              runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst())); //assign new cpu burst time
            }

            temp.remove(0);
            sortProcesses(temp);

            //add processes back to the ready list
            for (int i = 0; i < temp.size(); i++){
              readyProcesses.add(temp.get(i));
              temp.remove(i);
              i--;
            }
            quantum = 2; //reset quantum
          } else {
            quantum = 2; //reset quantum
          }
        }
      }
    }

    if (temp.size()>0){
      sortProcesses(temp);

      //add processes back to the ready list
      for (int i = 0; i < temp.size(); i++){
        readyProcesses.add(temp.get(i));
        temp.remove(i);
        i--;
      }
    }

    // if (runningProcess == null && readyProcesses.size() == 1){
    //   runningProcess = readyProcesses.get(0);
    //   runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst()));
    //   runningProcess.setState(Process.READY);
    //   readyProcesses.remove(0);
    // }

  }

  public static void ShortestJobFirst(){

    //get all the processes that arrive in this time and set them as ready
    for (int i = 0; i < processes.size(); i++){
      if (processes.get(i).getArrivalTime() == cpuCycle){
        readyProcesses.add(processes.get(i));
        processes.get(i).setState(Process.READY);
        processes.remove(i);
        i--;
      }
    }

    //sort the readyProcesses based on their CPU time
    Collections.sort(readyProcesses, new Comparator<Process>(){
      public int compare(Process p1, Process p2){
        return p1.getCpuTimeLeft() - p2.getCpuTimeLeft();
      }
    });

    //No current runningProcess and there are processes waiting for the CPU
    if (runningProcess == null && readyProcesses.size() > 0){
      runningProcess = readyProcesses.get(0);
      if (runningProcess.getCpuBurstTimeLeft() == -1){ //if previous run time was complete
        runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst())); //get the new CPU burst
      }
      runningProcess.setState(Process.RUNNING);
      readyProcesses.remove(0);
    }

    incrementTime();
    if (verbose) {printVerbose(false);}

    ArrayList<Process> temp = new ArrayList<Process>();
    //get processes whose I/O time is complete
    for (int i = 0; i < blockedProcesses.size(); i++){
      if (blockedProcesses.get(i).blockProcess()){
        temp.add(blockedProcesses.get(i));
        blockedProcesses.get(i).setState(Process.READY);
        blockedProcesses.remove(i);
        i--;
      }
    }

    sortProcesses(temp);

    //add them in sorted order to the readyProcesses
    for(int i = 0; i < temp.size(); i++){
      readyProcesses.add(temp.get(i));
      temp.remove(i);
      i--;
    }

    temp.clear();

    if (runningProcess != null){
      if (runningProcess.runProcess()){ //check if a process has finished running or if his time has ended.
        if (runningProcess.getCpuTimeLeft() == 0){//process complete
          finishedProcesses.add(runningProcess);
          runningProcess.setState(Process.TERMINATED);
          runningProcess = null;
        } else { //process CPU burst time is 0;
          blockedProcesses.add(runningProcess);
          if (runningProcess.getioBurstTimeLeft() == -1){ // I/O burst is complete
            runningProcess.setioBurstTime();; //set the blocked time
          }
          runningProcess.setState(Process.BLOCKED);
          runningProcess = null;
        }
      }
    }
  }

  public static void HighestPenaltyRatioNext(){

    for (int i = 0; i < processes.size(); i++){
      if (processes.get(i).getArrivalTime() == cpuCycle){
        readyProcesses.add(processes.get(i));
        processes.get(i).setState(Process.READY);
        processes.remove(i);
        i--;
      }
    }

    for (int i = 0; i < readyProcesses.size(); i++){
      readyProcesses.get(i).setPenaltyRatio(cpuCycle);
    }

    Collections.sort(readyProcesses, new Comparator<Process>(){
      public int compare(Process p1, Process p2){
        if (p2.getPenaltyRatio() > p1.getPenaltyRatio()){
          return 1;
        } else if (p2.getPenaltyRatio() < p1.getPenaltyRatio()){
          return -1;
        } else {
          return p1.getArrivalNumber() - p2.getArrivalNumber();
        }

      }
    });

    if (runningProcess == null && readyProcesses.size() > 0){
      runningProcess = readyProcesses.get(0);
      if (runningProcess.getCpuBurstTimeLeft() == -1){
        runningProcess.setCpuBurstTime(randomOS(runningProcess.getCpuBurst()));
      }
      runningProcess.setState(Process.RUNNING);
      readyProcesses.remove(0);
    }

    incrementTime();
    if (verbose) {printVerbose(false);}

    //ArrayList<Process> temp = new ArrayList<Process>();
    for (int i = 0; i < blockedProcesses.size(); i++){
       //keep track of processes that go from blocked to ready
      if (blockedProcesses.get(i).blockProcess()){
        readyProcesses.add(blockedProcesses.get(i));
        blockedProcesses.get(i).setState(Process.READY); //if blocked time is zero process become ready for the CPU
        blockedProcesses.remove(i);
        i--;
      }
    }

    for (int i = 0; i < readyProcesses.size(); i++){
      readyProcesses.get(i).setPenaltyRatio(cpuCycle);
    }

    Collections.sort(readyProcesses, new Comparator<Process>(){
      public int compare(Process p1, Process p2){
        if (p2.getPenaltyRatio() > p1.getPenaltyRatio()){
          return 1;
        } else if (p2.getPenaltyRatio() < p1.getPenaltyRatio()){
          return -1;
        } else {
          return p1.getArrivalNumber() - p2.getArrivalNumber();
        }

      }
    });


    if (runningProcess != null){
      if (runningProcess.runProcess()){ //check if a process has finished running or if his time has ended.
        if (runningProcess.getCpuTimeLeft() == 0){//process complete
          finishedProcesses.add(runningProcess);
          runningProcess.setState(Process.TERMINATED);
          runningProcess = null;
        } else { //process CPU burst time is 0;
          blockedProcesses.add(runningProcess);
          if (runningProcess.getioBurstTimeLeft() == -1){ // I/O burst is complete
            runningProcess.setioBurstTime();; //set the blocked time
          }
          runningProcess.setState(Process.BLOCKED);
          runningProcess = null;
        }
      }
    }
  }

  public static void getScanners(String fileName){

    File randomNumbers = new File("./random-numbers.txt");
    File input = new File(fileName);

    try{
      scanner = new Scanner(input);
      randomScanner = new Scanner(randomNumbers);
    } catch (Exception e){
      e.printStackTrace();
    }

  }

  public static int randomOS(int value){
    int num = randomScanner.nextInt();
    System.out.println("Find burst when choosing ready process to run " + num);
    return 1 + (num % value);
  }

  public static void getProcesses(){

    int n = scanner.nextInt();
    System.out.print("\nThe original input was: " + n + " ");
    for (int i = 0; i < n; i ++){
      while(!scanner.hasNextInt()){scanner.next();}
      processes.add(new Process(i, scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.nextInt()));
      System.out.print("(" + processes.get(i).getArrivalTime() + " " + processes.get(i).getCpuBurst() + " " +
      processes.get(i).getCpuTimeLeft() + " " + processes.get(i).getioBurst() + ") ");
    }

    //sort them by their arrival time
    sortProcesses(processes);

    System.out.print("\nThe (sorted) input is: " + n + " ");
    for (int i = 0; i < n; i++){
      //add all proceses to the allProcesses list used to print the data
      allProcesses.add(processes.get(i));
      System.out.print("(" + processes.get(i).getArrivalTime() + " " + processes.get(i).getCpuBurst() + " " +
      processes.get(i).getCpuTimeLeft() + " " + processes.get(i).getioBurst() + ") ");
    }
    System.out.println("\n");
  }

  public static void incrementTime(){
    //increment time for all processes
    for (int i = 0 ; i < processes.size(); i++){
      processes.get(i).incrementTime();
    }

    for (int i = 0; i < blockedProcesses.size(); i++){
      blockedProcesses.get(i).incrementTime();
    }
    //increase blocked time if there are blocked processes
    if (blockedProcesses.size() > 0){
      totalBlockedTime++;
    }

    for (int i = 0; i < readyProcesses.size(); i++){
      readyProcesses.get(i).incrementTime();
    }

    //increment runningProcess time;
    if (runningProcess != null){
      runningProcess.incrementTime();
    }

    //add cpuCycle
    cpuCycle++;

  }

  public static void printData(int scheduler){
    //Method to print all the data required

    switch (scheduler){
      case FCFS: System.out.println("\nThe scheduling algorithm used was First Come First Served."); break;
      case RR: System.out.println("\nThe scheduling algorithm used was Round Robin with quantum 2."); break;
      case SJF: System.out.println("\nThe scheduling algorithm used was Shortest Job First."); break;
      case HPRN: System.out.println("\nThe scheduling algorithm used was Highest Penalty Ratio Next"); break;
    }

    int finishingTime = 0;
    int waitingTime = 0;
    int CPUtime = 0;
    int turnaroundTime = 0;

    Process p;
    for (int i = 0; i < allProcesses.size(); i++){
      p = allProcesses.get(i);
      System.out.println("\nProcess " + i + ":");
      p.printProcessData();
      if (finishingTime < p.getTotalTime()){
        finishingTime = p.getTotalTime(); //get the time of the last finished process as the total time
      }

      CPUtime += p.getTotalTime() - p.getArrivalTime() - p.getioTime() - p.getWaitTime();
      waitingTime += p.getWaitTime();
      turnaroundTime += p.getTotalTime() - p.getArrivalTime();
    }

    System.out.println("\nSummary Data:");
    System.out.println("\tFinishing Time: " + finishingTime);
    System.out.println("\tCPU Utilization: " + (float)CPUtime/finishingTime);
    System.out.println("\tI/O Utilization: " + (float)totalBlockedTime/finishingTime);
    System.out.println("\tThroughput: " + 100*(float)allProcesses.size()/finishingTime + " processes per hundred cycles");
    System.out.println("\tAverage turnaround time: " + (float)turnaroundTime/allProcesses.size());
    System.out.println("\tAverage waiting time: " + (float)waitingTime/allProcesses.size());
  }

  public static void resetData(String fileInput){
    cpuCycle = 0;
    totalBlockedTime = 0;
    runningProcess = null;
    processes.clear();
    allProcesses.clear();
    finishedProcesses.clear();
    blockedProcesses.clear();
    getScanners(fileInput);
    getProcesses();
  }

  public static void printVerbose(boolean RRflag){
    System.out.printf("Before cycle %5d: ", cpuCycle);
    for (int i = 0; i < allProcesses.size(); i++){
      switch(allProcesses.get(i).getState()){

        case Process.UNSTARTED:
          System.out.printf("%12s%3d", "unstarted", 0); break;
        case Process.READY:
          System.out.printf("%12s%3d", "ready", 0); break;
        case Process.BLOCKED:
          System.out.printf("%12s%3d", "blocked", allProcesses.get(i).getioBurstTimeLeft()); break;
        case Process.RUNNING:
          System.out.printf("%12s%3d", "running", RRflag ? Math.min(allProcesses.get(i).getCpuBurstTimeLeft(), quantum) : allProcesses.get(i).getCpuBurstTimeLeft());
          break;
        case Process.TERMINATED:
          System.out.printf("%12s%3d", "terminated", 0); break;
      }
    }
    System.out.println(".");
  }

  public static void sortProcesses(ArrayList<Process> processes){
    //sorts processes by their arrival time
    Collections.sort(processes, new Comparator<Process>(){
      public int compare(Process p1, Process p2){
        if (p1.getArrivalTime() != p2.getArrivalTime()){
          return p1.getArrivalTime() - p2.getArrivalTime();
        } else {
          return p1.getArrivalNumber() - p2.getArrivalNumber();
        }
      }
    });
  }

}
