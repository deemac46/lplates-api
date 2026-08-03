// package com.dmc.lplates;

// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.util.ArrayDeque;
// import java.util.Collections;
// import java.util.PriorityQueue;
// import java.util.Queue;

// @SpringBootTest
// class LplatesApplicationTests {


//     int[] myArray = {4,8,22,16,2,10,6,12,18,20};

//     int [] dupe = new int[10];

//     @Test
//     void myInsertionSortTest() {



//         for (int i : myArray) {
//             System.out.print(i+",");
//         }
//         System.out.println();
//         System.out.println("-");

//         for (int i = 1; i < myArray.length; i++) {

//             int current = myArray[i];
//             int j = i-1;
//             while (j>=0 && myArray[j]>current){
//                 myArray[j+1] = myArray[j];
//                 j--;
//             }
//             myArray[j+1] = current;

//         }
//         for (int i : myArray) {
//             System.out.print(i+",");
//         }

//     }


// }
