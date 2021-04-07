package com.example.map_matching_realtime;

import java.util.ArrayList;
import java.util.Vector;

public class Point {
    private Double x; // x좌표 (경도)
    private Double y; // y좌표 (위도)
    private int linkID; // 1-2에서만 필요

    public int getLinkID() {
        return linkID;
    }

    public void setLinkID(int linkID) {
        this.linkID = linkID;
    }

    // LinkID도 파라미터로 받는 생성자
    public Point (Double x, Double y, int linkID){
        this.x = x;
        this.y = y;
        this.linkID = linkID;
    }

    // 스트링으로 파라미터로 받는 생성자
    public Point (String x, String y){
        this.x = Double.parseDouble(x);
        this.y = Double.parseDouble(y);
    }

    // Double형을 파라미터로 가지는 생성자
    public Point (Double x, Double y){
        this.x = x;
        this.y = y;
    }

    // 출력~
    public String toString() {
        return x + "\t"+ y + "\tlink ID: " + linkID;
    }

    public Double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public ArrayList<Link> findRadiusLink(ArrayList<Link> linkArrayList, ArrayList<Node> nodeArrayList){
        int Radiusnum = 3;
        ArrayList<Link> RadiusLinkID = new ArrayList<>();
        for(int i=0;i<linkArrayList.size();i++){

            //원 안에 시작,끝점이 있는 경우
            if(this.coordDistance(nodeArrayList.get(linkArrayList.get(i).getStartNodeID()).getCoordinate())<Radiusnum||
                    this.coordDistance(nodeArrayList.get(linkArrayList.get(i).getEndNodeID()).getCoordinate())<Radiusnum)
                RadiusLinkID.add(linkArrayList.get(i));
            else{// 원 안에 시작, 끝점이 없는 경우
                double startX = nodeArrayList.get(linkArrayList.get(i).getStartNodeID()).getCoordinate().getX();
                double startY = nodeArrayList.get(linkArrayList.get(i).getStartNodeID()).getCoordinate().getY();
                double endX = nodeArrayList.get(linkArrayList.get(i).getEndNodeID()).getCoordinate().getX();
                double endY = nodeArrayList.get(linkArrayList.get(i).getEndNodeID()).getCoordinate().getY();

                double inclination = 0.0;
                double Y_Intercept = 0.0;
                boolean whenZero = false;
                boolean zerosmaller = false;
                if(startX-endX==0){//기울기가 0인 경우 -> x=c인 경우
                    whenZero=true;
                    if(Math.abs(startX-this.getX())<=Radiusnum)
                        zerosmaller=true;
                }
                else{
                    inclination = (startY-endY)/(startX-endX); //기울기
                    Y_Intercept = -(inclination*startX)+startY;} //Y절편

                //원의 중심 부터 직선 사이의 거리가 반지름보다 작은 경우
                if((Math.abs(inclination*this.getX()-this.getY()+Y_Intercept)/Math.sqrt(Math.pow(inclination,2)+1)<Radiusnum&&!whenZero)||zerosmaller) {
                    //원의 중심 부터 직선까지의 거리가 r보다 작다면 다음 식에 들어오게 된다.
                    Vector2D vectorFromCircleToLine1 = new Vector2D(endX-this.getX(),endY-this.getY());
                    Vector2D vectorFromCircleToLine2 = new Vector2D(startX-this.getX(),startY-this.getY());
                    Vector2D vectorLine1 = new Vector2D(startX-endX,startY-endY);
                    Vector2D vectorLine2 = new Vector2D(endX-startX,endY-startY);
                    if((180/Math.PI)*Math.acos(vectorFromCircleToLine1.dot(vectorLine1)/(vectorFromCircleToLine1.getLength()*vectorLine1.getLength()))>=90
                            &&(180/Math.PI)*Math.acos(vectorFromCircleToLine2.dot(vectorLine2)/(vectorFromCircleToLine2.getLength()*vectorLine2.getLength()))>=90) {
                        RadiusLinkID.add(linkArrayList.get(i));
                    }
                }
            }
        }
        return RadiusLinkID;
    }//반경 내에 존재하는 link ID구하기
    //testtest

    public Double coordDistance(Point a){
        return Math.sqrt(Math.pow(a.getX()-this.getX(),2)+Math.pow(a.getY()-this.getY(),2));
    }//점과 점 사이의 거리 구하기
}//test