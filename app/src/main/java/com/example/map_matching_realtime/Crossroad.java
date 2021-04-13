package com.example.map_matching_realtime;

import android.util.Pair;

import java.util.ArrayList;

public class Crossroad {

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
    public static void different_link_matching(RoadNetwork roadNetwork, Candidate candidate_start, Candidate candidate_end, ArrayList<Candidate> subMatching){
        Node linked_node;
        linked_node = candidate_start.getInvolvedLink().isLinkNextToPoint_Node(roadNetwork, candidate_end.getInvolvedLink()); //두 링크의 사이 노드

        Candidate link_candidate = new Candidate(); //Node를 candidate로 작성
        link_candidate.setPoint(linked_node.getCoordinate());

        FSWViterbi.setMatched_sjtp(link_candidate); //맞닿아있는 노드 저장
        subMatching.add(link_candidate);
    }

    //적어놓은 방식
    public static void future_gps(ArrayList<ArrayList<Candidate>> arrOfCandidates, ArrayList<Candidate> subMatching) {

        ArrayList<Pair<Link, ArrayList<Double>>> link_list = new ArrayList<>();
        Link link;

        int size = arrOfCandidates.size(); //사이즈 저장

        ArrayList<Candidate> last_candidates = arrOfCandidates.get(size-1);

        for(Candidate c : last_candidates){
            link = c.getInvolvedLink();
            ArrayList<Double> ep = new ArrayList<>();
            ep.add(c.getEp());

            link_list.add(new Pair<>(link, ep));
        } //마지막 gps(3초 후 gps)의 candidates의 링크와 ep를 쌍으로 해서 저장


        for(int j=0; j< size-1; j++){
            ArrayList<Candidate> candidates = arrOfCandidates.get(j);
            for(Candidate c: candidates){
                for(int i=0; i<link_list.size(); i++){
                    if(c.getInvolvedLink() == link_list.get(i).first){
                        link_list.get(i).second.add(c.getEp()); //ep list에 추가
                        break;
                    }
                }
            }
        } //arrOfCandidates의 link가 후보에 존재한다면 ep 저장

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
                subMatching.add(c);
            }
        }//확률이 가장 높은 Link의 candidate를 매칭

    }


}
