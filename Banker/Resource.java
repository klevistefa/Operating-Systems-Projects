public class Resource{
  //A resource class that represents a type of resource
  private int total; //total units of the resource
  private int amountLeft; //total units available

  public Resource(){
    this.total = 0;
    this.amountLeft = 0;
  }

  public int getTotal(){
    return this.total;
  }

  public void setTotal(int total){
    this.total = total;
  }

  public int getAmountLeft(){
    return this.amountLeft;
  }

  public void setAmountLeft(int amount){
    this.amountLeft = amount;
  }

}
