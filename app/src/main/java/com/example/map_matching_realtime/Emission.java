package com.example.map_matching_realtime;


import java.util.ArrayList;

public class Emission {

    private static ArrayList<Double> emission_median = new ArrayList<Double>();

   /* public static Double coordDistanceofPoints(Point a, Point b){
        return Math.sqrt(Math.pow(a.getX()-b.getX(),2)+Math.pow(a.getY()-b.getY(),2));
    }//유클리드 거리 구하기*/

    //emission probability 구하는 함수
    public static double Emission_pro(Candidate cand, Point gps, Point candidate, int size) {
        double ep_distance = 0;

        ep_distance = Calculation.calDistance(candidate, gps); //후보point와 gps point의 유클리드 직선 거리

        cand.setEp_median(ep_distance); //candidiate마다 median 값 저장
        //세정 : 나 왜 이거 여기서 저장해? -> 나중에 median값 저장 쉽게 하기 위해서 해놓음!

        if(emission_median.size() == 0){
            return  (1/ep_distance);
        } //median이 없을 떄 출력

        double ep = 0;
        double sigma=0;
        //System.out.println("ep median 확인" +emission_median.get((emission_median.size() / 2)));

        sigma = (1.4826) * emission_median.get((emission_median.size() / 2));

        ep = Math.exp(Math.pow(Math.abs(ep_distance) / sigma, 2) * (-0.5)) / (Math.sqrt(2 * Math.PI) * sigma);

        return ep;

    } //GPS와 후보의 거리 구하기, 중앙값 배열에 저장

    //중앙값 저장하는 함수, emission에 필요한 중앙값
    public static void Emission_Median(Candidate matching){
        if(emission_median.size() == 0)
            emission_median.add(matching.getEp_median());
        else{
            for(int i=0; i<emission_median.size(); i++){
                if(emission_median.get(i) > matching.getEp_median()){
                    emission_median.add(i, matching.getEp_median());
                    break;
                }
                if(i == emission_median.size()-1){
                    emission_median.add(matching.getEp_median());
                    break;
                }
            }//위치 찾고 삽입하는 과정, 오름차순으로 나열
        }

    }


}