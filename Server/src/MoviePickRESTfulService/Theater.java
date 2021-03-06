package MoviePickRESTfulService;

public class Theater{
  private String theaterName;
  private String theaterAddress;
  
  public Theater(){
    this.theaterName = "";
    this.theaterAddress = "";
  }
  
  public Theater(String name, String address){
    this.theaterName = name;
    this.theaterAddress = address;
  }
  
  public String getTheaterName(){
    return this.theaterName;
  }
  
  public void setTheaterName(String name){
    this.theaterName = name;
  }
  
  public String getTheaterAddress(){
    return this.theaterAddress;
  }
  
  public void setTheaterAddress(String address){
    this.theaterAddress = address;
  }
  
  public boolean equals(Theater theater){
    if(this.theaterName.toLowerCase().compareTo(theater.getTheaterName().toLowerCase()) == 0 && this.theaterAddress.toLowerCase().compareTo(theater.getTheaterAddress().toLowerCase()) == 0)
      return true;
    return false;
  }
}
