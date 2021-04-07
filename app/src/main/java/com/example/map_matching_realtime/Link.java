package com.example.map_matching_realtime;
import java.util.ArrayList;

public class Link {
    private int linkID; // Link ID
    private int startNodeID; // Link의 Start Node
    private int endNodeID; // Link의 End Node
    private Double weight; // Link의 weight (길이)
    private int width;
    private String itLooksLike; // 수평: hor, 수직: ver, 대각선: dia

    public String getItLooksLike() {
        return itLooksLike;
    }

    public void setItLooksLike(String itLooksLike) {
        this.itLooksLike = itLooksLike;
    }

    //////생성자, getter, setter, toString//////
    // int로 ID 파라미터 받음
    public Link (int linkID, int startNodeID, int endNodeID, Double weight){
        this.linkID = linkID;
        this.startNodeID = startNodeID;
        this.endNodeID = endNodeID;
        this.weight = weight;
    }

    public Link (int linkID, int startNodeID, int endNodeID, Double weight,int width){
        this.linkID = linkID;
        this.startNodeID = startNodeID;
        this.endNodeID = endNodeID;
        this.weight = weight;
        this.width = width;
    }

    // String으로 ID 파라미터 받음
    public Link(String linkID, String startNodeID, String endNodeID, Double weight) {
        this.linkID = Integer.parseInt(linkID);
        this.startNodeID = Integer.parseInt(startNodeID);
        this.endNodeID = Integer.parseInt(endNodeID);
        this.weight = weight;
        this.width = 0;
    }
    // String으로 ID 파라미터 받음
    public Link(String linkID, String startNodeID, String endNodeID, Double weight,String width) {
        this.linkID = Integer.parseInt(linkID);
        this.startNodeID = Integer.parseInt(startNodeID);
        this.endNodeID = Integer.parseInt(endNodeID);
        this.weight = weight;
        this.width = Integer.parseInt(width);
    }

    public String toString() {
        return "[" + linkID + "]\t" + "(" + startNodeID +", "
                + endNodeID+")" + "\t" + "weight: " + weight + " itLooksLike: "+ itLooksLike;
    }

    public int getWidth(){return width;}

    public void setWidth(int width){this.width = width;}

    public int getLinkID() {
        return linkID;
    }

    public void setLinkID(int linkID) {
        this.linkID = linkID;
    }

    public int getStartNodeID() {
        return startNodeID;
    }

    public void setStartNodeID(int startNodeID) {
        this.startNodeID = startNodeID;
    }

    public int getEndNodeID() {
        return endNodeID;
    }

