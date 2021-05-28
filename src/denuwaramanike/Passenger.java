package denuwaramanike;

public class Passenger {
    private String firstName;
    private int seatNumber;
    private int secondsInQueue;

    public Passenger(){
        super();
        this.firstName=null;
        this.secondsInQueue=0;
        this.seatNumber=0;

    }

    public String getName(){
        return firstName;
    }

    public void setName(String name){
        this.firstName=name;
    }

    public void setSeatNumber(int seatNumber){
        this.seatNumber=seatNumber;
    }

    public int getSeatNumber(){
        return seatNumber;
    }

    //********Setter method to set a passenger's waiting time******************
    public void setSecondsInQueue(int sec) {
        this.secondsInQueue = sec;
    }

    //********Getter method to get a passenger's waiting time******************
    public int getSecondsInQueue(){
        return secondsInQueue;
    }
}
