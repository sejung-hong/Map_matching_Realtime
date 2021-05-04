package com.example.map_matching_realtime;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.os.Bundle;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class FileIO {
    // pilot test 2
    String directoryName;
    private final ArrayList<Integer> horIDList = new ArrayList<>(
            Arrays.asList(-1, -2)); // 완전한 가로는 없음
    private final ArrayList<Integer> verIDList = new ArrayList<>(
            Arrays.asList(-1, -2)); // 완전한 세로도 없음
    private final ArrayList<Integer> diaIDList = new ArrayList<>( // 대각선만 있음.
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                    31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                    41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
                    51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
                    61, 62, 63, 64, 65, 66, 67, 68, 69, 70,
                    71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
                    81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
                    91, 92, 93, 94, 95, 96, 97, 98, 99, 100,
                    101, 102, 103, 104, 105, 106));

    public FileIO(String dir) {
        directoryName = dir;
    }

    RoadNetwork generateRoadNetwork() throws IOException {

        RoadNetwork roadNetwork = new RoadNetwork();

        /*=======Node.txt 파일읽어오기 작업========*/
        //파일 객체 생성
        File file1 = new File(directoryName + "/data1/Node.txt");

        System.out.println("경로 출력 : " + file1.getAbsolutePath());

        if (!file1.exists()) {
            System.out.println("파일을 읽지 못함");
            return roadNetwork;
            //파일을 읽지 못하는 경우
            //emulator에서 data->data->com.example.map_matching->files에 data1(Node.txt, Link.txt)를 추가해주어야함
        } else {
            System.out.println("파일을 읽음");
        }

        //입력 스트림 생성
        FileReader fileReader1 = new FileReader(file1);
        //BufferedReader 클래스 이용하여 파일 읽어오기

        BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
        //System.out.println("======== Node 정보 =======");
        while (bufferedReader1.ready()) {
            String line = bufferedReader1.readLine();
            String[] lineArray = line.split("\t");
            Point coordinate = new Point(lineArray[2], lineArray[1]);// 위도(y), 경도(x) 순서로 저장되어있으므로 순서 바꿈!
            Node node = new Node(lineArray[0], coordinate); // 노드생성
            roadNetwork.nodeArrayList.add(node); // nodeArrayList에 생성한 노드 추가
            //System.out.println(node); //node 정보 출력
        }
        // close the bufferedReader
        bufferedReader1.close();

        /*=======Link.txt 파일읽어오기 작업========*/
        //파일 객체 생성
        File file2 = new File(directoryName + "/data1/Link.txt");
        //입력 스트림 생성
        FileReader fileReader2 = new FileReader(file2);
        //BufferedReader 클래스 이용하여 파일 읽어오기
        BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
        //System.out.println("======== Link 정보 =======");
        while (bufferedReader2.ready()) {
            String line = bufferedReader2.readLine();
            String[] lineArray = line.split("\t");
            //Point coordinate = new Point (lineArray[1], lineArray[2]);
            // weight 구하기 - 피타고라스법칙 적용
            // a=밑변 b=높이 weight=(a제곱+b제곱)의 제곱근
            Double weight = Calculation.calDistance(
                    roadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[1])).getCoordinate().getY(),
                    roadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[1])).getCoordinate().getX(),
                    roadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[2])).getCoordinate().getY(),
                    roadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[2])).getCoordinate().getX()
            );

            // link 생성
            Link link = new Link(lineArray[0], lineArray[1], lineArray[2], weight);
            if (horIDList.contains(link.getLinkID())) {
                link.setItLooksLike("hor");
            } else if (diaIDList.contains(link.getLinkID())) {
                link.setItLooksLike("dia");
            } else if (verIDList.contains(link.getLinkID())) {
                link.setItLooksLike("ver");
            } else {
                link.setItLooksLike("꽝입니다");
            }
            roadNetwork.linkArrayList.add(link); // linkArrayList에 생성한 노드 추가
            //System.out.println(link);  //link 정보 출력
//            System.out.print("involving points:");
//            System.out.println(link.getInvolvingPointList());
        }
        // close the bufferedReader
        bufferedReader2.close();

        return roadNetwork;
    }

}