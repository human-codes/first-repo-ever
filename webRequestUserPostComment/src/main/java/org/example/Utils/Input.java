package org.example.Utils;

import java.util.Scanner;

public class Input {
    public static int anInt(String msg){
        Scanner scanner=new Scanner(System.in);
        System.out.print(msg);
        if(scanner.hasNextInt()){
            return scanner.nextInt();
        }
        else{
            System.out.println("Wrong command - TRY AGAIN!");
            return anInt(msg);
        }
    }

    public static String stringIn(String msg){
        Scanner scannerStr=new Scanner(System.in);
        System.out.print(msg);
        if (scannerStr.hasNextLine()){
            return scannerStr.nextLine();
        }
        else {
            System.out.println("Wrong command - TRY AGAIN!");
            return stringIn(msg);
        }
    }




}
