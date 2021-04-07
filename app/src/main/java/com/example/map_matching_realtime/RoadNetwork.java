package com.example.map_matching_realtime;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class RoadNetwork {

    // 데이터를 보관할 ArrayList들을 담는 class..
    // 필요 없을 수도 있지만 혹시의 상황을 대비해서 만들었음
    // 일단 이런 구조로 ArrayList로 선언해보고 이 클래스 정 필요 없겠다 싶으면 다음 테스트에서 다시 파기 예정
    // 만약 필요하게 되면 private으로 만들어주고 getter setter만들 예정
    protected ArrayList<Node> nodeArrayList = new ArrayList<>();
    protected ArrayList<Link> linkArrayList = new ArrayList<>();

    protected ArrayList<Point> routePointArrayList = new ArrayList<>();

    // _nodeID를 nodeID로 가지는 node반환
    public Node getNode (int _nodeID) {
        for (Node currNode : nodeArrayList) {
            if (currNode.getNodeID() == _nodeID) {
                return currNode;
            }
        }
        // 탐색에 실패한 경우 nodeId가 -1인 Node반환
        return new Node(-1, new Point((double)-99,(double)-99));
    }

    public Node getNode1 (Point nodePoint){
        for(Node currNode : nodeArrayList){
            if(currNode.getCoordinate().getX().doubleValue() == nodePoint.getX().doubleValue()
                    && currNode.getCoordinate().getY().doubleValue() == nodePoint.getY().doubleValue())
                return currNode;
        }
        // 탐색에 실패한 경우 nodeId가 -1인 Node반환
        return new Node(-1, new Point((double)-99,(double)-99));
    }

    // _linkID를 linkID로 가지는 link반환
    public Link getLink (int _linkID) {
        for (Link currLink : linkArrayList) {
            if (currLink.getLinkID() == _linkID) {
                return currLink;
            }
        }
        // 탐색에 실패한 경우 nodeId가 -1인 Link반환
        return new Link(-1,-1,-1,(double)-1);
    }
    // nodeID_s를 start node ID로, nodeID_e를 end node id로 가지는 가지는 link반환
    // 혹은 nodeID_e를 start node ID로, nodeID_s를 end node id로 가지는 가지는 link반환
    public Link getLink (int nodeID_s, int nodeID_e) {
        for (Link currLink : linkArrayList) {
            if ((currLink.getStartNodeID() == nodeID_s) && (currLink.getEndNodeID() == nodeID_e)
                    || (currLink.getStartNodeID() == nodeID_e) && (currLink.getEndNodeID() == nodeID_s)) {
                return currLink;
            }
        }
        // 탐색에 실패한 경우 nodeId가 -1인 Link반환
        return new Link(-1,-1,-1,(double)-1);
    }
    List<Pair<Link,Integer>> getLink1 (int nodeID) {
        List<Pair<Link,Integer>> pairs = new ArrayList<>();
        for (Link currLink : linkArrayList) {
            if (currLink.getStartNodeID() == nodeID) {
                pairs.add(new Pair<Link,Integer>(currLink,currLink.getEndNodeID()));
            }
            else if(currLink.getEndNodeID() == nodeID){
                pairs.add(new Pair<Link,Integer>(currLink,currLink.getStartNodeID()));
            }
        }
        return pairs;
    }

    // testNo에 맞게 경로 Point로 생성하는 작업
    // 아직  startNode가 닿는지 endNode가 닿는지에 따라 순서대로/역순으로 나오는 로직은 추가 안함
    /*왼쪽에서 오른쪽으로 가는 방향만 고려함 (왼, 오를 따질 수 없는 경우는 아래에서 위로 가는 방향만 고려)
     *되는 루트 →, ↑, ↗,↘
     *안되는 루트: ←, ↓, ↙, ↖
     * */
    public ArrayList<Point> routePoints (int testNo) {
        ArrayList<Point> routePoints = new ArrayList<>();

        if(testNo == 1){
            int[] routeNodes = { 0, 10, 7, 9, 6, 5, 4, 3, 2, 1};
            for (int i=0; i<routeNodes.length-1; i++) {
                Link routelink = getLink(routeNodes[i], routeNodes[i+1]); //두 노드를 끝으로 하는 링크 반환
                routePoints.addAll(getInvolvingPointList(getNode(routeNodes[i]).getCoordinate(),
                        getNode(routeNodes[i+1]).getCoordinate(), routelink.getWeight()));

                /*
                routePoints.addAll(getInvolvingPointList(getNode(routelink.getStartNodeID()).getCoordinate(),
                        getNode(routelink.getEndNodeID()).getCoordinate()));*/
            }
        }
        routePointArrayList = routePoints;
        return routePoints;
    }

    // link개수 출력하기
    int getLinksSize () {
        return linkArrayList.size();
    }

    // 우리 route node만 입력 해도 실제 경로 쭈르륵 떠야 해서 이 부분에 involving point list 살짝 변경해서 넣음
    // GPS데이터 생성을 위한 Point.linkID 설정하는 코드 추가
    public ArrayList<Point> getInvolvingPointList(Point start, Point end, Double weight){

        //involving points 구하기

        // start point와 end point 좌표 지정
        double xs = start.getX();
        double ys = start.getY();
        double xe = end.getX();
        double ye = end.getY();

        ArrayList<Point> involvingPointList = new ArrayList<>();

        int linkID = getLink(getNode1(new Point(xs, ys)).getNodeID(), getNode1(new Point(xe, ye)).getNodeID()).getLinkID();

        //기울기와 상관없이 구하겠음
        double deltaX = (xe-xs)/(Math.round(weight)); //X 변화값
        double deltaY = (ye-ys)/(Math.round(weight)); //Y 변화값
        //(int)(Math.round(weight)) : weight를 반올림하여 정수로 나타냄

        for(int i = 0; i < (int)(Math.round(weight)); i++) {
            involvingPointList.add(new Point(xs + (i*deltaX), ys + (i*deltaY), linkID));
        } // involvingPointList에 Point 추가

        return involvingPointList;
    }



}