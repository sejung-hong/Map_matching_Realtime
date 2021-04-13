package com.example.map_matching_realtime;

import java.util.ArrayList;

public class Candidate {

    private Point point;
    private Link involvedLink;
    private double tp;
    private double ep;
    private double tpep;
    private double ep_median;
    private double tp_median;
    private double acc_prob;// accumulated probability (이전 최대 edge와 해당 node의 ep*tp를 곱함)

    public int getPrev_index() {
        return prev_index;
    }

    public void setPrev_index(int prev_index) {
        this.prev_index = prev_index;
    }

    private int prev_index;

    public double getAcc_prob() {
        return acc_prob;
    }

    public void setAcc_prob(double acc_prob) {
        this.acc_prob = acc_prob;
    }

    public Candidate(){
        this.point = null;
        this.involvedLink = null;
        this.tp= 0.0;
        this.ep=0.0;
        this.tpep=0.0;
        this.ep_median=0.0;
        this.tp_median=0.0;
    }

    public Candidate (Point point, Link involvedLink){
        this.point = point;
        this.involvedLink = involvedLink;
        this.tp= 0.0;
        this.ep=0.0;
        this.tpep=0.0;
        this.ep_median=0.0;
        this.tp_median=0.0;
    }

    public void setPoint(Point point){this.point=point;}

    public void setInvolvedLink(Link link){this.involvedLink=link;}

    public Point getPoint() {
        return point;
    }

    public Link getInvolvedLink(){
        return involvedLink;
    }
    //유림 혹시 몰라 push

    public void setTp(double tp) {
        this.tp = tp;
    }


    public void setEp(double ep) {
        this.ep = ep;
    }


    public double getEp() {
        return ep;
    }

    public void setTpep(double tpep){
        this.tpep=tpep;
    }


    public void setEp_median(double ep_median) {this.ep_median = ep_median;}

    public void setTp_median(double tp_median){this.tp_median = tp_median;}

    public void setMax_tp_median(double max_tp_median){this.tp_median = max_tp_median;}

    public double getEp_median() { return ep_median; }

    public double getTp_median() {
        return tp_median;
    }

    @Override
    public String toString() {
        return "point: "+ point + "  involvedLink: " + involvedLink + " tp/ep/tpep: "+tp+"/"+ep+"/"+tpep;//+"\n";
    }

    public String toStringOnlyPoint() {
        return point.toString();
    }

    public static ArrayList<Candidate> findRadiusCandidate(ArrayList<GPSPoint> gpsPointArrayList, Point center,
                                                           Integer Radius, RoadNetwork roadNetwork, int timestamp,
                                                           Emission emission) {
        //System.out.println("GPS (center) : "+ center);
        ArrayList<Candidate> resultCandidate = new ArrayList<>();
        for (int i = 0; i < roadNetwork.linkArrayList.size(); i++) {
            //System.out.println("  위 GPS 에서 [" + roadNetwork.linkArrayList.get(i).getLinkID() + "]로 수선의발");
            double startX = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getStartNodeID()).getCoordinate().getX();
            double startY = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getStartNodeID()).getCoordinate().getY();
            double endX = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getEndNodeID()).getCoordinate().getX();
            double endY = roadNetwork.nodeArrayList.get(roadNetwork.linkArrayList.get(i).getEndNodeID()).getCoordinate().getY();

            // convert start GEO -> TM
            GeoPoint in_pt = new GeoPoint(startX, startY);
            GeoPoint start_t = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);

            // convert end GEO -> TM
            in_pt = new GeoPoint(endX, endY);
            GeoPoint end_t = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);

            // convert center (GPS) GEO -> TM
            in_pt = new GeoPoint(center.getX(), center.getY());
            GeoPoint center_t = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);

            Vector2D vectorFromStartToCenter = new Vector2D(center_t.getX() - start_t.getX(), center_t.getY() - start_t.getY());
            Vector2D vectorFromEndToCenter = new Vector2D(center_t.getX() - end_t.getX(), center_t.getY() - end_t.getY());
            Vector2D vectorFromEndToStart = new Vector2D(start_t.getX() - end_t.getX(), start_t.getY() - end_t.getY());

            double dotProduct1 = vectorFromStartToCenter.dot(vectorFromEndToStart);
            double dotProduct2 = vectorFromEndToCenter.dot(vectorFromEndToStart);
            if (dotProduct1 * dotProduct2 <= 0) {

                //System.out.println("dot Product : "+dotProduct1+", "+dotProduct2);
                Candidate candidate = new Candidate();
                candidate.setInvolvedLink(roadNetwork.linkArrayList.get(i));
                Vector2D vectorStart = new Vector2D(start_t.getX(), start_t.getY());
                Vector2D vectorC = new Vector2D(center_t.getX(), center_t.getY()); //원점에서 시작해 center로의 vector
                Vector2D vectorH = vectorStart.getAdded(vectorFromEndToStart.getMultiplied(
                        (vectorC.getSubtracted(vectorStart).dot(vectorFromEndToStart))
                                / Math.pow(vectorFromEndToStart.getLength(), 2))); //원점에서 시작해 수선의 발로의 vector

                // generate vectorH to TM and convert it TM -> GEO ///
                GeoPoint tm_vector = new GeoPoint(vectorH.getX(), vectorH.getY());
                GeoPoint geo_vector = GeoTrans.convert(GeoTrans.TM, GeoTrans.GEO, tm_vector);

                Point candiPoint = new Point(geo_vector.getX(), geo_vector.getY());
                candiPoint.setLinkID(roadNetwork.linkArrayList.get(i).getLinkID());
                candidate.setPoint(candiPoint); //수선의 발 vector의 x와 y값을 candidate의 point로 대입
                GeoPoint candiPtGeo = new GeoPoint (candiPoint.getX(), candiPoint.getY());
                if (Calculation.calDistance(center.getY(), center.getX(), candiPtGeo.getY(), candiPtGeo.getX()) > Radius)  {
                    continue;
                }
                //System.out.println("  -> 내리기 성공!");
                resultCandidate.add(candidate);
//////////////////////////////////////////
                //candidate마다 ep, tp 구하기
                candidate.setEp(emission.Emission_pro(candidate, center, candidate.getPoint(), timestamp)); //ep구하기
                // caculation에서 계산하지 않고 emision 클래스로 바로 이동

            }

        }

        return resultCandidate;
    }

}