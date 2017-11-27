/* Class that represents a frame;
   It holds the cycle, the procees and the value
*/
public class Frame{
  private int cycle;
  private int process;
  private int value;

  public Frame(int cycle, int process, int value){
    this.cycle = cycle;
    this.process = process;
    this.value = value;
  }

  public int getCycle(){
    return cycle;
  }

  public int getProcess(){
    return process;
  }

  public int getValue(){
    return value;
  }

  public void setCycle(int cycle){
    this.cycle = cycle;
  }

  public void setProcess(int process){
    this.process = process;
  }

  public void setValue(int value){
    this.value = value;
  }
}
