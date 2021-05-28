package denuwaramanike;

import java.util.ArrayList;

public class PassengerQueue {
    static final int SEATING_CAPACITY=42;
    private static Passenger[] queueArray=new Passenger[SEATING_CAPACITY]; //Passenger Array for store train queue passengers
    private int first;
    private int last;
    private int maxStayInQueue;
    private int maxLength;      //Train Queue Maximum length
    private int currentLength;  //Train queue current length

    public PassengerQueue(){
        maxStayInQueue=0;
        first=-1;
        last=-1;
        currentLength=0;
        maxLength=42;       //Maximum Length of the Train Queue
    }
    //**********Create circular Array and add a Passenger to the end of the queue*********************
    public void add(Passenger next){
        if (isFull()){
            System.out.println("Train Queue is full. Unable to add more passengers .");
        }else {
            last=(last+1)%SEATING_CAPACITY;
            queueArray[last]=next;
            currentLength++;
            if (first==-1){
                first=last;
            }
        }
    }
    //************Create circular Array and remove a Passenger from the front of the queue*************
    public Passenger remove(){
        Passenger removePassenger = new Passenger();
        removePassenger=queueArray[first];
        queueArray[first]=null;
        first=(first+1)%SEATING_CAPACITY;
        currentLength--;

        return removePassenger;
    }
    //*************Check train queue is empty or not*********************
    public boolean isEmpty(){
        return (currentLength==0);
    }

    //*************Check train queue is full or not*********************
    public boolean isFull(){
        return (currentLength==SEATING_CAPACITY);
    }

    //************Getter method to return train queue current length****************
    public int getCurrentLength(){
        return currentLength;
    }

    //*****************Return temporary ArrayList for Current train queue passengers***********
    public ArrayList<Passenger> displayTrainQueue(){
        ArrayList<Passenger> temporaryQueueList =new ArrayList<>();
        for (int i=0;i<queueArray.length;i++){
            if (queueArray[i]!=null){
                temporaryQueueList.add(queueArray[i]);
            }
        }
        return temporaryQueueList;
    }
    //***********Method to set delete passenger index value as null*******************
    public void deletePassenger(int deleteSeatNumber) {
        boolean deleteCondition=false;
        for (int i = 0; i < queueArray.length; i++) {
            if (queueArray[i]!=null){
                if (deleteSeatNumber==queueArray[i].getSeatNumber()){
                    System.out.println("Seat number "+queueArray[i].getSeatNumber()+" passenger successfully deleted from Train Queue .");
                    queueArray[i]=null;
                    deleteCondition=true;
                }
            }
        }
        if (deleteCondition!=true){
            System.out.println("This seat number is not in Train Queue . Please recheck seat number. ");
        }
    }
    //********************Re-order Circular train queue after delete a passenger from the Train queue**************
    public void reorderTrainQueueAfterDelete(){
        Passenger swapPassenger;
        for (int i=0;i<queueArray.length-1;i++){
            for (int j=0;j<queueArray.length-i-1;j++){
                if (queueArray[j]==null){
                    swapPassenger=queueArray[j+1];
                    queueArray[j+1]=queueArray[j];
                    queueArray[j]=swapPassenger;
                }
            }
        }
        //Check Train queue is empty
        int countNullSlot=0;
        for (int x=0;x<queueArray.length;x++){
            if (queueArray[x]==null){
                countNullSlot++;
            }
        }

        //If Train Queue is empty then re set first and last values
        if (countNullSlot==queueArray.length){
            first=-1;
            last=-1;
            currentLength=0;
        }

        //If Train Queue is not empty , then set last object index+1  as last
        for (int y=0;y<queueArray.length-1;y++){
            if (queueArray[y]!=null){
                last=y+1;
                currentLength=y+1;
            }
        }
    }

    //****************Method to reset train queue last and first index values******************
    public void resetTrainQueue(){
        first=-1;
        last=-1;
        currentLength=0;
    }
    //*****************Setter Method to set total waiting time in queue************************
    public void setMaxStayInQueue(int maxStayInQueue) {
        this.maxStayInQueue = maxStayInQueue;
    }
}
