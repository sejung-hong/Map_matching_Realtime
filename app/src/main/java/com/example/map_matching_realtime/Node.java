package com.example.map_matching_realtime;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Node {
    private int nodeID; // nodeID
    private Point coordinate; // Node의 좌표
    //////생성자, getter, setter, toString//////
    // ID를 String형으로 받는 Node 생성자
    public Node (String nodeID, Point coordinate) {
        this.nodeID = Integer.parseInt(nodeID);
        this.coordinate = coordinate;
    }

    // ID를 int형으로 받는 Node 생성자
    public Node (int nodeID, Point coordinate) {
        this.nodeID = nodeID;
        this.coordinate = coordinate;
    }

    // Getters and Setters
    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public Point getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Point coordinate) {
        this.coordinate = coordinate;
    }

    public String toString() {
        return "[" + nodeID + "]\t" + "(" +coordinate.getX().toString() +", "
                + coordinate.getY().toString()+")";
    }
    //////////////////////////////////////////////

    // [NOT VERIFIED] 이 노드를 startNode 혹은 endNode로 가지는 link의 arraylist 반환
    public ArrayList<Link> includingLinks (ArrayList<Link> linkArrayList) {
        ArrayList<Link> resultLinks = new ArrayList<>();
        for (Link link : linkArrayList) {
            if (link.getStartNodeID() == nodeID || link.getEndNodeID() == nodeID)
                resultLinks.add(link);
        }
        return  resultLinks;
    }
}