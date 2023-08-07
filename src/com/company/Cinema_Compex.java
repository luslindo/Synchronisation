package com.company;

import java.util.concurrent.*;
import java.util.*;

class Cinema_Complex extends Thread {
    Patron aPatron;
    Usher ushers;
    Queue<Patron> BarQueue = new LinkedList<>();
    Bar_Cashier bar_cashier;
    /* PREREQUISITES */

  /* we create the semaphores. First there are no patrons and
   the ushers are resting so we call the constructor with parameter
   0 thus creating semaphores with zero initial permits.
   Semaphore(1) constructs a binary semaphore, as desired. */

    public static  Semaphore accessToilet= new Semaphore(1);
    public static Semaphore patrons = new Semaphore(0);
    public static Semaphore usher = new Semaphore(0);
    public static Semaphore snackbar_cashier = new Semaphore(2);

    /* we denote that the number of chairs in this barbershop is 5. */

    public static final int TICKETS = 10;

  /* we create the integer numberOfAvailable so that the patrons
   can either sit on a available tickets or leave the cinema if there
   are no tickets available */

    public static int numberOfAvailableTickets = TICKETS;


    /* THE PATRON THREAD */

    class Patron extends Thread {

  /* we create the integer iD which is a unique ID number for every customer
     and a boolean notCut which is used in the Customer waiting loop */

        int iD;
        boolean Watch=false;

        /* Constructor for the Patron */

        public Patron() {

        }

        public void run() {
            while (Watch) {  // as long as the patron is not watching
                    if (numberOfAvailableTickets > 0) {  //if there are any Available Tickets
                        System.out.println("Patron " + Thread.currentThread().getId()+ " Entering the cinema .");
                        numberOfAvailableTickets--;  //tickets has been collect by patron
                        toilet_or_Bar();//the Patron visits toilet or snack bar
                        patrons.release();  //notify the Usher that there is a patron
                        //accessSeats.release();  // don't need to lock the chairs anymore
                        try {
                            usher.acquire();  // now it's this patron's turn but we have to wait if the usher is busy
                            this.takeSeat(); // this patron is taking a seat read for the show
                            this.watchShow();  //watching....

                        } catch (InterruptedException ex) {}
                    }
                    else  {  // there are no free seats

                        System.out.println("There are no Available Tickets. Patron " + Thread.currentThread().getId() + " has left the cinema.");

                    }
                }
        }


        /* this method will simulate watching a show */

        public  void takeSeat(){
            System.out.println("Patron " +Thread.currentThread().getId() + " taking a seat ");
        }
        public void watchShow(){
            System.out.println("Patron " + Thread.currentThread().getId()+ " is watching the show ");
            Watch=true;
            try {
                sleep(9090);
                Random rand = new Random();
                if(rand.nextDouble() < 0.75){
                    visit_Toilet();
                }
            } catch (InterruptedException ex) {}

        }

        public void leave() {
            System.out.println("Patron " + Thread.currentThread().getId() + " left the cinema ");
        }
    }

    /*THE BAR CASHIER THREAD*/
    class Bar_Cashier extends Thread{
        public Bar_Cashier(){

        }
        public void run(){
            while(!BarQueue.isEmpty()){
                try {
                    patrons.acquire();
                    attended();
                    snackbar_cashier.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        public void attended(){
            Patron patron=BarQueue.poll();
            System.out.println("Bar Cashier "+this.getId()+" attended Patron "+patron.getId());
        }
    }
    /* THE USHER THREAD */

    class Usher extends Thread {
        public Usher() {
        }

        public void run() {
            while(true) {  // runs in an infinite loop
                try {
                    patrons.acquire(); // tries to acquire a patrons - if none is available ushers rest
                    allocation();  //allocating ...
                    usher.release();  // the usher is ready to take and allocate
                } catch (InterruptedException ex) {}
            }
        }
        void allocate(){
            System.out.println("Usher "+Thread.currentThread().getId()+" allocated "+aPatron.getId()+" to a seat");
        }
    }

    /* main method */

    public static void main(String args[]) {

        Cinema_Complex cinema_complex = new Cinema_Complex();  //Creates a new cinema_complex
        cinema_complex.start();  // Let the simulation begin
    }
    private void visitSnackBar() {
        System.out.println("Patron "+ Thread.currentThread().getId() +" is visiting SnackBar"+"and entering the queue ");
        try {
            BarQueue.add(aPatron);
            snackbar_cashier.acquire();
            bar_cashier.attended();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("patron "+aPatron.getId()+" leave the Snack Bar");
        }


    }

    private synchronized void visit_Toilet() {

        System.out.println("Patron "+ Thread.currentThread().getId() +" is visiting toilet");
        try {
            accessToilet.acquire();
            sleep(5050);
        } catch (InterruptedException ex) {}
        finally {
            accessToilet.release();

        }
        if(!aPatron.Watch){
            System.out.println("Patron "+ Thread.currentThread().getId() +"  left the Toilet and go to Ushers ");
        }else {
            System.out.println("Patron "+ Thread.currentThread().getId() +"  left the Toilet and to watch again  ");
        }


    }
    public void toilet_or_Bar() {
        int randomise = (int) (Math.random() * 2);
        if (randomise == 0)
            visitSnackBar();
        else{
                visit_Toilet();

        }
    }
    /* this method will simulate allocating method */

    public void allocation(){
        ushers.allocate();
        try {
            sleep(5000);
        } catch (InterruptedException ex){ }
    }

    public void run(){
        for (int i = 0; i < 2; i++) {
            ushers=new Usher();
            ushers.start();
        }

        for (int i = 0; i < 2; i++) {
            bar_cashier=new Bar_Cashier();
            bar_cashier.start();
        }

        //Ready for another day of work

        /* This method will create new patron for a while */

        for (int i=1; i<100; i++) {
            aPatron = new Patron();
            aPatron.start();
            try {
                sleep(2000);
            } catch(InterruptedException ex) {}
        }
    }
}
