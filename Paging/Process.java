/*Class that represents a process*/
public class Process{
  //Reference probabilites
  private float A;
  private float B;
  private float C;

  //Data needed for producing the outputs
  private int size;
  private int refNum;
  private int previousRef;
  private int faults;
  private int evictions;
  private int residency;
  private int currentRef;

  //Flags that tell us about states of a process
  private boolean firstTimeReferenced;
  private boolean isFinished;

  public Process(float A, float B, float C, int size, int refNum){
    this.A = A;
    this.B = B;
    this.C = C;
    this.size = size;
    this.refNum = refNum;

    firstTimeReferenced = true;
    isFinished = false;
    previousRef = Integer.MIN_VALUE;
    faults = 0;
    evictions = 0;
    residency = 0;
  }

  public float getA(){
    return A;
  }

  public float getB(){
    return B;
  }

  public float getC(){
    return C;
  }

  public int getSize(){
    return size;
  }

  public int getRefNum(){
    return refNum;
  }

  public int getPreviousRef(){
    return previousRef;
  }

  public int getFaults(){
    return faults;
  }

  public int getEvictions(){
    return evictions;
  }

  public int getResidency(){
    return residency;
  }

  public int getCurrentRef() {
    return currentRef;
  }

  public boolean isFirstTimeReference(){
    return firstTimeReferenced;
  }

  public boolean isFinished(){
    return isFinished;
  }

  public boolean decrementRef(){
    refNum--;
    if(refNum > 0){
      return false;
    }
    isFinished = true;
    return true;
  }

  public void setFirstTimeReferencedFalse(){
    firstTimeReferenced = false;
  }

  public void setPreviousRef(int ref){
    previousRef = ref;
  }

  public void incrementFaults(){
    faults++;
  }

  public void incrementEvictions(){
    evictions++;
  }

  public void setResidency(int res){
    residency += res;
  }

  public void setCurrentRef(int ref) {
    currentRef = ref;
  }
}
