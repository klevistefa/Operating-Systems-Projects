public class Instruction{
  //A class that represents one line of input.
  private int instruction; //instruction code
  private int resource; //resource used
  private int amount; //units of resource used
  private int delay;

  //note that task number is not tracked here but at the Task class

  public Instruction(){
    this.instruction = -1;
    this.resource = 0;
    this.amount = -1;
    this.delay = 0;
  }

  public int getInstruction(){
    return instruction;
  }

  public int getResource(){
    return resource;
  }

  public int getAmount(){
    return amount;
  }

  public int getDelay(){
    return delay;
  }

  public void setInstruction(int instruction){
    this.instruction = instruction;
  }

  public void setResource(int resource){
    this.resource = resource;
  }

  public void setAmount(int amount){
    this.amount = amount;
  }

  public void setDelay(int delay){
    this.delay = delay;
  }

}
