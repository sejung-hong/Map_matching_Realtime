package com.example.map_matching_realtime;

import java.util.ArrayList;

public class Transition {
    private static ArrayList<Double> transition_median = new ArrayList<Double>();

    public static Double routeDistanceofPoints(Candidate pre_matching, Candidate cand, RoadNetwork roadNetwork) {
        double routeDistance;
        //Point a,b는 링크 안에서의 point

        //a,b가 같은 링크일 때 유클리드 거리
        if (pre_matching.getInvolvedLink() == cand.getInvolvedLink())
            routeDistance = Calculation.calDistance(pre_matching.getPoint(), cand.getPoint());
            //a,b가 다른 링크일 때
        else {
            //case 1: a,b가 다른 링크이고 두 링크가 맞닿아 있을때
            if (pre_matching.getInvolvedLink().isLinkNextTo(roadNetwork, cand.getInvolvedLink().getLinkID())) {
                Point linked_point = new Point(0.0, 0.0); //두 링크가 만나는 점
                linked_point = pre_matching.getInvolvedLink().isLinkNextToPoint(roadNetwork, cand.getInvolvedLink());
                routeDistance = Calculation.calDistance(pre_matching.getPoint(), linked_point) + Calculation.calDistance(cand.getPoint(), linked_point);
                //a와 두 링크가 만나는 점까지 거리 + b와 두 링크가 만나는 점까지 거리
            }
            //case 2: a,b가 다른 링크이고 두 링크가 맞닿아 있지 않을때
            else {
                routeDistance = -1;// false 갈 수 없음, 후보 탈락
            }
        }
        return routeDistance;
    }//경로상의 거리 구하기

    public static double Transition_pro(Point gps_pre, Point gps, Candidate pre_candidate, Candidate candidate, RoadNetwork roadNetwork) {

        double tp_gps_distance, tp_candidate_distance;
        double dt = 0;

        //case 1 : 이전 gps_point 와 gps_point의 유클리드 직선거리
        tp_gps_distance = Calculation.calDistance(gps_pre, gps); //이전gps_point 와 gps_point의 유클리드 직선거리
        //case 2 : 이전 매칭 point와 gps point의 유클리드 직선거리
        //tp_gps_distance = coordDistanceofPoints(matching_pre.getPoint(), gps); //gps와 이전 매칭된 point

        //case 1 : 유클리드 거리
        //tp_candidate_distance = Calculation.calDistance(matching_pre.getPoint(), candidate.getPoint()); //유클리드 거리 (원래 coordDistanceOfPo)
        //case 2 : 경로상의 거리
        tp_candidate_distance = routeDistanceofPoints(pre_candidate, candidate, roadNetwork); //경로상의 거리
        //이전 매칭된point와 후보의 유클리드 직선거리
        //실제 tp는 직선거리가 아니고 경로상의 거리여야함!!

        double tp = 0.000000001;

        if (tp_candidate_distance < 0) {
            tp = 0.000000001;
            //pre_candidate.setExist_tp(1);
            candidate.setTp_median(0);
            return tp;
        } //거리가 0보다 작으면 후보 탈락

        dt = Math.abs(tp_gps_distance - tp_candidate_distance); //gps와 경로 거리 차이 절대값
        candidate.setTp_median(dt); //candidiate마다 median 값 저장

        double beta = 0;

        if(transition_median.size() == 0) {
            tp = 0.000000001;
            //pre_candidate.setExist_tp(1);
            return tp;
        } //median값이 없을때

        //System.out.println("tp median 확인" +transition_median.get((transition_median.size() / 2)));
        beta = transition_median.get(transition_median.size() / 2) / (Math.log(2));
        tp = Math.exp((dt * (-1)) / beta) / beta;
        //tp 구하는 공식

        //candidate.setTp(tp); //tp 저장 cc -> nc로 이동할 확률을 cc에 저장
        return tp;
    }

    //중앙값 저장하는 함수, beta의 median값
    public static void Transition_Median(Candidate matching) {

        if(matching.getTp_median() == 0){
            return;
        }
        //매칭된 candidate에서 median값이 0이라면 transition_median arraylist에 저장하지 않음.
        //tp가 없다는 의미

        if (transition_median.size() == 0){
            transition_median.add(matching.getTp_median());
        }

        else {
            for (int i = 0; i < transition_median.size(); i++) {

                if(matching.getTp_median() == 0)
                    break;
                //매칭된 candidate에서 median값이 0이라면 transition_median arraylist에 저장하지 않음.
                //tp가 없다는 의미

                if (transition_median.get(i) > matching.getTp_median()) {
                    transition_median.add(i, matching.getTp_median());
                    break;
                }
                if (i == transition_median.size() - 1) {
                    transition_median.add(matching.getTp_median());
                    break;
                }
            }//위치 찾고 삽입하는 과정, 오름차순으로 나열
        }

    }


}