package com.example.map_matching_realtime;

import java.util.ArrayList;
import java.util.Arrays;

public class FSWViterbi {
    private static final ArrayList<Candidate[]> subpaths_yhtp = new ArrayList<>();
    private static final ArrayList<Candidate> matched_yhtp = new ArrayList<>();
    private static double correctness_yhtp;

    public static ArrayList<Candidate> getMatched_yhtp() {
        return matched_yhtp;
    }
    public static ArrayList<Candidate> getMatched_sjtp() {
        return matched_sjtp;
    }
    public static void setMatched_sjtp(Candidate candidate){
        matched_sjtp.add(candidate);
    }
    public static void setMatched_yhtp(Candidate candidate){
        matched_sjtp.add(candidate);
    }


    private static final ArrayList<Candidate[]> subpaths_sjtp = new ArrayList<>();
    private static final ArrayList<Candidate> matched_sjtp = new ArrayList<>();
    private static double correctness_sjtp;

    // gps 받아올때마다 FSW비터비로 매칭하는 메서드 -윤혜tp
    public static ArrayList<Candidate> generateMatched(double[][] tp_matrix , int wSize, ArrayList<ArrayList<Candidate>> arrOfCandidates,
                                                                                            ArrayList<GPSPoint> gpsPointArrayList, /*ArrayList<Point> subRPA, ArrayList<GPSPoint> subGPSs, */
                                                                                            Transition transition, int timeStamp, RoadNetwork roadNetwork, String tp_type) {
        // arrOfCandidates를 순회하며 찾은 path의 마지막을 matching_success에 추가하는 loop
        // t는 timestamp를 의미
        // subpath 생성 및 matched arraylist에 저장
        // 현재 candidates와 다음 candidates로 가는 t.p와 e.p곱 중 최대 값을 가지는 curr와 그 index를 maximum_tpep[현재]에 저장

        double maximum_prob = 0;
        Candidate[] subpath = new Candidate[wSize-1]; // path의 길이를 t로 설정
        //System.out.println("yhtp debugging");
        for (int i = 0; i < wSize - 1; i++) { // i moves in window
            ArrayList<Candidate> curr_candidates = arrOfCandidates.get(i);
            ArrayList<Candidate> next_candidates = arrOfCandidates.get(i+1);
            //System.out.println("☆origin point:" + subRPA.get(i+1));// 테스트 하려면 메서드 인자에 subGPSs추가해야함
            //System.out.println("☆GPS point: " + subGPSs.get(i));// 테스트 하려면 메서드 인자에 subRPA추가해야함
            // 다음 candidate를 하나씩 순회
            for (Candidate nc : next_candidates) {
                maximum_prob = 0;
                nc.setEp(Emission.Emission_pro(nc, gpsPointArrayList.get(timeStamp-1).getPoint(), nc.getPoint(), timeStamp));
                System.out.println("  nc: " + nc.getPoint() + "/ ep: " + nc.getEp());
                // 현재 candidate를 하나씩 순회하며
                for (Candidate cc : curr_candidates) {
                    double tp;
                    if (tp_type.equals("yh")) {
                        tp = tp_matrix[cc.getInvolvedLink().getLinkID()][nc.getInvolvedLink().getLinkID()];
                        //cc.setTp(tp); //tp 저장 cc -> nc로 이동할 확률을 cc에 저장 // 굳이 tp 저장할 필요 없음.
                    } else {
                        //System.out.println("[FSWViterbi] cc:" + cc);
                        tp = Transition.Transition_pro(gpsPointArrayList.get(timeStamp-1).getPoint(), gpsPointArrayList.get(timeStamp-3).getPoint(), cc, nc, roadNetwork);
                        //tp = cc.getTp();
                    }
                    //cc.setTp(tp); //tp 저장 cc -> nc로 이동할 확률을 cc에 저장 // 굳이 tp 저장할 필요 없음.
                    //cc.setEp(Emission.Emission_pro(cc, gpsPointArrayList.get(timeStamp-2).getPoint(), nc.getPoint(), timeStamp));
                    //이미 저장되어있으므로 저장할 필요 없음.

                    double prob = tp * nc.getEp();

                    cc.setTpep(prob);
                    System.out.println("    cc: " + cc);

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
        if (tp_type.equals("yh")) {
            subpaths_yhtp.add(subpath);
            subpathArrayList = new ArrayList<>(Arrays.asList(subpath));
            // subpath를 모두 매칭!!
            matched_yhtp.addAll(subpathArrayList);
        } else {
            subpaths_sjtp.add(subpath);
            subpathArrayList = new ArrayList<>(Arrays.asList(subpath));
            // subpath를 모두 매칭!!
            matched_sjtp.addAll(subpathArrayList);

            for(Candidate c:subpath){
                Emission.Emission_Median(c);
                Transition.Transition_Median(c);
            } //emission_median값, transition median 저장

        }
        return subpathArrayList;
    }
    // subpath출력 메서드 (테스트용) -윤혜tp
    public static void printSubpath(int wSize, String tp_type) {
        // subpath 출력
        int t = wSize - 2;
        ArrayList<Candidate[]> subpaths = new ArrayList<>();
        if (tp_type.equals("yh")) {
            subpaths = subpaths_yhtp;
        } else {
            subpaths = subpaths_sjtp;
        }

        for (Candidate[] subpath : subpaths) {
            System.out.print(t + "] ");
            for (int i = 0; i < subpath.length; i++) {
                System.out.print("[" + subpath[i].getInvolvedLink().getLinkID() + "]");
                if (i != subpath.length - 1)
                    System.out.print(" ㅡ ");
            }
            System.out.println();
            t++;
        }
    }

    public static void test (RoadNetwork rn, String tp_mode) {

        ArrayList<Candidate> matched;
        if (tp_mode.equals ("yh")) {
            matched = matched_yhtp;
        } else {
            matched = matched_sjtp;
        }
        int i = 0;
        double correctness = 0, total = 0;
        for (i = 0 ; i < matched_yhtp.size(); i++) {
            //System.out.println("origin link: " + rn.routePointArrayList.get(i).getLinkID() + ", matched link: " + c.getInvolvedLink().getLinkID() );
            Candidate c = matched_yhtp.get(i);
            Point point = rn.routePointArrayList.get(5*i);
            System.out.println("Point: " + point + " -> Matched: " + c);
            if (point.getLinkID() == matched.get(i).getInvolvedLink().getLinkID()) {
                correctness ++ ;
            }
            total ++;
            i++;
        }
        if (tp_mode.equals ("yh")) {
            correctness_yhtp = 100*(correctness/total);
            System.out.println("yhtp matched: " + matched_yhtp.size() + ", origin points: " + rn.routePointArrayList.size());
            System.out.println(tp_mode + "tp correctness: " + correctness_yhtp);
        } else {
            correctness_sjtp = 100*(correctness/total);
            System.out.println("sjtp matched: " + matched_yhtp.size() + ", origin points: " + rn.routePointArrayList.size());
            System.out.println(tp_mode + "tp correctness: " + correctness_sjtp);

        }

    }

    // yhtp와 sjtp 비교용 메서드 (테스트용)
    public static void compareYhtpAndSjtp () {
        System.out.println("========= Compare correctness =========\nCorrectness(yhtp) = " + correctness_yhtp);
        System.out.println("Correctness(sjtp) = " + correctness_sjtp+"\n=======================================");
    }

    public static void compareYHandSJ() {
        int i = 0;
        int j = 0;
        for (Candidate c: matched_yhtp) {
            if (c.getPoint().getX().compareTo(matched_sjtp.get(i).getPoint().getX()) != 0 ||
                    c.getPoint().getY().compareTo(matched_sjtp.get(i).getPoint().getY()) != 0) {
                System.out.println(i + "] NOT SAME!");
                System.out.println("   yh: " + c + "\n   sj:" + matched_sjtp.get(i).getPoint());
                j++;
            }
            i++;
        }
        double prob = ((double)j/(double)i)*100;
        System.out.println("yhtp의 정확도:" + correctness_yhtp);
        System.out.println("sjtp의 정확도:" + correctness_sjtp);
        //System.out.println("=> 두 tp의 매칭 결과가 다른 확률:" + prob);
    }

}