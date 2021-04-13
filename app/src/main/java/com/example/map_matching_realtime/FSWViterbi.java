package com.example.map_matching_realtime;

import java.util.ArrayList;
import java.util.Arrays;

public class FSWViterbi {
    private static final ArrayList<Candidate> matched_yhtp = new ArrayList<>();


    public static ArrayList<Candidate> getMatched_sjtp() {
        return matched_sjtp;
    }

    public static void setMatched_sjtp(Candidate candidate) {
        matched_sjtp.add(candidate);
    }



    private static final ArrayList<Candidate[]> subpaths_sjtp = new ArrayList<>();
    private static final ArrayList<Candidate> matched_sjtp = new ArrayList<>();
    private static double correctness_sjtp;

    // gps 받아올때마다 FSW비터비로 매칭하는 메서드 -윤혜tp
    public static ArrayList<Candidate> generateMatched(int wSize, ArrayList<ArrayList<Candidate>> arrOfCandidates,
                                                       ArrayList<GPSPoint> gpsPointArrayList, int timeStamp, RoadNetwork roadNetwork, ArrayList<Candidate> subMatching) {
        // arrOfCandidates를 순회하며 찾은 path의 마지막을 matching_success에 추가하는 loop
        // t는 timestamp를 의미
        // subpath 생성 및 matched arraylist에 저장
        // 현재 candidates와 다음 candidates로 가는 t.p와 e.p곱 중 최대 값을 가지는 curr와 그 index를 maximum_tpep[현재]에 저장

        double maximum_prob = 0;
        Candidate[] subpath = new Candidate[wSize - 1]; // path의 길이를 t로 설정
        //System.out.println("yhtp debugging");
        for (int i = 0; i < wSize - 1; i++) { // i moves in window
            ArrayList<Candidate> curr_candidates = arrOfCandidates.get(i);
            ArrayList<Candidate> next_candidates = arrOfCandidates.get(i + 1);
            //System.out.println("☆origin point:" + subRPA.get(i+1));// 테스트 하려면 메서드 인자에 subGPSs추가해야함
            //System.out.println("☆GPS point: " + subGPSs.get(i));// 테스트 하려면 메서드 인자에 subRPA추가해야함
            // 다음 candidate를 하나씩 순회
            for (Candidate nc : next_candidates) {
                maximum_prob = 0;
                nc.setEp(Emission.Emission_pro(nc, gpsPointArrayList.get(timeStamp - 1).getPoint(), nc.getPoint(), timeStamp));
                //System.out.println("  nc: " + nc.getPoint() + "/ ep: " + nc.getEp());
                // 현재 candidate를 하나씩 순회하며
                for (Candidate cc : curr_candidates) {
                    double tp = 0.0;
                    tp = Transition.Transition_pro(gpsPointArrayList.get(timeStamp - 1).getPoint(), gpsPointArrayList.get(timeStamp - 3).getPoint(), cc, nc, roadNetwork);
                    //tp = cc.getTp();

                    //cc.setTp(tp); //tp 저장 cc -> nc로 이동할 확률을 cc에 저장 // 굳이 tp 저장할 필요 없음.
                    //cc.setEp(Emission.Emission_pro(cc, gpsPointArrayList.get(timeStamp-2).getPoint(), nc.getPoint(), timeStamp));
                    //이미 저장되어있으므로 저장할 필요 없음.

                    double prob = tp * nc.getEp();

                    cc.setTpep(prob);
                    //System.out.println("    cc: " + cc);

                    if (i == 0) { // window내 window의 시작 부분
                        if (maximum_prob < prob * cc.getEp()) { // 최대의 acc_prob를 갱신하며 이전전
                            maximum_prob = prob * cc.getEp();// window의 시작부분이므로 현재의 ep * 다음의 ep * 현재->다음의tp를 Acc_prob에 축적한다
                            nc.setPrev_index(curr_candidates.indexOf(cc));
                            nc.setAcc_prob(maximum_prob);
                            nc.setMax_tp_median(nc.getTp_median()); // 확률이 제일 높은 median값을 저장
                            //System.out.println("    MAX!");
                        }
                    } else { // window 내 그 외의 부분
                        if (maximum_prob < prob * cc.getAcc_prob()) {
                            maximum_prob = prob * cc.getAcc_prob(); // 현재의 acc_prob * 다음의 ep * 현재->다음의 tp를 Acc_prob에 축적한다
                            nc.setPrev_index(curr_candidates.indexOf(cc));
                            nc.setAcc_prob(maximum_prob);
                            nc.setMax_tp_median(nc.getTp_median()); // 확률이 제일 높은 median값을 저장
                            //System.out.println("    MAX!");
                        }
                    }
                }
            }
        }

        // 마지막 candidates 중 acc_prob가 가장 높은 것 max_last_candi에 저장
        Candidate max_last_candi = new Candidate();
        double max_prob = 0;
        for (Candidate candidate : arrOfCandidates.get(wSize - 1)) {
            if (max_prob < candidate.getAcc_prob()) {
                max_prob = candidate.getAcc_prob();
                max_last_candi = candidate;
            }
        }
        // max_last_candi를 시작으로 back tracing하여 subpath구하기
        //System.out.println("")
        Candidate tempCandi = arrOfCandidates.get(wSize - 2).get(max_last_candi.getPrev_index());
        subpath[subpath.length - 1] = tempCandi;
        //int i = subpath.length - 1;
        for (int j = subpath.length - 2; j >= 0; j--) {
            tempCandi = arrOfCandidates.get(j).get(tempCandi.getPrev_index());
            subpath[j] = tempCandi;
        }

        ArrayList<Candidate> subpathArrayList;
        // 생성된 subpath를 subpaths에 추가
        subpaths_sjtp.add(subpath);
        subpathArrayList = new ArrayList<>(Arrays.asList(subpath));
        // subpath를 모두 매칭!!
        matched_sjtp.addAll(subpathArrayList);
        subMatching.addAll(subpathArrayList);

        for (Candidate c : subpath) {
            Emission.Emission_Median(c);
            Transition.Transition_Median(c);
        } //emission_median값, transition median 저장

        return subpathArrayList;
    }

}