    public void setEndNodeID(int endNodeID) {
        this.endNodeID = endNodeID;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /*public ArrayList<Point> getInvolvingPointList() {
        return involvingPointList;
    }
    public void setInvolvingPointList(ArrayList<Point> involvingPointList) {
        this.involvingPointList = involvingPointList;
    }*/
    //////////////////////////////////////

    //  [VERIFIED] 이 링크의 startNode(이 아이)와 이웃하는(이 아이를 startNode혹은 endNode로 가지는) links 출력
    public ArrayList<Link> linksNeighborOnStartNode (RoadNetwork roadNetwork) {
        ArrayList<Link> result = roadNetwork.getNode(startNodeID).includingLinks(roadNetwork.linkArrayList);
        for (int i=0;i<result.size();i++) {
            Link l = result.get(i);
            if (l.getLinkID() == linkID) {
                result.remove(l);
                i--;
            }
        }
        return result;
    }

    //  [VERIFIED] 이 링크의 endNode(이 아이)와 이웃하는(이 아이를 startNode혹은 endNode로 가지는) links 출력
    public ArrayList<Link> linksNeighborOnEndNode (RoadNetwork roadNetwork) {
        ArrayList<Link> result =  roadNetwork.getNode(endNodeID).includingLinks(roadNetwork.linkArrayList);
        for (int i=0;i<result.size();i++) {
            Link l = result.get(i);
            if (l.getLinkID() == linkID) {
                result.remove(l);
                i--;
            }
        }
        return result;
    }

    //  [VERIFIED] 이 링크의 startNode endNode(이 아이)와 이웃하는(이 아이를 startNode혹은 endNode로 가지는) links 출력
    public ArrayList<Link> linksNeighborOnStartOrEndNode (RoadNetwork roadNetwork) {
        ArrayList<Link> resultLinks = new ArrayList<>();
        resultLinks.addAll(linksNeighborOnStartNode(roadNetwork));
        resultLinks.addAll(linksNeighborOnEndNode(roadNetwork));

        return resultLinks;
    }

    // 11/20에 만듦: param으로 받은 link와 이 링크가 이웃하는지 여부 출력
    public boolean isLinkNextTo(RoadNetwork rn, int _linkID) {
        if (this.linksNeighborOnStartOrEndNode(rn) != null) {
            for (Link l : this.linksNeighborOnStartOrEndNode(rn)) {
                if (l.getLinkID() == _linkID)
                    return true;
            }
        } return false;
    }

    //두 링크가 연결되어있을때 연결되어있는 노드
    public Point isLinkNextToPoint(RoadNetwork rn, Link _linkID){
        Point linked_point = new Point(0.0, 0.0);

        if (this.getStartNodeID() == _linkID.getStartNodeID()) {
            linked_point = rn.getNode(this.getStartNodeID()).getCoordinate(); //point 반환
        }
        else if(this.getStartNodeID() == _linkID.getEndNodeID()){
            linked_point = rn.getNode(this.getStartNodeID()).getCoordinate(); //point 반환
        }
        else if(this.getEndNodeID() == _linkID.getStartNodeID()){
            linked_point = rn.getNode(this.getEndNodeID()).getCoordinate(); //point 반환
        }
        else if(this.getEndNodeID() == _linkID.getEndNodeID()){
            linked_point = rn.getNode(this.getEndNodeID()).getCoordinate(); //point 반환
        }

        return linked_point;
    }
    public Node isLinkNextToPoint_Node(RoadNetwork rn, Link _linkID){

        Node linked_point = null;
        if (this.getStartNodeID() == _linkID.getStartNodeID()) {
            linked_point = rn.getNode(this.getStartNodeID()); //Node 반환
        }
        else if(this.getStartNodeID() == _linkID.getEndNodeID()){
            linked_point = rn.getNode(this.getStartNodeID()); //Node 반환

        }
        else if(this.getEndNodeID() == _linkID.getStartNodeID()){
            linked_point = rn.getNode(this.getEndNodeID()); //Node 반환

        }
        else if(this.getEndNodeID() == _linkID.getEndNodeID()){
            linked_point = rn.getNode(this.getEndNodeID()); //Node 반환

        }

        return linked_point;
    }

    // 11/20에 만듦: 이 링크와 이웃한 링크 개수 출력
    public int nextLinksNum(RoadNetwork rn) {
        int n = 0;
        if (this.linksNeighborOnStartOrEndNode(rn) != null) {
            for (Link l : this.linksNeighborOnStartOrEndNode(rn)) {
                n++;
            }
        } return n;
    }

    public static ArrayList<Link> AdjacentLink(Link mainLink,RoadNetwork roadNetwork,ArrayList<AdjacentNode> heads){
        int startNode=mainLink.getStartNodeID();
        int endNode = mainLink.getEndNodeID();
        ArrayList<Link> secondLink = new ArrayList<>();
        //ArrayList<Node> startAdjacentNode = new ArrayList<>();
        //ArrayList<Node> endAdjacentNode = new ArrayList<>();
        AdjacentNode pointer = heads.get(roadNetwork.nodeArrayList.get(startNode).getNodeID()).getNextNode();
        while(true){
            if(pointer==null) break;
            secondLink.add(roadNetwork.getLink(pointer.getNode().getNodeID(),startNode));
            pointer=pointer.getNextNode();
        }
        pointer = heads.get(roadNetwork.nodeArrayList.get(endNode).getNodeID()).getNextNode();
        while(true){
            if(pointer==null) break;
            secondLink.add(roadNetwork.getLink(pointer.getNode().getNodeID(),endNode));
            pointer=pointer.getNextNode();
        }
        return secondLink;
    }
}