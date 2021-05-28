package denuwaramanike;

import com.mongodb.*;
import com.mongodb.client.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainStation extends Application {
    static final int SEATING_CAPACITY=42;
    private Passenger[] waitingRoom=new Passenger[SEATING_CAPACITY];
    ArrayList<Passenger> waitingRoomPassengerList = new ArrayList<>();  //temporary ArrayList For waiting room
    private PassengerQueue trainQueue=new PassengerQueue();          //get PassengerQueue Class to TrainStation Class
    ArrayList<String> seatReservationDetailList=new ArrayList<>();  //ArrayList to store seat reservation details of local date
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //*****Call method to select root and load seat reservation data
        loadReservationDetail();
        //*****Call method to check passenger reservation and add them to the waiting room
        System.out.println("check passenger seat reservation and board them to the waiting room .");
        System.out.println();
        checkPassengerDetail();
        System.out.println();
        String option="";
        while (!option.equalsIgnoreCase("q")) {
            Scanner input = new Scanner(System.in);
            System.out.println();
            System.out.println("DENUWARA MANIKE TRAIN : Train Station Main Menu");
            System.out.println("..........................................................");
            System.out.println("Enter \"A\" to add a passenger to the Train Queue .");
            System.out.println("Enter \"V\" to view Train Queue .");
            System.out.println("Enter \"D\" to delete a passenger from the Train Queue .");
            System.out.println("Enter \"S\" to store Train Queue data.");
            System.out.println("Enter \"L\" to load data from the file .");
            System.out.println("Enter \"R\" to run the simulation and Produce report .");
            System.out.println("Enter \"Q\" to exit the program .");
            System.out.println("..........................................................");
            System.out.println("What is your opinion : ");
            option = input.next().toLowerCase();
            switch (option) {
                case "a":
                    addPassengerToTrainQueue();
                    break;
                case "v":
                    displayWaitingRoom();       //Call method to display waiting room
                    viewTrainQueue();
                    break;
                case "d":
                    deletePassengerFromTrainQueue();
                    break;
                case "s":
                    storeTrainQueueData();
                    break;
                case "l":
                    loadTrainQueueData();
                    break;
                case "r":
                    runTheSimulation();
                    break;
                case "q":
                    System.out.println("End of the program");
                    break;
                default:
                    System.out.println("Invalid Input .Try again");
                    break;

            }
        }
    }

    //****Load train queue data from database****
    private void loadTrainQueueData() {
        ArrayList<Passenger> loadedList = new ArrayList<>();   //Temporary list to load data
        trainQueue.resetTrainQueue();     //Before add load data the train queue re set queue array index and length
        java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
        MongoClient client = MongoClients.create("mongodb://LocalHost:27017");        //Create mongo client
        MongoDatabase database = client.getDatabase("DenuwaraManikeTrainStation");  //accessing the database
        MongoCollection trainStationDataCollection = database.getCollection("trainStationDataCollection");//create table
        FindIterable<Document> load = trainStationDataCollection.find();

        for (Document record : load) {
            Passenger loadPassenger = new Passenger();
            loadPassenger.setName(record.get("passengerName").toString());
            String seat = (record.get("seatNumber").toString());       //First get seat number to String variable
            int seatNo = Integer.parseInt(seat);                       //Then convert it to integer data type
            loadPassenger.setSeatNumber(seatNo);                       //set seat number
            loadedList.add(loadPassenger);                             //Add object to the temporary created ArrayList
        }
        //***ArrayList to get current train queue details***
        ArrayList<Passenger> storedTrainQueue = trainQueue.displayTrainQueue();
        System.out.println("Train queue current size  :" + storedTrainQueue.size());
        //*********Compare loaded passenger list and current train queue list***********
        if (storedTrainQueue.size()==0) {
            System.out.println("Train Queue is empty");
        } else {
            //***Add passengers to the train queue which in both load list and train queue***
            for (int x = 0; x < loadedList.size(); x++) {
                if (loadedList.get(x).getSeatNumber() == storedTrainQueue.get(0).getSeatNumber()) {
                    trainQueue.add(loadedList.get(x));
                    storedTrainQueue.remove(0);
                }
            }
            //****Add passengers to the train queue which are not in database*****
            for (int y = 0; y < storedTrainQueue.size(); y++) {
                trainQueue.add(storedTrainQueue.get(y));
            }
        }
    }

    //****Store train queue data to the database****
    private void storeTrainQueueData() {
        //***Create ArrayList to get current train queue data***
        ArrayList<Passenger> passengerListForStore=new ArrayList<>();
        passengerListForStore=trainQueue.displayTrainQueue();

        Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
        MongoClient client= MongoClients.create("mongodb://LocalHost:27017");        //Create mongo client
        MongoDatabase database=client.getDatabase("DenuwaraManikeTrainStation");  //accessing the database
        MongoCollection trainStationDataCollection=database.getCollection("trainStationDataCollection"); //create table
        BasicDBObject objectForStorePassenger =new BasicDBObject();
        trainStationDataCollection.drop();
        //******Store data**********
        for (Passenger passengerObject : passengerListForStore){    //Creating Passenger object
            Document document=new Document("subtopic","detail")     //Adding details to the document
                    .append("passengerName",passengerObject.getName())
                    .append("seatNumber",passengerObject.getSeatNumber());

            trainStationDataCollection.insertOne(document);  //Adding document to the Database
        }
        System.out.println("Successfully stored all Train Queue details to the database");
    }

    //****View the train queue with passenger details****
    private void viewTrainQueue() {
        //****Create stage to view details****
        Stage stage=new Stage();
        stage.setTitle("Denuwara Manike Train - Train Queue View");
        AnchorPane anchorPane=new AnchorPane();

        Image logo= new Image("file:trainLogo.png");   //****Insert image to the window****
        ImageView viewImage=new ImageView();
        viewImage.setImage(logo);
        Label titleLabel=new Label("     DENUWARA MANIKE TRAIN",viewImage);
        titleLabel.setLayoutX(50);   //****set position of scene's title****
        titleLabel.setLayoutY(20);
        titleLabel.setStyle("-fx-font-family:Rockwell Extra Bold;-fx-font-size:30"); //****set style to scene's title****

        Label title = new Label("  Train Station Train Queue  ");  //Create title Label
        title.setStyle("-fx-font-family:Segoe UI Black;-fx-font-size:22;-fx-background-color:#A9A9A9");
        title.setLayoutX(50);
        title.setLayoutY(130);

        GridPane viewPane=new GridPane();   //Create GridPane to add buttons
        viewPane.setLayoutX(30);
        viewPane.setLayoutY(170);
        viewPane.setPadding(new Insets(30,20,10,30));
        viewPane.setHgap(10);
        viewPane.setVgap(10);

        //***Create ArrayList to get train queue passenger details
        ArrayList<Passenger> temporaryListToPassengers=new ArrayList<>();
        //***Get train queue passenger details to the created ArrayList
        temporaryListToPassengers=trainQueue.displayTrainQueue();
        Button[] button=new Button[42];
        int trainQueueSize=temporaryListToPassengers.size();
        for (int i=0;i<SEATING_CAPACITY;i++){
            if (trainQueueSize>0){           //create buttons for passengers in train queue
                button[i]=new Button();
                button[i].setStyle("-fx-background-color:#00BFFF;-fx-pref-height:70;-fx-pref-width:100");
                button[i].setText(temporaryListToPassengers.get(i).getName().toUpperCase()+"\nSeat "+String.valueOf(temporaryListToPassengers.get(i).getSeatNumber()));
                trainQueueSize--;
            }else {                         //create empty Buttons for passengers not in train queue
                button[i]=new Button();
                button[i].setStyle("-fx-pref-height:70;-fx-pref-width:100");
                button[i].setText("Empty");
            }
        }
        //************Add created buttons to the GridPane**************************
        int index=0;
        for (int row=0;row<6;row++){
            for (int column=0;column<7;column++){
                viewPane.add(button[index++],row,column);
            }
        }
        Button close=new Button(" CLOSE");
        close.setStyle("-fx-background-color:#bfdce7; -fx-pref-height:40; -fx-pref-width:70;");
        viewPane.add(close,5,10);
        close.setOnAction(event -> stage.close());
        anchorPane.getChildren().addAll(titleLabel,viewPane,title);
        Scene scene=new Scene(anchorPane,900,900);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

    }

    //****Display the train queue****
    private void displayTrainQueue(){
        //***Create stage to display details****
        Stage stage=new Stage();
        stage.setTitle("Denuwara Manike Train - Train Queue");
        AnchorPane anchorPane=new AnchorPane();

        Image logo= new Image("file:trainLogo.png");   //****Insert image to the window****
        ImageView viewImage=new ImageView();
        viewImage.setImage(logo);
        Label titleLabel=new Label("     DENUWARA MANIKE TRAIN",viewImage);
        titleLabel.setLayoutX(30);   //****set position of scene's title****
        titleLabel.setLayoutY(20);
        titleLabel.setStyle("-fx-font-family:Rockwell Extra Bold;-fx-font-size:30");//**set style to scene's title**

        Label title = new Label("  Train Station Train Queue   ");  //Create title Label
        title.setStyle("-fx-font-family:Segoe UI Black;-fx-font-size:22;-fx-background-color:#A9A9A9");
        title.setLayoutX(50);
        title.setLayoutY(130);

        //***Create grid pane***
        GridPane viewPane=new GridPane();
        viewPane.setLayoutX(50);
        viewPane.setLayoutY(170);
        viewPane.setPadding(new Insets(30,20,10,30));
        viewPane.setHgap(10);
        viewPane.setVgap(10);

        //Create ArrayList to get train queue passenger details
        ArrayList<Passenger> temporaryList=new ArrayList<>();
        //Get train queue passenger details to the created ArrayList
        temporaryList=trainQueue.displayTrainQueue();
        Button[] button=new Button[42];
        int trainQueueSize=temporaryList.size();
        for (int i=0;i<SEATING_CAPACITY;i++){
            if (trainQueueSize>0){        //create buttons for passengers in train queue
                button[i]=new Button();
                button[i].setStyle("-fx-background-color:#00BFFF;-fx-pref-height:70;-fx-pref-width:100");
                button[i].setText(temporaryList.get(i).getName().toUpperCase()+"\nSeat "+String.valueOf(temporaryList.get(i).getSeatNumber()));
                trainQueueSize--;
            }else {                      //create empty Buttons for passengers not in waiting room
                button[i]=new Button();
                button[i].setStyle("-fx-pref-height:70;-fx-pref-width:100");
                button[i].setText("No \nPassenger");
                button[i].setVisible(true);
            }
        }
        //****Add created buttons to the GridPane*****
        int index=0;
        for (int row=0;row<6;row++){
            for (int column=0;column<7;column++){
                viewPane.add(button[index++],row,column);
            }
        }
        Button close=new Button(" CLOSE");
        close.setStyle("-fx-background-color:#bfdce7; -fx-pref-height:40; -fx-pref-width:70;");
        viewPane.add(close,5,10);
        close.setOnAction(event -> stage.close());
        anchorPane.getChildren().addAll(titleLabel,viewPane,title);
        Scene scene=new Scene(anchorPane,900,900);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

    }

    //****Display passengers in the waiting room****
    private void displayWaitingRoom(){
        //***create stage to display details***
        Stage stage=new Stage();
        stage.setTitle("Denuwara Manike Train - Waiting Room");
        AnchorPane anchorPane=new AnchorPane();

        Image logo= new Image("file:trainLogo.png");   //****Insert image to the window****
        ImageView viewImage=new ImageView();
        viewImage.setImage(logo);
        Label titleLabel=new Label("     DENUWARA MANIKE TRAIN",viewImage);
        titleLabel.setLayoutX(30);   //****set position of scene's title****
        titleLabel.setLayoutY(20);
        titleLabel.setStyle("-fx-font-family:Rockwell Extra Bold;-fx-font-size:30");//**set style to scene's title**

        Label title = new Label("  Train Station Waiting Room   ");  //Create title Label
        title.setStyle("-fx-font-family:Segoe UI Black;-fx-font-size:22;-fx-background-color:#A9A9A9");
        title.setLayoutX(50);
        title.setLayoutY(130);

        GridPane viewPane=new GridPane();
        viewPane.setLayoutX(50);
        viewPane.setLayoutY(170);
        viewPane.setPadding(new Insets(30,20,10,30));
        viewPane.setHgap(10);
        viewPane.setVgap(10);

        Button[] button=new Button[42];
        int waitingRoomSize=waitingRoomPassengerList.size();
        for (int i=0;i<SEATING_CAPACITY;i++){
            if (waitingRoomSize>0){          //create buttons for passengers in waiting room
                button[i]=new Button();
                button[i].setStyle("-fx-background-color:#00BFFF;-fx-pref-height:70;-fx-pref-width:100");
                button[i].setText(waitingRoomPassengerList.get(i).getName().toUpperCase()+"\nSeat "+String.valueOf(waitingRoomPassengerList.get(i).getSeatNumber()));
                waitingRoomSize--;
            }else {                         //create empty Buttons for passengers not in waiting room
                button[i]=new Button();
                button[i].setStyle("-fx-pref-height:70;-fx-pref-width:100");
                button[i].setVisible(true);
            }
        }
        //*****Add created buttons to the GridPane*****
        int index=0;
        for (int row=0;row<6;row++){
            for (int column=0;column<7;column++){
                viewPane.add(button[index++],row,column);
            }
        }
        Button go=new Button(" GO");
        go.setStyle("-fx-background-color:#bfdce7; -fx-pref-height:40; -fx-pref-width:70;");
        viewPane.add(go,5,10);
        go.setOnAction(event -> stage.close());
        anchorPane.getChildren().addAll(titleLabel,viewPane,title);
        Scene scene=new Scene(anchorPane,900,900);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }

    //****Delete passenger from Train Queue****
    private void deletePassengerFromTrainQueue() {
        try {
            Scanner delete = new Scanner(System.in);
            System.out.println("Enter passenger seat number you want to delete from the train queue : ");
            int deleteSeatNumber = delete.nextInt();
            //***Call method to set delete index as null***
            trainQueue.deletePassenger(deleteSeatNumber);
            //***Call method to reorder the train queue after delete a passenger***
            trainQueue.reorderTrainQueueAfterDelete();

        }catch (Exception e){
            System.out.println("Invalid Input .Try Again ! ");
        }
    }

    //****Boarding passengers and produce summary report****
    private void runTheSimulation() {
        //Create ArrayList to store boarded passenger details
        ArrayList<Passenger> boardedPassenger = new ArrayList<>();
        //Create Array to store Passenger's boarding time
        Integer[] waitingTimeArray = new Integer[trainQueue.getCurrentLength()];
        System.out.println("Train queue length : " + trainQueue.getCurrentLength());
        int countBoardingRound = 0;  //Count Boarding passengers
        int totalWaitingTime = 0;
        double averageWaitingTime;
        int minimumWaitingTime;      //set minimum time took to check boarding ticket
        int maximumWaitingTime;      //set maximum time took to check boarding ticket
        if (trainQueue.isEmpty()) {
            System.out.println("Train Queue is empty. No  passengers to board train .");
        } else {
            for (int i = trainQueue.getCurrentLength(); i > 0; i--) {
                boardedPassenger.add(trainQueue.remove());   //add checked passenger to the boarded list
                System.out.println("Boarded Passenger name :  " + boardedPassenger.get(countBoardingRound).getName().toUpperCase());
                int boardingTimeInSecond = ((int) (Math.random() * ((6 - 1) + 1)) + 1) + ((int) (Math.random() * ((6 - 1) + 1)) + 1) + ((int) (Math.random() * ((6 - 1) + 1)) + 1);
                //****************Remove Passenger from train queue with respect to the time*****************
                try {
                    Thread.sleep(boardingTimeInSecond*1000);         //set time intervals
                } catch (InterruptedException e) {
                    System.out.println("Something went wrong");
                }
                totalWaitingTime = totalWaitingTime + boardingTimeInSecond; //Increment total waiting time
                //Set passenger waiting time
                boardedPassenger.get(countBoardingRound).setSecondsInQueue(boardingTimeInSecond);
                waitingTimeArray[countBoardingRound] = boardingTimeInSecond;    //Add waiting time to the array
                countBoardingRound++;                                           //Increment boarding passenger by one
                System.out.println("It took " + boardingTimeInSecond + " seconds to check and  board a Passenger to the Train Queue .");
            }
            trainQueue.setMaxStayInQueue(totalWaitingTime);                      //set total waiting in queue

            //**sort time arrayList and find maximum waiting time and minimum waiting time**
            int sizeOfTimeArray = waitingTimeArray.length;
            for (int i = 0; i < sizeOfTimeArray; i++) {
                for (int j = 0; j < sizeOfTimeArray - i - 1; j++) {
                    if (waitingTimeArray[j] > waitingTimeArray[j + 1]) {
                        //swap waitingTimeArray.get(j) and waitingTimeArray.get(j+1)
                        int temporary = waitingTimeArray[j];
                        waitingTimeArray[j] = waitingTimeArray[j + 1];
                        waitingTimeArray[j + 1] = temporary;
                    }
                }
            }
            averageWaitingTime = ((totalWaitingTime / (sizeOfTimeArray)) * 100) / 100;
            minimumWaitingTime = waitingTimeArray[0];
            maximumWaitingTime = waitingTimeArray[sizeOfTimeArray - 1];

            //*****create stage to display boarding summary details*****
            Stage stage = new Stage();
            stage.setTitle("Denuwara Manike Train - Passenger Boarding Details");
            AnchorPane anchorPane = new AnchorPane();

            Image logo = new Image("file:trainLogo.png");   //****Insert image to the window****
            ImageView viewImage = new ImageView();
            viewImage.setImage(logo);
            Label titleLabel = new Label("     DENUWARA MANIKE TRAIN", viewImage);
            titleLabel.setLayoutX(30);   //****set position of scene's title****
            titleLabel.setLayoutY(20);
            titleLabel.setStyle("-fx-font-family:Rockwell Extra Bold;-fx-font-size:30");  //****set style to scene's title****

            Label title = new Label("  Boarding passengers to the train - Summary of the boarding details   ");  //Create title Label
            title.setStyle("-fx-font-family:Segoe UI Black;-fx-font-size:22;-fx-background-color:#A9A9A9");
            title.setLayoutX(50);
            title.setLayoutY(130);

            //*****Create column to display passenger name*****
            Button[] button1 = new Button[boardedPassenger.size()];
            VBox vBox1 = new VBox(20);
            for (int i = 0; i < boardedPassenger.size(); i++) {
                button1[i] = new Button();
                button1[i].setStyle("-fx-background-color:#00BFFF;-fx-pref-height:50;-fx-pref-width:100");
                button1[i].setText(boardedPassenger.get(i).getName().toUpperCase());
                vBox1.getChildren().add(button1[i]);
            }
            vBox1.setLayoutX(120);
            vBox1.setLayoutY(280);

            //*****Create column to display passenger seat number*****
            Button[] button2 = new Button[boardedPassenger.size()];
            VBox vBox2 = new VBox(20);
            for (int i = 0; i < boardedPassenger.size(); i++) {
                button2[i] = new Button();
                button2[i].setStyle("-fx-background-color:#00BFFF;-fx-pref-height:50;-fx-pref-width:60");
                button2[i].setText(String.valueOf(boardedPassenger.get(i).getSeatNumber()));
                vBox2.getChildren().add(button2[i]);
            }
            vBox2.setLayoutX(330);
            vBox2.setLayoutY(280);

            //*****Create column to display passenger's waiting time*****
            Button[] button3 = new Button[boardedPassenger.size()];
            VBox vBox3 = new VBox(20);
            for (int i = 0; i < boardedPassenger.size(); i++) {
                button3[i] = new Button();
                button3[i].setStyle("-fx-background-color:#00BFFF;-fx-pref-height:50;-fx-pref-width:60");
                button3[i].setText(String.valueOf(boardedPassenger.get(i).getSecondsInQueue()));
                vBox3.getChildren().add(button3[i]);
            }
            vBox3.setLayoutX(550);
            vBox3.setLayoutY(280);

            //*****create column title labels*****
            Label nameTitle=new Label("Boarded Passenger\n Name ");
            nameTitle.setStyle("-fx-font-family:Arial Black;-fx-font-size:18");
            nameTitle.setLayoutX(120);
            nameTitle.setLayoutY(220);
            Label seatNumber=new Label("Boarded Passenger\n Seat Number ");
            seatNumber.setStyle("-fx-font-family:Arial Black;-fx-font-size:18");
            seatNumber.setLayoutX(330);
            seatNumber.setLayoutY(220);
            Label boardingTime=new Label("Boarded Passenger\n Boarding time(seconds) ");
            boardingTime.setStyle("-fx-font-family:Arial Black;-fx-font-size:18");
            boardingTime.setLayoutX(550);
            boardingTime.setLayoutY(220);

            anchorPane.getChildren().addAll(titleLabel, title, vBox1,vBox2,vBox3,nameTitle,seatNumber,boardingTime);
            //*****Display summary of the passenger boarding details*****
            Button okButton=new Button();
            okButton.setText("Go To the Menu");
            okButton.setLayoutX(600);
            okButton.setLayoutY(250);
            okButton.setOnAction(event -> stage.close());

            AnchorPane anchorPane1=new AnchorPane();
            Label maxLengthQueue=new Label("Maximum length of Train Queue : \t"+sizeOfTimeArray);
            maxLengthQueue.setLayoutX(80);
            maxLengthQueue.setLayoutY(50);
            Label totalLabel=new Label("Total waiting time spend in Train Queue : \t"+totalWaitingTime+" seconds");
            totalLabel.setLayoutX(80);
            totalLabel.setLayoutY(80);
            Label averageLabel=new Label("Average time spend in Train Queue :  \t"+averageWaitingTime+" seconds");
            averageLabel.setLayoutX(80);
            averageLabel.setLayoutY(110);
            Label minLabel=new Label("Minimum time took to check one passenger's boarding ticket : \t"+minimumWaitingTime+" seconds");
            minLabel.setLayoutX(80);
            minLabel.setLayoutY(140);
            Label maxLabel=new Label("Maximum time took to check one passenger's boarding ticket : \t"+maximumWaitingTime+" seconds");
            maxLabel.setLayoutX(80);
            maxLabel.setLayoutY(170);
            anchorPane1.getChildren().addAll(maxLengthQueue,totalLabel,averageLabel,minLabel,maxLabel,okButton);
            BorderPane borderPane=new BorderPane();
            borderPane.setPadding(new Insets(30,10,10,10));
            borderPane.setCenter(anchorPane);
            borderPane.setBottom(anchorPane1);


            //*****Create ScrollPane to display details*****
            ScrollPane scrollPane=new ScrollPane();
            scrollPane.setContent(borderPane);
            scrollPane.setPrefSize(600,400);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);   //Display scroll bar in the pane
                scrollPane.setHmax(1);

            Scene scene = new Scene(scrollPane, 900, 1000);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

            //******write summary details in to a text file******
            File summaryFile=new File("boardingSummary.txt");
            FileWriter fw=null;
            PrintWriter pw=null;
            try {
                fw=new FileWriter(summaryFile,true);
                pw=new PrintWriter(fw,true);
                pw.println();
                pw.println("Boarded details of Denuwaramanike Train ");
                for (Passenger detail:boardedPassenger){
                    pw.println("Boarded details");
                    pw.println("Passenger name : "+detail.getName());
                    pw.println("Passenger seat Number : "+detail.getSeatNumber());
                    pw.println("Passenger boarded time : "+detail.getSecondsInQueue());
                    pw.println();
                }
                pw.println();
                pw.println("Train Queue current length : "+sizeOfTimeArray);
                pw.println("Total waiting time spend in Train Queue : "+totalWaitingTime);
                pw.println("Average time spend in Train Queue : "+averageWaitingTime);
                pw.println("Minimum time took to check one passenger's boarding ticket : "+minimumWaitingTime);
                pw.println("Maximum time took to check one passenger's boarding ticket : "+maximumWaitingTime);
            } catch (FileNotFoundException e){
                System.out.println("File not found.");
            } catch (IOException e) {
                System.out.println("No permission to write the file");
            }
            finally {
                try {
                    fw.close();
                    pw.close();
                } catch (IOException e) {
                    System.out.println("Something went wrong .");
                }
            }
        }
    }

    //****Move passengers to the Train Queue****
    private void addPassengerToTrainQueue() {

        System.out.println("Passengers in waiting room : ");
        int totalPassengersInWatingRoom = 0;
        for (int count = 0; count < waitingRoomPassengerList.size(); count++) {
            System.out.println("Passenger Name : " + waitingRoomPassengerList.get(count).getName().toUpperCase());
            System.out.println("Passenger reserved Seat Number : " + waitingRoomPassengerList.get(count).getSeatNumber());
            totalPassengersInWatingRoom++;

        }
        displayWaitingRoom();                       //Call a method to display waiting room

        //***Generate random number and move passengers to the train queue***
        System.out.println("No of Passengers in waiting room : " + totalPassengersInWatingRoom);
        System.out.println("--------------------------------------------------");
        int randomNumber = (int) (Math.random() * ((6-1)+1))+1;     //Generate random number between 1 and 6
        System.out.println("Randomly generated number to select how many passengers join to the Train Queue at a time  :  " + randomNumber);
        System.out.println();
        if (randomNumber <= waitingRoomPassengerList.size()) {
            for (int addPassenger = 0; addPassenger < randomNumber; addPassenger++) {
                trainQueue.add(waitingRoomPassengerList.get(0));      //Always set index to 0
                waitingRoomPassengerList.remove(0);
            }
        } else {
            if (waitingRoomPassengerList.size()==0){
                System.out.println("There is no more passengers in Waiting room to add Train Queue ");
            }else {
                System.out.println("There is no " + randomNumber + " more passengers in Waiting room to add Train Queue . Please roll dice again .");
            }
        }
        System.out.println("-------------------------------------------------");
        int freeSpaceInQueue=SEATING_CAPACITY - trainQueue.getCurrentLength();    //Calculate free space of Train Queue
        if (freeSpaceInQueue==0){
            System.out.println("Train Queue is Full");
        }else {
            System.out.println("Train queue has " +freeSpaceInQueue+" free spaces .");
        }

        displayTrainQueue();     //Call method to display train queue
    }

    //****Check passenger seat reservation and add them to the waiting room****
    private void checkPassengerDetail() {
        Scanner findDetail=new Scanner(System.in);
        String stop="";
        int noOfPresentPassenger=0;    //Number of created Passenger object
        //**Create sub menu to check passenger seat reservation**
        checkingMenu:
        while (!stop.equalsIgnoreCase("q")) {
            System.out.println("-------------------------------------------------------");
            System.out.println("Enter \"C\" to check passenger seat reservation ");
            System.out.println("Enter \"B\" if there is no more passengers to add waiting room ");
            System.out.println("--------------------------------------------------------");
            System.out.println("What is your opinion :");
            stop=findDetail.next().toLowerCase();

            switch (stop){
                case "c":
                    try {
                        Passenger passengerObject = new Passenger();   //create Passenger object
                        Scanner sc = new Scanner(System.in);
                        System.out.println("Enter Passenger Seat Number");
                        int passengerSeatNumber = sc.nextInt();
                        int noOfWaitingRoomPassengers = 0;
                        //Check entered seat number has reservation or not
                        if (seatReservationDetailList.get(passengerSeatNumber - 1) != null) {
                            for (int x = 0; x < waitingRoom.length; x++) {
                                //check entered seat number passenger already in waiting room or not
                                if (waitingRoom[x] != null) {
                                    if (passengerSeatNumber == waitingRoom[x].getSeatNumber()) {
                                        noOfWaitingRoomPassengers++;
                                    }
                                }
                            }
                            if (noOfWaitingRoomPassengers == 0) {
                                System.out.println(seatReservationDetailList.get(passengerSeatNumber - 1).toUpperCase() + "  have seat reservation for  " + passengerSeatNumber + " seat.");
                                passengerObject.setName(seatReservationDetailList.get(passengerSeatNumber - 1));
                                passengerObject.setSeatNumber(passengerSeatNumber);
                                waitingRoom[noOfPresentPassenger] = passengerObject;   //Add Passenger to the waiting room
                                waitingRoomPassengerList.add(passengerObject);

                                noOfPresentPassenger++;
                            } else {
                                System.out.println("Please re-check seat number.This seat number passenger already in the waiting room ");
                            }
                        } else {
                            System.out.println("This seat number do not have any seat reservation .");
                        }

                        break;
                    }catch (Exception e){
                        System.out.println("Invalid Input . Try again ! ");
                        continue ;
                    }
                case "b":
                    System.out.println("All passengers in Train Station added to the waiting room");
                    break checkingMenu;
                default:
                    System.out.println("Invalid Input .Try again");
                    break;
            }
        }
    }

    //****Select train root and load seat reservation details****
    private void loadReservationDetail() {
        try {
            //*****Create stage to select train root*****
            Stage stage = new Stage();
            stage.setTitle("Select date and root");
            AnchorPane anchorPane = new AnchorPane();

            Image logo = new Image("file:trainLogo.png");
            ImageView viewImage = new ImageView();
            viewImage.setImage(logo);
            Label titleLabel = new Label("     DENUWARA MANIKE TRAIN", viewImage);     // create  page title
            titleLabel.setStyle("-fx-font-family:Rockwell Extra Bold;-fx-font-size:30");    //set style for  title
            titleLabel.setLayoutX(20);
            titleLabel.setLayoutY(30);

            //******Create choice box to select train root********
            Label setDateAndTrip = new Label("  Select Train Root   ");  //Create Label to say user to select your date and trip
            setDateAndTrip.setStyle("-fx-font-family:Segoe UI Black;-fx-font-size:22;-fx-background-color:#A9A9A9");
            setDateAndTrip.setLayoutX(50);
            setDateAndTrip.setLayoutY(140);
            Label tripOn = new Label("Select Train Root  ");
            tripOn.setLayoutX(50);
            tripOn.setLayoutY(260);

            ChoiceBox<String> choiceBox = new ChoiceBox<>();
            choiceBox.getItems().add("--Choose the Train Root --");
            choiceBox.getItems().add("Colombo to Badulla");
            choiceBox.getItems().add("Badulla to Colombo");
            choiceBox.setValue("--Choose the Train Root --");
            choiceBox.setLayoutX(270);
            choiceBox.setLayoutY(260);

            //******Get local date to run the program*********
            LocalDate localDate = LocalDate.now();
            System.out.println("Today is : " + localDate);      //Print local date

            //******Create submit button*******
            Button next = new Button("SUBMIT");
            next.setLayoutX(450);
            next.setLayoutY(400);
            next.setStyle("-fx-background-color:#bbdce7; -fx-pref-height:40; -fx-pref-width:100;");
            final String[] date = new String[1];
            final String[] trip = new String[1];
            next.setOnAction(event -> {
                if (choiceBox.getValue().equals("--Choose the Train Root --")) {
                    choiceBox.setStyle("-fx-background-color:#74D5DD");
                } else {
                    date[0] = String.valueOf(localDate);
                    trip[0] = choiceBox.getValue();
                    stage.close();
                }
            });

            anchorPane.getChildren().addAll(titleLabel, setDateAndTrip, tripOn, choiceBox, next);
            Scene scene = new Scene(anchorPane, 700, 500);
            stage.setScene(scene);
            stage.showAndWait();

            HashMap<String, ArrayList<String>> seatBookingDetail = new HashMap<>();   //HashMap to store seat reservations detail from selected root

            String currentDate = date[0];   //****Get Local date****
            //*****************Load seat reservation data from Database***********************************
            if (trip[0].equals("Badulla to Colombo")) {
                java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
                MongoClient client= MongoClients.create("mongodb://LocalHost:27017");       // Create mongo client
                MongoDatabase database = client.getDatabase("TrainSeatReservationDb");   //accessing the database
                MongoCollection<Document> collectionBadulla = database.getCollection("DenuwaraManikeBadulla"); //create table collection

                collectionBadulla.find().forEach(new Block<Document>() {
                    @Override
                    public void apply(Document document) {
                        for (String id : document.keySet()) {
                            if (id.equals("_id"))
                                continue;
                            Object arrayObject = document.get(id);
                            ArrayList loadList = (ArrayList) arrayObject;
                            seatBookingDetail.put(id, loadList);
                        }
                    }
                });
                System.out.println("Successfully Load data from Baddula to Colombo seat reservation");

                //Check database contain reservation list for current date
                if (seatBookingDetail.containsKey(currentDate)) {
                    seatReservationDetailList = seatBookingDetail.get(currentDate);
                } else {
                    String[] seatReservationBadullaTemparyArray = new String[42];
                    for (int i = 0; i < 42; i++) {
                        seatReservationBadullaTemparyArray[i] = null;
                    }//***********Convert Array to ArrayList**************
                    seatReservationDetailList = new ArrayList<>(Arrays.asList(seatReservationBadullaTemparyArray));
                }
            } else {
                java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
                MongoClient client= MongoClients.create("mongodb://LocalHost:27017");           //Creating mongo client
                MongoDatabase database = client.getDatabase("TrainSeatReservationDb");       //accessing the database
                MongoCollection<Document> collectionColombo = database.getCollection("DenuwaraManikeColombo"); //create table

                collectionColombo.find().forEach(new Block<Document>() {
                    @Override
                    public void apply(Document document) {
                        for (String id : document.keySet()) {
                            if (id.equals("_id"))
                                continue;
                            Object arrayObject = document.get(id);
                            ArrayList loadList = (ArrayList) arrayObject;
                            seatBookingDetail.put(id, loadList);
                        }
                    }
                });
                System.out.println("Successfully loaded data from Colombo to Badulla seat reservation");

                //Check database contain reservation list for current date
                if (seatBookingDetail.containsKey(currentDate)) {
                    seatReservationDetailList = seatBookingDetail.get(currentDate);
                } else {
                    String[] seatReservationBadullaTemparyArray = new String[42];
                    for (int i = 0; i < 42; i++) {
                        seatReservationBadullaTemparyArray[i] = null;
                    }
                    seatReservationDetailList = new ArrayList<>(Arrays.asList(seatReservationBadullaTemparyArray));
                }
            }
        }catch (Exception e){
            System.out.println("*******************************************************");
            System.out.println("Before the run program you need to select train root . Please re run the program and select the train root .");
            System.out.println("******************************************************");
            System.out.println();
        }


    }
}

