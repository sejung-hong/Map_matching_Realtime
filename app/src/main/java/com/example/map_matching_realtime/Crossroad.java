package com.example.map_matching_realtime;

import android.util.Pair;

import java.util.ArrayList;

public class Crossroad {

    //갈림길이 있는지 확인, 갈림길이 존재하면 true 존재하지 않으면 false
    public static boolean exist_Crassroad(RoadNetwork roadNetwork, Candidate candidate) {

        //candidate와 candidate가 있는 link의 시작점이나 끝점의 거리를 구함
        Node start, end;
        start = roadNetwork.getNode(candidate.getInvolvedLink().getStartNodeID());
        end = roadNetwork.getNode(candidate.getInvolvedLink().getEndNodeID());

        double start_dis, end_dis;
        start_dis = Calculation.calDistance(candidate.getPoint(), start.getCoordinate());
        end_dis = Calculation.calDistance(candidate.getPoint(), end.getCoordinate());
        //유클리드 거리 구하기

        //갈림길이 5m안에 있을 때 true
        //거리가 5m(근거 없는 5m, 더 늘릴 필요도 있음)
        if (start_dis < 5) { //start노드와의 거리
            //node가 가지는 링크가 3개 이상일 때 갈림길이라고 판단
            if (start.includingLinks(roadNetwork.linkArrayList).size() > 2) {
                return true;
            }
        }
        if (end_dis < 5) { //end노드와의 거리
            if (end.includingLinks(roadNetwork.linkArrayList).size() > 2) {
                return true;
            }
        }

        //갈림길이 5m밖에 있을 때 false
        return false;
    }


    //매칭된 링크가 같은지 같지 않은지 확인하는 함수
    public static int different_Link(RoadNetwork roadNetwork, Candidate candidate_start, Candidate candidate_end) {

        if(candidate_start.getInvolvedLink() == candidate_end.getInvolvedLink()){
            return 0; //같은 링크에 존재 할때
        }

        else{ // 다른 링크에 존재할 때
            if(candidate_start.getInvolvedLink().isLinkNextTo(roadNetwork, candidate_end.getInvolvedLink().getLinkID())){
                //node가 가지는 링크가 3개 이상일 때 갈림길이라고 판단
                Node linked_node;
                linked_node = candidate_start.getInvolvedLink().isLinkNextToPoint_Node(roadNetwork, candidate_end.getInvolvedLink()); //두 링크의 사이 노드
                if(linked_node.includingLinks(roadNetwork.linkArrayList).size() > 2){
                    return 1; //candidate가 서로 다른 링크이고 두 링크가 맞닿아 있을때
                }
                else
                    return 2;
            }
            else{
                return 2; //candidate가 서로 다른 링크이고 두 링크가 맞닿아 있지 않을때
            }
        }
    }

    //맞닿아있는 노드 저장
    public static void different_link_matching(RoadNetwork roadNetwork, Candidate candidate_start, Candidate candidate_end){
        Node linked_node;
        linked_node = candidate_start.getInvolvedLink().isLinkNextToPoint_Node(roadNetwork, candidate_end.getInvolvedLink()); //두 링크의 사이 노드

        Candidate link_candidate = new Candidate(); //Node를 candidate로 작성
        link_candidate.setPoint(linked_node.getCoordinate());

        FSWViterbi.setMatched_sjtp(link_candidate); //맞닿아있는 노드 저장
    }

    //적어놓은 방식
    public static void future_gps(RoadNetwork roadNetwork, ArrayList<GPSPoint> gpsPointArrayList,  ArrayList<ArrayList<Candidate>> arrOfCandidates) {

        ArrayList<Pair<Link, ArrayList<Double>>> link_list = new ArrayList<>();
        Link link;

        ArrayList<Candidate> last_candidates = arrOfCandidates.get(2);

        for(Candidate c : last_candidates){
            link = c.getInvolvedLink();
            ArrayList<Double> ep = new ArrayList<>();
            ep.add(c.getEp());

            link_list.add(new Pair<>(link, ep));
        } //마지막 gps(3초 후 gps)의 candidates의 링크와 ep를 쌍으로 해서 저장


        ///////// 이부분 합쳐도 되지만 일단 그냥 냅둠 /////////
        ArrayList<Candidate> candidates = arrOfCandidates.get(1);
        for(Candidate c: candidates){
            for(int i=0; i<link_list.size(); i++){
                if(c.getInvolvedLink() == link_list.get(i).first){
                    link_list.get(i).second.add(c.getEp()); //ep list에 추가
                    break;
                }
            }
        }
        candidates = arrOfCandidates.get(0);
        for(Candidate c: candidates){
            for(int i=0; i<link_list.size(); i++){
                if(c.getInvolvedLink() == link_list.get(i).first){
                    link_list.get(i).second.add(c.getEp()); //ep list에 추가
                    break;
                }
            }
        }
        ///////// 이부분 합쳐도 되지만 일단 그냥 냅둠 /////////

        double max_aver = 0;
        Link max_link = null;
        for(int i=0; i<link_list.size(); i++){
            double aver = 0;
            for(int j=0; j<link_list.get(i).second.size(); j++){
                aver += link_list.get(i).second.get(j); // ep 더해줌
            }
            aver = aver / link_list.get(i).second.size(); //평균 구하기
            if(max_aver < aver){
                max_aver = aver;
                max_link = link_list.get(i).first;
            }
        }
        //ep의 평균을 구해서 확률이 가장 높은 것을 선택

        for(Candidate c : last_candidates){
            if(max_link == c.getInvolvedLink()){
                Emission.Emission_Median(c); //median값 저장
                FSWViterbi.getMatched_sjtp().get(FSWViterbi.getMatched_sjtp().size()-1).setInvolvedLink(c.getInvolvedLink()); // 중간 Node를 매칭될 링크와 같다고 판단
                FSWViterbi.setMatched_sjtp(c);
            }
        }//확률이 가장 높은 Link의 candidate를 매칭

    }


}
